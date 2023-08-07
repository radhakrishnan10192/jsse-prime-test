/* PAYPAL CONFIDENTIAL - For Evaluation Purposes only */
package com.paypal.infra.ssl.sessioncacheprovider;

/**
 * 
 * @author akanamkandy
 * Factory to create SSLSessionCacheProvider. For now we have only CDB based cacheprovider but
 * in future we could have different type of cache provider.
 * 
 *  introducing factory calss to decouple JSS from cacheprvider impl
 */
public class SSLSessionCacheProviderFactory {
	public static SSLSessionCacheProvider getSSLCacheProvider() {
		// for now simply return CDBBased cache provider
		return SSLSessionCacheProviderTestImpl.getInstance();
	}
}
