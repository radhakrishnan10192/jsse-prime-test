package com.paypal.jsse.benchmark.server;

import com.paypal.jsse.benchmark.config.JsseTestSysProps;
import com.paypal.jsse.benchmark.config.SslConfig;

import javax.net.ssl.SSLContext;

public abstract class HttpsServer<S> implements SslConfig {

    private final S server;

    public HttpsServer() {
        final JsseTestSysProps.ServerConfig serverConfig = new JsseTestSysProps.ServerConfig();
        final String host = serverConfig.getHost();
        final int port = serverConfig.getPort();
        server = createServer(host, port, createSslContext(false));
    }

    abstract S createServer(String host, int port, SSLContext sslContext);
    abstract void listen();
    public abstract void stopServer();

    public S getServer() {
        return server;
    }
}
