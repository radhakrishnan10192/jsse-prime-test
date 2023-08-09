package com.paypal.jsse.test.ssl;

import static com.paypal.jsse.test.config.SysPropsReader.readProperty;

public class SSLConfig {
    private final boolean paypalJsseEnabled;

    public SSLConfig() {
        paypalJsseEnabled = readProperty("paypal.jsse.enable", Boolean.class, true);
    }

    public boolean isPaypalJsseEnabled() {
        return paypalJsseEnabled;
    }
}
