package com.paypal.jsse.tester.client;

import com.paypal.infra.ssl.PayPalSSLSession;
import com.paypal.jsse.tester.client.metrics.Metric;
import com.paypal.jsse.tester.client.metrics.MetricsRegistry;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.NettyPipeline;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.SslProvider;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.math.BigDecimal;

public class ReactorNettyHttpsClient extends HttpsClient<HttpClient> {
    private static final Logger logger = LoggerFactory.getLogger(ReactorNettyHttpsClient.class);

    private static final String SESSION_LOGGER_HANDLER = "SSL_SESSION_LOGGER_HANDLER";

    public ReactorNettyHttpsClient(final Boolean resumptionTest) {
        super(resumptionTest);
    }

    public ReactorNettyHttpsClient(final MetricsRegistry metricsRegistry,
                                   final Boolean resumptionTest) {
        super(metricsRegistry, resumptionTest);
    }

    @Override
    public HttpClient createHttpsClient(final String host,
                                        final int port,
                                        final SSLContext sslContext) {
        HttpClient httpClient = HttpClient.create(ConnectionProvider.newConnection())
                .keepAlive(false)
                .secure(SslProvider.builder().sslContext(nettySslContext(sslContext)).build())
                .baseUrl(String.format("https://%s:%s", host, port));
        final Metric.SSLMetric sslMetrics;
        if(metricsRegistry != null) {
            sslMetrics = new Metric.SSLMetric();
            metricsRegistry.addMetric(sslMetrics);
        } else {
            sslMetrics = null;
        }
        if(resumptionTest || sslMetrics != null) {
            httpClient = httpClient.doOnChannelInit((observer, channel, address) -> {
                final ChannelPipeline pipeline = channel.pipeline();
                pipeline.addBefore(NettyPipeline.SslHandler,
                        "SslHandshakeTimeRecorder",
                        new SslHandshakeTimeRecorder(sslMetrics, resumptionTest));
            });
        }
        return httpClient;
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
    String clientName() {
        return "Reactor Netty";
    }

    private static class SslHandshakeTimeRecorder extends ChannelInboundHandlerAdapter {

        private final Metric.SSLMetric sslMetrics;
        private final boolean resumptionTest;

        public SslHandshakeTimeRecorder(final Metric.SSLMetric sslMetrics,
                                        final boolean resumptionTest) {
            this.sslMetrics = sslMetrics;
            this.resumptionTest = resumptionTest;
        }

        @Override
        public void channelActive(final ChannelHandlerContext ctx) {
            final long tlsHandshakeTimeStart = System.nanoTime();
            final Future<Channel> handshakeFuture = ctx.pipeline().get(SslHandler.class)
                    .handshakeFuture();
            handshakeFuture.addListener(f -> {
                ctx.pipeline().remove(this);
                if(sslMetrics != null) {
                    final BigDecimal elapsedTime = elapsedTime(tlsHandshakeTimeStart);
                    sslMetrics.addMetric(elapsedTime.doubleValue());
                }
                if(resumptionTest) {
                    final SSLSession sslSession = ctx.pipeline().get(SslHandler.class).engine().getSession();
                    if (sslSession instanceof PayPalSSLSession) {
                        logSessionInfo(((PayPalSSLSession) sslSession).isResumed(), sslSession);
                    } else {
                        logSessionInfo(false, sslSession);
                    }
                }
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
