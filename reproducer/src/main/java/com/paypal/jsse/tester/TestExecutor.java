package com.paypal.jsse.tester;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.openjdk.jmh.runner.options.TimeValue.milliseconds;

public class TestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(TestExecutor.class);

    public static void main(String[] args) {
        try {
            final ChainedOptionsBuilder optionsBuilder = new OptionsBuilder()
                    .include(Benchmark.class.getSimpleName())
                    .forks(readProperty("jmh.forks", Integer.class, 1))
                    .threads(readProperty("jmh.threads", Integer.class, Runtime.getRuntime().availableProcessors()))
                    .warmupIterations(readProperty("jmh.warmup.iterations", Integer.class, 1))
                    .warmupTime(milliseconds(readProperty("jmh.warmup.time.ms", Integer.class, 5000)))
                    .measurementIterations(readProperty("jmh.measurement.iterations", Integer.class, 3))
                    .measurementTime(milliseconds(readProperty("jmh.measurement.time.ms", Integer.class, 60000)))
                    .mode(Mode.valueOf("AverageTime"))
                    .resultFormat(ResultFormatType.JSON)
                    .result(String.format("benchmark-%s-%s.json",
                            TestExecutor.class.getSimpleName(),
                            System.getProperty("java.version")))
                    .timeUnit(TimeUnit.valueOf("MILLISECONDS"));

            final Options opt = optionsBuilder.build();
            new Runner(opt).run();
        } catch (RunnerException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("Execution done");
    }

    public static <T> T readProperty(final String propertyName,
                              final Class<T> targetType,
                              final T defaultValue) {
        final String propertyValue = System.getProperty(propertyName, String.valueOf(defaultValue));
        return convertToSpecificType(propertyValue, targetType);
    }

    private static <T> T convertToSpecificType(final String propertyValue,
                                        final Class<T> targetType) {
        if (targetType == null || targetType.equals(String.class)) {
            return (T) propertyValue;
        } else if (targetType.equals(Boolean.class)) {
            return (T) Boolean.valueOf(propertyValue);
        } else if (targetType.equals(Integer.class)) {
            return (T) Integer.valueOf(propertyValue);
        } else if (targetType.equals(Double.class)) {
            return (T) Double.valueOf(propertyValue);
        }
        throw new RuntimeException("Unsupported target type: " + targetType);
    }

    private static void executeTests(){
        try {
            final long executionTime = readProperty("executionTime", Integer.class, 5) * 60000;
            int threads = readProperty("threads", Integer.class, 5);
            Executor executor = Executors.newFixedThreadPool(threads);

            final Benchmark httpsCallBenchmark = new Benchmark();
            httpsCallBenchmark.setup();

            CountDownLatch countDownLatch = new CountDownLatch(1);

            IntStream.range(0, threads)
                    .forEach(e -> CompletableFuture.runAsync(() ->
                    {
                        long startTime = System.currentTimeMillis();
                        while ((System.currentTimeMillis() - startTime) < executionTime){
                            try {
                                httpsCallBenchmark.execute();
                            }catch (Exception ex){
                                logger.error("Error", ex);
                            }
                        }
                    },executor));

            countDownLatch.await(executionTime + 10000, TimeUnit.MILLISECONDS);

            httpsCallBenchmark.cleanup();
        }catch (Exception exception){
            logger.error("Error", exception);
        }
    }
}
