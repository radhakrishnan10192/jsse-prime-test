package com.paypal.jsse.benchmark.client.lnp;

import com.paypal.jsse.benchmark.client.HttpsClient;
import com.paypal.jsse.benchmark.client.ReactorNettyHttpsClient;
import com.paypal.jsse.benchmark.client.metrics.MetricsRegistry;
import reactor.netty.http.client.HttpClient;

public class ReactorNettyHttpsClientLoadSim extends HttpsClientLoadSim<HttpClient> {
    @Override
    protected HttpsClient<HttpClient> createHttpsClient(final String host,
                                                        final int port,
                                                        final MetricsRegistry metricsRegistry) {
        return new ReactorNettyHttpsClient(host, port, metricsRegistry);
    }
}
