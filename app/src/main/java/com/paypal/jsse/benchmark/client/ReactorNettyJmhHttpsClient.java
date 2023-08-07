package com.paypal.jsse.benchmark.client;

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

import static com.paypal.jsse.benchmark.SSLContextFactory.sslContext;

public class ReactorNettyJmhHttpsClient extends JmhHttpsClient<HttpClient> {
    private static final Logger logger = LoggerFactory.getLogger(ReactorNettyJmhHttpsClient.class);

    @Override
    public HttpClient createHttpsClient(String host, int port) {
        return HttpClient.create(ConnectionProvider.newConnection())
                .keepAlive(false)
                .secure(SslProvider.builder().sslContext(nettySslContext(sslContext(true))).build())
                .baseUrl(String.format("https://%s:%s", host, port));
    }

    @Override
    public void executeHttpsCall(final Metrics metrics) {
        final String response = client
                .doOnChannelInit((observer, channel, address) -> {
                    final ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addBefore(NettyPipeline.SslHandler,
                            "SslHandshakeTimeRecorder",
                            new SslHandshakeTimeRecorder(metrics));
                })
                .get()
                .uri("/hi")
                .responseSingle((resp, bytes) -> bytes.asString())
                .doOnError(throwable -> logger.debug(throwable.getMessage(), throwable))
                .onErrorReturn("failed")
                .block();
        if(logger.isDebugEnabled()) {
            logger.debug("Response: " + response);
        }
    }

    private class SslHandshakeTimeRecorder extends ChannelInboundHandlerAdapter {

        private final Metrics metrics;

        public SslHandshakeTimeRecorder(Metrics metrics) {
            this.metrics = metrics;
        }

        @Override
        public void channelActive(final ChannelHandlerContext ctx) {
            final long tlsHandshakeTimeStart = System.nanoTime();
            ctx.pipeline().get(SslHandler.class)
                    .handshakeFuture()
                    .addListener(f -> {
                        ctx.pipeline().remove(this);
                        final BigDecimal elapsedTime = elapsedTime(tlsHandshakeTimeStart);
                        metrics.getSslMetrics().addSSLHandshakeTime(elapsedTime.doubleValue());
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
