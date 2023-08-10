package com.paypal.jsse.benchmark.client.lnp;

import com.paypal.jsse.benchmark.client.HttpsClient;
import com.paypal.jsse.benchmark.client.metrics.MetricsRegistry;
import com.paypal.jsse.benchmark.config.JsseTestSysProps;
import com.paypal.jsse.benchmark.server.HttpsServer;

import java.util.Optional;

public class HttpsCallLoadExecutor {
    public HttpsCallLoadExecutor() {
        final boolean isStartEmbeddedServer = new JsseTestSysProps.HttpCallBenchmarkConfig().isStartEmbeddedServer();
        HttpsServer<?> server = null;
        if(isStartEmbeddedServer) {
            final String serverType = JsseTestSysProps.ServerType.serverTypePropVal();
            final Optional<JsseTestSysProps.ServerType> svrTypeOpt = JsseTestSysProps.ServerType.getServerType(serverType);
            server = svrTypeOpt
                    .map(svrType -> svrType.getServer().get())
                    .orElseThrow(() -> new RuntimeException("Invalid ServerType : " + serverType));
        }
        try {
            final MetricsRegistry metricsRegistry = new MetricsRegistry();

            final String clientType = JsseTestSysProps.ClientType.clientTypePropVal();
            final Optional<JsseTestSysProps.ClientType> clientTypeOpt = JsseTestSysProps.ClientType.getClientType(clientType);

            final HttpsClient<?> httpsClient = clientTypeOpt.
                    map(clType -> clType.getClient().apply(metricsRegistry))
                    .orElseThrow(() -> new RuntimeException("Invalid client type : " + clientType));

            final LoadSimulator loadSimulator = () -> httpsClient::executeHttpsCall;
            loadSimulator.execute();
            metricsRegistry.report();
        } finally {
            if(server != null) {
                server.stopServer();
            }
        }
    }
}
