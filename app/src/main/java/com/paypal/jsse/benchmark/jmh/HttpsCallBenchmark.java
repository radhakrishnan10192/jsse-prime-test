package com.paypal.jsse.benchmark.jmh;

import com.paypal.jsse.benchmark.SysProps;
import com.paypal.jsse.benchmark.client.JmhHttpsClient;
import com.paypal.jsse.benchmark.server.HttpsServer;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import java.util.Optional;

@State(Scope.Benchmark)
public class HttpsCallBenchmark {

    private final JmhHttpsClient<?> client;
    private final HttpsServer<?> server;

    public HttpsCallBenchmark() {
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

        final String clientType = SysProps.ClientType.clientTypePropVal();
        final Optional<SysProps.ClientType> clientTypeOpt = SysProps.ClientType.getClientType(clientType);
        client = clientTypeOpt
                .map(clType -> clType.getClient().get())
                .orElseThrow(() -> new RuntimeException("Invalid client type : " + clientType));
    }

    public HttpsCallBenchmark(final JmhHttpsClient<?> client, HttpsServer<?> server) {
        this.client = client;
        this.server = server;
    }

    @Benchmark
    public void execute(final JmhHttpsClient.Metrics metrics) {
        client.executeHttpsCall(metrics);
    }

    @TearDown
    public void cleanup() {
        if(server != null) {
            server.stopServer();
        }
    }

}
