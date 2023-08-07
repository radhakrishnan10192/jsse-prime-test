/* PAYPAL CONFIDENTIAL - For Evaluation Purposes only */
/**
 * 
 */
package com.paypal.infra.ssl;


import com.paypal.infra.ssl.util.MockDataHelper;

import java.util.List;

/**
 * @author 
 * PayPalJSSEProperties - class facilitates setting of certain properties of SSL context like client and server cipher suit setting.
 */
@SuppressWarnings("restriction")
public class PayPalJSSEProperties {
	//below ciphersuites match the paypal_external_ssl_thwate profile in KM
	private static volatile Boolean preloadOption = null;
	
	
	//setting default set of cipher suite for server. this list can be override by calling method setServerCipherSuites()
	private static volatile String[] serverCiphers = null;
	
	//setting default set of cipher suite for client. this list can be override by calling method setClientCipherSuites()
	private static volatile String[] clientCiphers = null;
//	Azul change
//	private static volatile  SSLSessionCacheMode sessionCacheMode = SSLSessionCacheMode.SSL_SESS_CACHE_NO_INTERNAL_STORE;
	
	private static volatile String[] serverProtocolsEnabled = null;
	private static volatile String[] clientProtocolsEnabled = null;
	private static volatile Byte clientAuth = null;

	final static String PROPERTIES_FILE = "mockData.properties";

	private static MockDataHelper mockDataHelper = setMockDataHelper();

	public static final byte CLAUTH_NONE = 0;
	public static final byte CLAUTH_REQUESTED = 1;
	public static final byte CLAUTH_REQUIRED = 2;
	public static final byte CLAUTH_NOT_SET = 3;

	/**
	 * set server side cipher suites. This cipher suites used for SSL ServerHello message.
	 * The cipher suite names are explained in JSSE user guides.
	 * @param ciphers
	 * null - set ciphers to null if need to set JSSE's default cipher suites (full set of cipher suite)
	 * explicitly set array of cipher suite if client need to override cipher suite set by PayPal JSSE 
	 */
	synchronized public static void setServerCipherSuites(String[] ciphers) {

	}

	private static MockDataHelper setMockDataHelper(){

		return new MockDataHelper();
	}
	
	public static String[] getServerCipherSuites() {

		return getArrayProperties("ServerCipherSuites");
		// nothing configured in protected so use default
	}
	
	/**
	 * set client side cipher suites. This cipher suites used for SSL ClientHello message.
	 * The cipher suite names are explained in JSSE user guides.
	 * @param ciphers
	 * null - set ciphers to null if need to set JSSE's default cipher suites (full set of cipher suite)
	 * explicitly set array of cipher suite if client need to override cipher suite set by PayPal JSSE
	 */
	synchronized public static void setClientCipherSuites(String[] ciphers) {

	}
	
	public static String[] getClientCipherSuites() {

		return getArrayProperties("ClientCipherSuites");
	}

	private static String[] getArrayProperties(String cipherSuites) {

		List<String> serverCipherSuites = mockDataHelper.getDelimitedValuesAsListOfString(cipherSuites, ",");
		serverCiphers = new String[serverCipherSuites.size()];
		return serverCipherSuites.toArray(serverCiphers);
	}

	//08/01/2011 - methods for setting SSL cache mode
	/**
	 * method to set SSL session cache mode.
	 * @param SSLSessionCacheMode
	 */
	/*
    Azul change: commented due to deletion of SSLSessionCacheMode method
	public static void setSessionCacheMode(SSLSessionCacheMode mode) {

	}
	
	 */
	
	/**
	 * method to retrieve current SSL session cache mode.
	 * return SSLCacheMode
	 */
	/*
    Azul change: commented due to deletion of SSLSessionCacheMode method
	public static SSLSessionCacheMode getSessionCacheMode() {
		return sessionCacheMode;
	}
	
	 */
	
	public static boolean isJsseLoadable() {
		return true;
	}
	
	public static boolean isPreLoadSessionsOn() {
		return preloadOption;
    }
	
	public static String[] getServerProtocolsEnabled() {

		return getArrayProperties("ServerProtocolsEnabled");
	}

	public static String[] getClientProtocolsEnabled() {

		return getArrayProperties("ClientProtocolsEnabled");
	}

	/**
	 * Adding this API just in case any Client want to override protocol versions. 
	 * @param clientProtocols
	 */
	synchronized public static void setClientProtocolsEnabled(String clientProtocols) {

	}
	
	synchronized public static void setServerProtocolsEnabled(String serverProtocols) {

	}
	
	public static byte getClientAuth() {

		switch (mockDataHelper.getString("ClientAuth")){
			case "openssl_client_certs" : return CLAUTH_REQUESTED;
			case "openssl_custom_auth" : return CLAUTH_REQUIRED;
			case "openssl_no_auth" : return CLAUTH_NONE;
			default: return CLAUTH_NOT_SET;
		}
    }
	
	/**
	 * set Client Auth flag. Possible values are
	 * PayPalJSSEProperties.CLAUTH_REQUIRED
	 * PayPalJSSEProperties.CLAUTH_REQUESTED
	 * PayPalJSSEProperties.CLAUTH_NONE
	 * @param flag
	 * @return
	 */
	synchronized public static void setClientAuth(byte flag) {
		clientAuth = flag;
	}

	private static String getProtectedProperty(String property) {
		String v = null;
		return v;
	}
	
	private static String formatVersion(String str) {
		return ("0000" + str).substring(str.length());
	}
}
