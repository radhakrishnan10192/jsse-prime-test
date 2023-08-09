package com.paypal.jsse.benchmark.client.lnp;

import com.paypal.jsse.benchmark.client.HttpsClient;
import com.paypal.jsse.benchmark.config.JsseTestSysProps;
import com.paypal.jsse.benchmark.client.metrics.MetricsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static reactor.core.publisher.Mono.fromCompletionStage;

public abstract class HttpsClientLoadSim<C> {

    private static final Logger logger = LoggerFactory.getLogger(HttpsClientLoadSim.class);

    private final HttpsClient<C> httpsClient;
    private final MetricsRegistry metricsRegistry;

    private final JsseTestSysProps.HttpClientLoadConfig httpClientLoadConfig;

    private final CountDownLatch countDownLatch;

    public HttpsClientLoadSim()  {
        try {
            countDownLatch = new CountDownLatch(1);
            this.metricsRegistry = new MetricsRegistry();
            httpClientLoadConfig = new JsseTestSysProps.HttpClientLoadConfig();
            httpsClient = createHttpsClient(httpClientLoadConfig.getHost(),
                    httpClientLoadConfig.getPort(),
                    metricsRegistry);
            executeClientLoadTest();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    protected abstract HttpsClient<C> createHttpsClient(final String host,
                                                        final int port,
                                                        final MetricsRegistry metricsRegistry);


    public void executeClientLoadTest() throws InterruptedException {
        final long warmupTestStartTime = System.nanoTime();
        logger.info("\n\nStarting warmup test...");
        mkCalls(httpClientLoadConfig.getWarmupCount(),
                httpClientLoadConfig.getWarmupBucketSize(),
                httpClientLoadConfig.getWarmupDelayForEachBucket())
                .subscribe(warmupResponses -> {
                    logTestStatus("Warmup", warmupTestStartTime, warmupResponses);
                    this.metricsRegistry.resetAll();
                    final long loadTestStartTime = System.nanoTime();
                    logger.info("\n\nStarting load test...");
                    mkCalls(httpClientLoadConfig.getTotalNumberOfCalls(),
                            httpClientLoadConfig.getBucketCount(),
                            httpClientLoadConfig.getDelayForEachBucketInMs())
                            .subscribe(responses -> {
                                logTestStatus("Load", loadTestStartTime, responses);
                                try {
                                    metricsRegistry.report();
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                } finally {
                                    countDownLatch.countDown();
                                }
                            });
                });
        countDownLatch.await();
    }

    private static void logTestStatus(final String testType,
                                      final long executionStartTime,
                                      final List<String> responses) {
        logger.info("{} test completed...", testType);
        logger.info(
                "{} ::: Request count: {}, Time Taken: {} milliseconds",
                testType,
                responses.size(),
                elapsedTime(executionStartTime));
    }

    private Mono<List<String>> mkCalls(final int totalNumberOfCalls,
                                       final int bucketCount,
                                       final int delayForEachBucketInMs) {
        logger.info("Starting load for {} calls", totalNumberOfCalls);
        return Flux.range(1, totalNumberOfCalls / bucketCount)
                .delayElements(Duration.ofMillis(delayForEachBucketInMs))
                .flatMap(index -> Flux.
                        range(1, bucketCount)
                        .flatMap(idx -> fromCompletionStage(httpsClient.executeHttpsCallAsync())))
                .collectList()
                .doOnError(throwable -> {
                    logger.error(throwable.getMessage(), throwable);
                    countDownLatch.countDown();
                });
    }

    protected static BigDecimal elapsedTime(final long startTimeNS) {
        final double elapsedTime = (System.nanoTime() - startTimeNS) / 1.0e06;
        return BigDecimal.valueOf(elapsedTime).setScale(3, RoundingMode.FLOOR);
    }
}
