/* PAYPAL CONFIDENTIAL - For Evaluation Purposes only */
package com.paypal.infra.ssl;


/**
 * 
 * The implementations of this interfaces should return the service name basd on host and port.
 * 
 * The implementors should provide the instance of ServiceNameProvider  via 
 * SSLResumptionParameters.s
 * 
 * @author bantony
 *
 */
public interface ServiceNameProvider {
	
	String get (String hostname, int port);

}
