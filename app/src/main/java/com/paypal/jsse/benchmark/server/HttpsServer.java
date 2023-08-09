package com.paypal.jsse.benchmark.server;

import com.paypal.jsse.benchmark.config.JsseTestSysProps;
import com.paypal.jsse.test.ssl.SSLContextFactory;
import com.paypal.jsse.test.ssl.KMSSLContextFactory;

import javax.net.ssl.SSLContext;

public abstract class HttpsServer<S> {

    private final S server;

    public HttpsServer() {
        final JsseTestSysProps.ServerConfig serverConfig = new JsseTestSysProps.ServerConfig();
        final String host = serverConfig.getHost();
        final int port = serverConfig.getPort();
        server = createServer(host, port, createServerSslContext());
    }

    public SSLContext createServerSslContext() {
        final SSLContextFactory sslContextFactory = new KMSSLContextFactory();
        return sslContextFactory.sslContext(true);
    }

    abstract S createServer(String host, int port, SSLContext sslContext);
    abstract void listen();
    public abstract void stopServer();

    public S getServer() {
        return server;
    }
}
