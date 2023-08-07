package com.paypal.jsse.benchmark.server;

import com.paypal.jsse.benchmark.SysProps;

public abstract class HttpsServer<S> {

    private final S server;

    public HttpsServer() {
        final SysProps.ServerConfig serverConfig = new SysProps.ServerConfig();
        final String host = serverConfig.getHost();
        final int port = serverConfig.getPort();
        server = createServer(host, port);
    }

    abstract S createServer(String host, int port);
    abstract void listen();
    public abstract void stopServer();

    public S getServer() {
        return server;
    }
}
