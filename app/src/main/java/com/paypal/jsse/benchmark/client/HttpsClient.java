package com.paypal.jsse.benchmark.client;

import com.paypal.jsse.benchmark.SysProps;
import com.paypal.jsse.benchmark.client.metrics.MetricsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.util.Metrics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CompletionStage;

public abstract class HttpsClient<C> {

    private static final Logger logger = LoggerFactory.getLogger(HttpsClient.class);

    protected final C client;

    public HttpsClient() {
        this(null);
    }

    public HttpsClient(final String host,
                       final int port,
                       final MetricsRegistry registry) {
        this.client = createHttpsClient(host, port, registry);
    }

    public HttpsClient(final MetricsRegistry metricsRegistry) {
        final SysProps.ServerConfig serverConfig = new SysProps.ServerConfig();
        final String host = serverConfig.getHost();
        final int port = serverConfig.getPort();
        if(metricsRegistry != null) {
            this.client = createHttpsClient(host, port, metricsRegistry);
        } else {
            this.client = createHttpsClient(host, port);
        }
    }

    abstract protected C createHttpsClient(final String host, final int port, final MetricsRegistry metricsRegistry);

    abstract C createHttpsClient(final String host, final int port);

    public String executeHttpsCall() {
        return this.executeHttpsCall("/hi");
    }

    public CompletionStage<String> executeHttpsCallAsync() {
        return this.executeHttpsCallAsync("/hi");
    }

    public abstract String executeHttpsCall(final String path);

    public abstract CompletionStage<String> executeHttpsCallAsync(final String path);

    public C getClient() {
        return client;
    }

    protected static BigDecimal elapsedTime(final long startTimeNS) {
        final double elapsedTime = (System.nanoTime() - startTimeNS) / 1.0e06;
        return BigDecimal.valueOf(elapsedTime).setScale(3, RoundingMode.FLOOR);
    }

}
