package com.paypal.jsse.tester;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

public class SSLContextFactory {
    private static final Logger logger = LoggerFactory.getLogger(SSLContextFactory.class);

    public static SslContext nettySslContext(boolean isClient) {
        return new JdkSslContext(
                sslContext(isClient),
                isClient,
                null,
                IdentityCipherSuiteFilter.INSTANCE,
                null,
                ClientAuth.NONE, // this value is required for the constructor but is not used for clients
                null,
                false);
    }
    private static SSLContext sslContext(boolean forClient) {
        try {
            // Initialize KeyManagerFactory with the test keystore
            final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            final KeyStore ks = KeyStore.getInstance("JCEKS");
            final String password = "123456789";
            final String protectedFilePath = Objects.requireNonNull(SSLContextFactory.class.getClassLoader().getResource("mock-protected")).getPath();
            ks.load(Files.newInputStream(new File(protectedFilePath + "/azultest.jks").toPath()), password.toCharArray());
            keyManagerFactory.init(ks, password.toCharArray());

            final KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

            /// 03192013 - use ProtectedTrustManager instead of system default trust manager
            final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(ks);
            final TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            // Initialize SSLContext with KeyManagers
            final SSLContext sc = SSLContext.getInstance("TLSv1");
            sc.init(keyManagers, trustManagers, forClient ? null : getSecureRandom());
            logger.info("SSL Context setup for {} with provider: {}, protocol: {}", forClient ? "CLIENT" : "SERVER",
                    sc.getProvider(),
                    sc.getProtocol());
            return sc;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static SecureRandom getSecureRandom() throws GeneralSecurityException {
        final SecureRandom random;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new GeneralSecurityException(
                    "Couldn't find SHA1PRNG secure random number generator", e);
        }
        random.setSeed(System.currentTimeMillis());
        return random;
    }
}
