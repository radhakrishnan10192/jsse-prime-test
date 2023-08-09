package com.paypal.jsse.benchmark.client.lnp;

import com.paypal.jsse.benchmark.config.JsseTestSysProps;
import com.paypal.jsse.benchmark.server.HttpsServer;

import java.util.Optional;

public class HttpsClientLoadExecutor {

    public HttpsClientLoadExecutor() {
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
            final String clientType = JsseTestSysProps.ClientType.clientTypePropVal();
            final Optional<JsseTestSysProps.ClientType> clientTypeOpt = JsseTestSysProps.ClientType.getClientType(clientType);
            final HttpsClientLoadSim<?> loadSim = clientTypeOpt
                    .map(clType -> clType.getLoadSim().get())
                    .orElseThrow(() -> new RuntimeException("Invalid client type : " + clientType));
            loadSim.executeClientLoadTest();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if(server != null) {
                server.stopServer();
            }
        }
    }

}
