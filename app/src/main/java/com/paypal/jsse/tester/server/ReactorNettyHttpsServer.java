package com.paypal.jsse.tester.server;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.tcp.SslProvider;

import javax.net.ssl.SSLContext;
import java.time.Duration;

public class ReactorNettyHttpsServer extends HttpsServer<DisposableServer> {

    private static final Logger logger = LoggerFactory.getLogger(ReactorNettyHttpsServer.class);

    @Override
    public DisposableServer createServer(final String host,
                                         final int port,
                                         final SSLContext sslContext) {
        final DisposableServer disposableServer = HttpServer
                .create()
                .port(port)
                .secure(SslProvider.builder().sslContext(nettySslContext(sslContext)).build())
                .route(routes -> routes
                        .get("/hi",
                                (req, res) -> res.sendString(Mono.just("Hello world").delayElement(Duration.ofMillis(100)))))
                .bindNow();
        logger.info("Started reactor-netty based HTTPS server listening on port " + disposableServer.port());
        return  disposableServer;
    }

    @Override
    public void listen() {
        getServer().onDispose().block();
    }

    private SslContext nettySslContext(final SSLContext sslContext) {
        return new JdkSslContext(
                sslContext,
                false,
                null,
                IdentityCipherSuiteFilter.INSTANCE,
                null,
                ClientAuth.NONE, // this value is required for the constructor but is not used for clients
                null,
                false);
    }

    @Override
    public void stopServer() {
        this.getServer().disposeNow();
    }
}
