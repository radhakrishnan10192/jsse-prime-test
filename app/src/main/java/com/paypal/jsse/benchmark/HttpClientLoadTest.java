package com.paypal.jsse.benchmark;

import com.paypal.jsse.benchmark.client.HttpsClient;
import com.paypal.jsse.benchmark.client.ReactorNettyHttpClient;
import com.paypal.jsse.benchmark.server.ReactorNettyHttpsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.http.client.HttpClient;

public class HttpClientLoadTest {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientLoadTest.class);

    public HttpClientLoadTest() {
        new ReactorNettyHttpsServer();
        final HttpsClient<HttpClient> reactorNettyClient = new ReactorNettyHttpClient();
        final HttpClient httpsClient = reactorNettyClient.getClient();

        final String response = httpsClient.get()
                .uri("/hi")
                .responseSingle((resp, bytes) -> bytes.asString())
                .doOnError(throwable -> logger.error(throwable.getMessage(), throwable))
                .block();
    }
}
