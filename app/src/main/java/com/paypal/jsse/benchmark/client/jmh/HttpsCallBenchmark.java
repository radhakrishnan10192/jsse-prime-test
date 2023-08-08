package com.paypal.jsse.benchmark.client.jmh;

import com.paypal.jsse.benchmark.SysProps;
import com.paypal.jsse.benchmark.client.HttpsClient;
import com.paypal.jsse.benchmark.client.metrics.JMHMetricsRegistry;
import com.paypal.jsse.benchmark.server.HttpsServer;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import java.util.Optional;

@State(Scope.Benchmark)
public class HttpsCallBenchmark {

    private HttpsClient<?> client;
    private HttpsServer<?> server;
    private JMHMetricsRegistry metricsRegistry;

    @Setup
    public void setup(final JMHMetricsRegistry metricsRegistry) {
        final boolean isStartEmbeddedServer = new SysProps.HttpCallBenchmarkConfig().isStartEmbeddedServer();
        if(isStartEmbeddedServer) {
            final String serverType = SysProps.ServerType.serverTypePropVal();
            final Optional<SysProps.ServerType> svrTypeOpt = SysProps.ServerType.getServerType(serverType);
            server = svrTypeOpt
                    .map(svrType -> svrType.getServer().get())
                    .orElseThrow(() -> new RuntimeException("Invalid ServerType : " + serverType));
        } else {
            server = null;
        }

        this.metricsRegistry = metricsRegistry;
        final String clientType = SysProps.ClientType.clientTypePropVal();
        final Optional<SysProps.ClientType> clientTypeOpt = SysProps.ClientType.getClientType(clientType);
        client = clientTypeOpt
                .map(clType -> clType.getClient().apply(metricsRegistry))
                .orElseThrow(() -> new RuntimeException("Invalid client type : " + clientType));
    }

    @Benchmark
    public void execute() {
        client.executeHttpsCall();
    }

    @TearDown
    public void cleanup() {
        if(server != null) {
            server.stopServer();
        }
        this.metricsRegistry.report();
    }

}
