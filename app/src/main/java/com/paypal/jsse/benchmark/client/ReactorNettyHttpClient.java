package com.paypal.jsse.benchmark.client;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
import reactor.netty.DisposableServer;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.SslProvider;

import javax.net.ssl.SSLContext;

import static com.paypal.jsse.benchmark.SSLContextFactory.sslContext;

public class ReactorNettyHttpClient extends HttpsClient<HttpClient> {
    @Override
    HttpClient createHttpsClient(final String host,
                                 final int port) {
        return HttpClient.create(ConnectionProvider.newConnection())
                .keepAlive(false)
                .secure(SslProvider.builder().sslContext(nettySslContext(sslContext(true))).build())
                .baseUrl(String.format("https://%s:%s", host, port));
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
