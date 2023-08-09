package com.paypal.jsse.benchmark.client;

import com.paypal.jsse.benchmark.client.metrics.Metric;
import com.paypal.jsse.benchmark.client.metrics.MetricsRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.NettyPipeline;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.SslProvider;

import javax.net.ssl.SSLContext;
import java.math.BigDecimal;
import java.util.concurrent.CompletionStage;

public class ReactorNettyHttpsClient extends HttpsClient<HttpClient> {
    private static final Logger logger = LoggerFactory.getLogger(ReactorNettyHttpsClient.class);

    public ReactorNettyHttpsClient() {
        super();
    }

    public ReactorNettyHttpsClient(MetricsRegistry metricsRegistry) {
        super(metricsRegistry);
    }

    public ReactorNettyHttpsClient(String host, int port, MetricsRegistry registry) {
        super(host, port, registry);
    }

    @Override
    public HttpClient createHttpsClient(final String host,
                                        final int port,
                                        final SSLContext sslContext) {
        return HttpClient.create(ConnectionProvider.newConnection())
                .keepAlive(false)
                .secure(SslProvider.builder().sslContext(nettySslContext(sslContext)).build())
                .baseUrl(String.format("https://%s:%s", host, port));
    }

    @Override
    protected HttpClient createHttpsClient(final String host,
                                           final int port,
                                           final SSLContext sslContext,
                                           final MetricsRegistry metricsRegistry) {
        final Metric.SSLMetric sslMetrics = new Metric.SSLMetric();
        metricsRegistry.addMetric(sslMetrics);
        return createHttpsClient(host, port, sslContext)
                .doOnChannelInit((observer, channel, address) -> {
                    final ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addBefore(NettyPipeline.SslHandler,
                            "SslHandshakeTimeRecorder",
                            new SslHandshakeTimeRecorder(sslMetrics));
                });
    }

    @Override
    public String executeHttpsCall(final String path) {
        final String response = client
                .get()
                .uri(path)
                .responseSingle((resp, bytes) -> bytes.asString())
                .doOnError(throwable -> logger.debug(throwable.getMessage(), throwable))
                .block();
        if(logger.isDebugEnabled()) {
            logger.debug("Response: " + response);
        }
        return response;
    }

    @Override
    public CompletionStage<String> executeHttpsCallAsync(String path) {
        return client
                .get()
                .uri(path)
                .responseSingle((resp, bytes) -> bytes.asString())
                .doOnError(throwable -> logger.debug(throwable.getMessage(), throwable))
                .onErrorReturn("failed")
                .toFuture();
    }

    private static class SslHandshakeTimeRecorder extends ChannelInboundHandlerAdapter {

        private final Metric.SSLMetric sslMetrics;

        public SslHandshakeTimeRecorder(final Metric.SSLMetric sslMetrics) {
            this.sslMetrics = sslMetrics;
        }

        @Override
        public void channelActive(final ChannelHandlerContext ctx) {
            final long tlsHandshakeTimeStart = System.nanoTime();
            ctx.pipeline().get(SslHandler.class)
                    .handshakeFuture()
                    .addListener(f -> {
                        ctx.pipeline().remove(this);
                        final BigDecimal elapsedTime = elapsedTime(tlsHandshakeTimeStart);
                        sslMetrics.addMetric(elapsedTime.doubleValue());
                    });
            ctx.fireChannelActive();
        }
    }

    private SslContext nettySslContext(final SSLContext sslContext) {
        return new JdkSslContext(
                sslContext,
                true,
                null,
                IdentityCipherSuiteFilter.INSTANCE,
                null,
                ClientAuth.NONE, // this value is required for the constructor but is not used for clients
                null,
                false);
    }
}
