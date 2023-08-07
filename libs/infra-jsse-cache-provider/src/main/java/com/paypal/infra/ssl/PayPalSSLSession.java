/* PAYPAL CONFIDENTIAL - For Evaluation Purposes only */
/**
 * 
 */
package com.paypal.infra.ssl;

import javax.net.ssl.SSLSession;

/**
 * @author akanamkandy
 *
 */
public interface PayPalSSLSession extends SSLSession {
	public boolean isPPCachedSession();
	public boolean isResumed();
}
