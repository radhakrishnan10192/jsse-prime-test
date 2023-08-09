package com.paypal.jsse.benchmark.server;

import com.paypal.jsse.benchmark.config.JsseTestSysProps;

import java.util.Optional;

public class ReactorNettyStandaloneServer {
    public ReactorNettyStandaloneServer() {
        final String serverType = JsseTestSysProps.ServerType.serverTypePropVal();
        final Optional<JsseTestSysProps.ServerType> serverTypeOptional = JsseTestSysProps.ServerType.getServerType(serverType);
        final HttpsServer<?> server  = serverTypeOptional
                .map(svrType -> svrType.getServer().get())
                .orElseThrow(() -> new RuntimeException("Invalid ServerType : " + serverType));
        server.listen();
    }
}
