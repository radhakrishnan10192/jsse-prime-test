package com.paypal.jsse.benchmark.client;

import com.paypal.jsse.benchmark.client.metrics.MetricsRegistry;
import com.paypal.jsse.benchmark.config.JsseTestSysProps;
import com.paypal.jsse.benchmark.config.SslConfig;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class HttpsClient<C> implements SslConfig  {

    protected final C client;

    protected final String baseUrl;

    public HttpsClient() {
        this(null);
    }

    public HttpsClient(final MetricsRegistry metricsRegistry) {
        this(new JsseTestSysProps.ServerConfig(), metricsRegistry);
    }

    public HttpsClient(final JsseTestSysProps.ServerConfig serverConfig, final MetricsRegistry metricsRegistry) {
        this(serverConfig.getHost(), serverConfig.getPort(), metricsRegistry);
    }

    public HttpsClient(final String host,
                       final int port,
                       final MetricsRegistry metricsRegistry) {
        if(metricsRegistry != null) {
            this.client = createHttpsClient(host, port, createSslContext(true), metricsRegistry);
        } else {
            this.client = createHttpsClient(host, port, createSslContext(true));
        }
        this.baseUrl = createBaseUrl(host, port);
        logger.info("{} HTTPS client initialized...", clientName());
    }

    private String createBaseUrl(final String host, final int port) {
        return String.format("https://%s:%s", host, port);
    }

    abstract protected C createHttpsClient(final String host,
                                           final int port,
                                           final SSLContext sslContext,
                                           final MetricsRegistry metricsRegistry);

    protected abstract C createHttpsClient(final String host,
                                           final int port,
                                           final SSLContext sslContext);

    public String executeHttpsCall() {
        return this.executeHttpsCall("/hi");
    }

    public abstract String executeHttpsCall(final String path);

    abstract String clientName();

    public C getClient() {
        return client;
    }

    protected static BigDecimal elapsedTime(final long startTimeNS) {
        final double elapsedTime = (System.nanoTime() - startTimeNS) / 1.0e06;
        return BigDecimal.valueOf(elapsedTime).setScale(3, RoundingMode.FLOOR);
    }

    protected static void logSessionInfo(final boolean isResumed,
                                final SSLSession sslSession) {
        logger.info("\n\nSession Resumed: {}, Cipher suite: {}, Protocol: {}\n\n",
                isResumed,
                sslSession.getCipherSuite(),
                sslSession.getProtocol());
    }

}
