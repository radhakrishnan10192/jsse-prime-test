package com.paypal.infra.ssl;

import java.security.AccessController;
import java.security.Provider;
import java.security.Security;

import sun.security.action.GetPropertyAction;

/**
 * Defines the Paypal JSSE Provider and the algorithms supported by PayPal JSSE provider
 */

public class PayPalProvider extends Provider {
	private static final long serialVersionUID = -3263311943136849071L;

	private static String VERSION = "8.212";
	private static String INFO = "PayPal JSSE Provider v" + VERSION;

	public static String PROVIDER_NAME = "PayPal";

	public PayPalProvider() {
		super( PROVIDER_NAME, Double.parseDouble( VERSION ), INFO );
		
		//turning off extendedMasterSecret (refer rfc7627) as the existing PayPal sessions do not support this extension
		//and hence keeping it true breaks SSL-R (full handshake works even with true)
		System.setProperty("jdk.tls.useExtendedMasterSecret", "false");
	   //adding RASignature -  03112011
	   put("Signature.MD5andSHA1withRSA",
	   			"com.paypal.infra.ssl.jsse.RSASignature");
	       
        /*
         * SSL/TLS mechanisms
         */

       put("SSLContext.SSL",
                "com.paypal.infra.ssl.jsse.SSLContextImpl$TLS10Context");
       put("SSLContext.SSLv3",
                "com.paypal.infra.ssl.jsse.SSLContextImpl$TLS10Context");
       put("SSLContext.TLS",
            "com.paypal.infra.ssl.jsse.SSLContextImpl$TLS10Context");
       put("SSLContext.TLSv1",
            "com.paypal.infra.ssl.jsse.SSLContextImpl$TLS11Context");
       put("SSLContext.TLSv2",
               "com.paypal.infra.ssl.jsse.SSLContextImpl$TLS12Context");
       put("SSLContext.Default",
            "com.paypal.infra.ssl.jsse.DefaultSSLContextImpl$DefaultSSLContext");
       
       // 05/13/2011 - adding X509 and PKIX . Also adding SSL_TLS protocol (to support PP's geronimo configuration)
       put("SSLContext.SSL_TLS",
       "com.paypal.infra.ssl.jsse.SSLContextImpl");
       
       put("KeyManagerFactory.SunX509",
       "com.paypal.infra.ssl.jsse.KeyManagerFactoryImpl$SunX509");
	   put("KeyManagerFactory.NewSunX509",
	       "com.paypal.infra.ssl.jsse.KeyManagerFactoryImpl$X509");
	   put("Alg.Alias.KeyManagerFactory.PKIX", "NewSunX509");
	   put("TrustManagerFactory.SunX509",
	       "com.paypal.infra.ssl.jsse.TrustManagerFactoryImpl$SimpleFactory");
	   put("TrustManagerFactory.PKIX",
	       "com.paypal.infra.ssl.jsse.TrustManagerFactoryImpl$PKIXFactory");
	   put("Alg.Alias.TrustManagerFactory.SunPKIX", "PKIX");
	   put("Alg.Alias.TrustManagerFactory.X509", "PKIX");
	   put("Alg.Alias.TrustManagerFactory.X.509", "PKIX");
	   
	   // Disable SNI extension if clients disabled it. By default It is enabled
	   String enableSNIExtension = (String) AccessController.doPrivileged(
               new GetPropertyAction("jsse.enableSNIExtension"));
       if (enableSNIExtension == null) 
           System.setProperty("jsse.enableSNIExtension", "true");
	}

}