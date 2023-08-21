package com.paypal.jsse.tester.tests;

import com.paypal.jsse.tester.client.HttpsClient;
import com.paypal.jsse.tester.client.metrics.MetricsRegistry;
import com.paypal.jsse.tester.config.JsseTestSysProps;
import com.paypal.jsse.tester.server.HttpsServer;

import java.util.Optional;

public interface TestExecutor {

    default Optional<HttpsServer<?>> initializeTestServer() {
        final boolean isStartEmbeddedServer = new JsseTestSysProps.ServerConfig().isStartEmbeddedServer();
        if(isStartEmbeddedServer) {
            final String serverType = JsseTestSysProps.ServerType.serverTypePropVal();
            final Optional<JsseTestSysProps.ServerType> svrTypeOpt = JsseTestSysProps.ServerType.getServerType(serverType);
            return Optional.of(svrTypeOpt
                    .map(svrType -> svrType.getServer().get())
                    .orElseThrow(() -> new RuntimeException("Invalid ServerType : " + serverType)));
        }
        return Optional.empty();
    }

    default HttpsClient<?> initializeTestClient(final MetricsRegistry metricsRegistry,
                                                final boolean resumptionTest) {
        final String clientType = JsseTestSysProps.ClientType.clientTypePropVal();
        final Optional<JsseTestSysProps.ClientType> clientTypeOpt = JsseTestSysProps.ClientType.getClientType(clientType);

        return clientTypeOpt.
                map(clType -> clType.getClient().apply(metricsRegistry, resumptionTest))
                .orElseThrow(() -> new RuntimeException("Invalid client type : " + clientType));
    }
}
