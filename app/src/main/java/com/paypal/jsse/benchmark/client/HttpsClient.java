package com.paypal.jsse.benchmark.client;

import com.paypal.jsse.benchmark.client.metrics.MetricsRegistry;
import com.paypal.jsse.benchmark.config.JsseTestSysProps;
import com.paypal.jsse.test.ssl.SSLContextFactory;
import com.paypal.jsse.test.ssl.KMSSLContextFactory;

import javax.net.ssl.SSLContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CompletionStage;

public abstract class HttpsClient<C> {

    protected final C client;

    public HttpsClient() {
        this(null);
    }

    public HttpsClient(final String host,
                       final int port,
                       final MetricsRegistry registry) {
        this.client = createHttpsClient(host, port, createClientSslContext(), registry);
    }

    public HttpsClient(final MetricsRegistry metricsRegistry) {
        final JsseTestSysProps.ServerConfig serverConfig = new JsseTestSysProps.ServerConfig();
        final String host = serverConfig.getHost();
        final int port = serverConfig.getPort();
        if(metricsRegistry != null) {
            this.client = createHttpsClient(host, port, createClientSslContext(), metricsRegistry);
        } else {
            this.client = createHttpsClient(host, port, createClientSslContext());
        }
    }

    public SSLContext createClientSslContext() {
        final SSLContextFactory sslContextFactory = new KMSSLContextFactory();
        return sslContextFactory.sslContext(true);
    }

    abstract protected C createHttpsClient(final String host,
                                           final int port,
                                           final SSLContext sslContext,
                                           final MetricsRegistry metricsRegistry);

    abstract C createHttpsClient(final String host,
                                 final int port,
                                 final SSLContext sslContext);

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
