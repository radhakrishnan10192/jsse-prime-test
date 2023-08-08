package com.paypal.jsse.benchmark.client.lnp;

import com.paypal.jsse.benchmark.SysProps;
import com.paypal.jsse.benchmark.server.HttpsServer;

import java.util.Optional;

public class HttpsClientLoadExecutor {

    public HttpsClientLoadExecutor() {
        final boolean isStartEmbeddedServer = new SysProps.HttpCallBenchmarkConfig().isStartEmbeddedServer();
        HttpsServer<?> server = null;
        if(isStartEmbeddedServer) {
            final String serverType = SysProps.ServerType.serverTypePropVal();
            final Optional<SysProps.ServerType> svrTypeOpt = SysProps.ServerType.getServerType(serverType);
            server = svrTypeOpt
                    .map(svrType -> svrType.getServer().get())
                    .orElseThrow(() -> new RuntimeException("Invalid ServerType : " + serverType));
        }
        try {
            final String clientType = SysProps.ClientType.clientTypePropVal();
            final Optional<SysProps.ClientType> clientTypeOpt = SysProps.ClientType.getClientType(clientType);
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
