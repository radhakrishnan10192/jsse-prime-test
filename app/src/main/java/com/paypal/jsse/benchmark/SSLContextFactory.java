package com.paypal.jsse.benchmark;

import com.paypal.infra.ssl.PayPalProvider;
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
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Objects;

public class SSLContextFactory {
    private static final Logger logger = LoggerFactory.getLogger(SSLContextFactory.class);

    private static void setupPaypalJSSEProvider() {
        final boolean isPaypalJSSEEnabled = new SysProps.SSLConfig().isPaypalJsseEnabled();
        if(isPaypalJSSEEnabled) {
            final Provider[] registeredProviders = Security.getProviders();
            if (registeredProviders != null) {
                for (Provider provider : registeredProviders) {
                    if (provider instanceof com.paypal.infra.ssl.PayPalProvider) {
                        return;
                    }
                }
            }
            // check for match of JDK and PayPal JSSE versions
            final Provider paypalProvider = new PayPalProvider();
            final int position = 1;
            Security.insertProviderAt(paypalProvider, position);
            logger.info("PaypalProvider inserted at " + 1);
        }
    }

    public static SSLContext sslContext(final boolean forClient) {
        setupPaypalJSSEProvider();
        try {
            // Initialize KeyManagerFactory with the test keystore
            final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            final KeyStore ks = KeyStore.getInstance("JCEKS");
            final String password = "123456789";
            final String protectedFilePath = Objects.requireNonNull(SSLContextFactory.class.getClassLoader().getResource("protected")).getPath();
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
