package com.paypal.jsse.tester.tests.jmh;

import com.paypal.jsse.tester.tests.TestExecutor;
import com.paypal.jsse.tester.client.HttpsClient;
import com.paypal.jsse.tester.server.HttpsServer;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class HttpsCallBenchmark implements TestExecutor {
    private HttpsClient<?> client;
    private HttpsServer<?> server;
    private JMHMetricsRegistry metricsRegistry;

    @Setup
    public void setup(final JMHMetricsRegistry metricsRegistry) {
        this.server = initializeTestServer().orElse(null);
        this.metricsRegistry = metricsRegistry;
        client = initializeTestClient(this.metricsRegistry, false);
    }

    @Benchmark
    public void execute() {
        client.executeHttpsCall();
    }

    @TearDown
    public void cleanup() {
        if(this.server != null) {
            this.server.stopServer();
        }
        this.metricsRegistry.report();
    }

}
