package com.paypal.jsse.benchmark.client.sslr;

import com.paypal.infra.ssl.PayPalSSLSession;
import com.paypal.jsse.benchmark.client.HttpsClient;
import com.paypal.jsse.benchmark.client.ReactorNettyHttpsClient;
import com.paypal.jsse.benchmark.server.ReactorNettyHttpsServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.NettyPipeline;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLSession;

public class SslResumptionValidator {

    private static final Logger logger = LoggerFactory.getLogger(SslResumptionValidator.class);

    private static final String SESSION_CAPTURE_HANDLER = "SSL_SESSION_CAPTURE_HANDLER";

    private SSLSession sslSession;

    public SslResumptionValidator() {
        new ReactorNettyHttpsServer();
        final HttpsClient<HttpClient> reactorNettyClient = new ReactorNettyHttpsClient();
        final HttpClient httpsClient = reactorNettyClient.getClient();

        final String response = httpsClient
                .doOnChannelInit((observer, channel, address) -> {
                    final ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addBefore(NettyPipeline.SslReader,
                            SESSION_CAPTURE_HANDLER,
                            new SSLSessionCaptureHandler());
                }).get()
                .uri("/hi")
                .responseSingle((resp, bytes) -> bytes.asString())
                .doOnError(throwable -> logger.error(throwable.getMessage(), throwable))
                .block();
        if(StringUtils.isNotBlank(response)) {
            if (sslSession != null) {
                if (sslSession instanceof PayPalSSLSession) {
                    final PayPalSSLSession payPalSSLSession = (PayPalSSLSession) sslSession;
                    logSessionInfo(payPalSSLSession.isResumed(), payPalSSLSession);
                } else {
                    logSessionInfo(false, sslSession);
                }
            } else {
                logger.info("SSL session not found");
            }
        } else {
            logger.info("Invalid server response");
        }
    }

    private void logSessionInfo(final boolean isResumed,
                                final SSLSession sslSession) {
        logger.info("\n\nSession Resumed: {}, Cipher suite: {}, Protocol: {}\n\n",
                isResumed,
                sslSession.getCipherSuite(),
                sslSession.getProtocol());
    }

    private class SSLSessionCaptureHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof SslHandshakeCompletionEvent) {
                sslSession = ctx.channel().pipeline().get(SslHandler.class).engine().getSession();
            }
            ctx.pipeline().remove(SESSION_CAPTURE_HANDLER);
            super.userEventTriggered(ctx, evt);
        }
    }
}
