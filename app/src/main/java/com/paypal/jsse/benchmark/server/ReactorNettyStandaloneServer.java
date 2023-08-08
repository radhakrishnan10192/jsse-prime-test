package com.paypal.jsse.benchmark.server;

import com.paypal.jsse.benchmark.SysProps;

import java.util.Optional;

public class ReactorNettyStandaloneServer {
    public ReactorNettyStandaloneServer() {
        final String serverType = SysProps.ServerType.serverTypePropVal();
        final Optional<SysProps.ServerType> serverTypeOptional = SysProps.ServerType.getServerType(serverType);
        final HttpsServer<?> server  = serverTypeOptional
                .map(svrType -> svrType.getServer().get())
                .orElseThrow(() -> new RuntimeException("Invalid ServerType : " + serverType));
        server.listen();
    }
}
