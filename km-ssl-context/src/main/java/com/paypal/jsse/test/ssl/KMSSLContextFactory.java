package com.paypal.jsse.test.ssl;

import com.ebayinc.platform.security.SSLContextProvider;
import com.paypal.infra.protectedpkg.Constants;
import com.paypal.infra.protectedpkg.ProtectedPackageUtil;
import com.paypal.platform.security.PayPalSSLHelper;
import com.paypal.platform.security.ProtectedPkgSSLConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;

public class KMSSLContextFactory implements SSLContextFactory {

    private static final Logger logger = LoggerFactory.getLogger(KMSSLContextFactory.class);

    private static SSLContextProvider sslContextProvider;

    private void setupPaypalJSSEProvider() {
        if(sslContextProvider == null) {
            System.setProperty("paypal.jsse.enable", System.getProperty("paypal.jsse.enable", "true"));
            System.setProperty("paypal.jsse.disable", System.getProperty("paypal.jsse.disable", "false"));
            System.setProperty("keymaker.test.appname", "lnpmock");
            ProtectedPackageUtil.setupForInputViaSystemProperty();
            System.setProperty(Constants.SHARE_PASSWORD_PREFIX + 0, "aardvark");
            PayPalSSLHelper.initializeSecurity();
            sslContextProvider = new ProtectedPkgSSLConfigProvider();
        }
    }

    @Override
    public SSLContext sslContext(boolean forClient) {
        setupPaypalJSSEProvider();
        return sslContextProvider.getInternalSSLContext();
    }
}
