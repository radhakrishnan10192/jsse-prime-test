package com.paypal.jsse.benchmark.client;

import com.paypal.jsse.benchmark.SysProps;

public abstract class HttpsClient<C> {

    private final C client;

    public HttpsClient() {
        final SysProps.ServerConfig serverConfig = new SysProps.ServerConfig();
        final String host = serverConfig.getHost();
        final int port = serverConfig.getPort();
        this.client = createHttpsClient(host, port);
    }

    abstract C createHttpsClient(final String host, final int port);

    public C getClient() {
        return client;
    }
}
