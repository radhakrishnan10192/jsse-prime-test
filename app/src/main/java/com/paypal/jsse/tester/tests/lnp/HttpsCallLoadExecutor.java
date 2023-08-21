package com.paypal.jsse.tester.tests.lnp;

import com.paypal.jsse.tester.tests.TestExecutor;
import com.paypal.jsse.tester.client.HttpsClient;
import com.paypal.jsse.tester.client.metrics.MetricsRegistry;
import com.paypal.jsse.tester.server.HttpsServer;

import java.util.Optional;

public class HttpsCallLoadExecutor implements TestExecutor {
    public HttpsCallLoadExecutor() {
        final Optional<HttpsServer<?>> httpsServer = initializeTestServer();
        try {
            final MetricsRegistry metricsRegistry = new MetricsRegistry();
            final HttpsClient<?> httpsClient = initializeTestClient(metricsRegistry, false);
            final LoadSimulator loadSimulator = () -> httpsClient::executeHttpsCall;
            loadSimulator.execute(LoadSimulator.TestType.Warmup);
            metricsRegistry.resetAll();
            loadSimulator.execute(LoadSimulator.TestType.Load);
            metricsRegistry.report();
        } finally {
            httpsServer.ifPresent(HttpsServer::stopServer);
        }
    }
}
