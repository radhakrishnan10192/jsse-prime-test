package com.paypal.jsse.benchmark.config;

import com.paypal.jsse.test.ssl.SSLContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;

public interface SslConfig {

    Logger logger = LoggerFactory.getLogger(SslConfig.class);

    default SSLContext createSslContext(final boolean forClient) {
        try {
            final Class<?> kmSSLContextFactoryClass = Class.forName("com.paypal.jsse.test.ssl.KMSSLContextFactory");
            final SSLContextFactory sslContextFactory = (SSLContextFactory) kmSSLContextFactoryClass.getDeclaredConstructor().newInstance();
            return sslContextFactory.sslContext(forClient);
        } catch (ClassNotFoundException e) {
            // Ignore and proceed to check the other implementation exists in classpath.
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            final Class<?> mockSSLContextFactoryClass = Class.forName("com.paypal.jsse.test.ssl.MockSSLContextFactory");
            final SSLContextFactory sslContextFactory = (SSLContextFactory) mockSSLContextFactoryClass.getDeclaredConstructor().newInstance();
            return sslContextFactory.sslContext(forClient);
        } catch (ClassNotFoundException e) {
            // Ignore and proceed to check the other implementation exists in classpath.
            logger.error("Missing both KMSSLContextFactory and MockSSLContextFactory." +
                    " Using any one profile(-Pinfra-jsse or -Pmock-jsse) when executing maven command");
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
