package com.paypal.jsse.test.ssl;

import javax.net.ssl.SSLContext;

public interface SSLContextFactory {

    SSLContext sslContext(final boolean forClient);
}
