/* PAYPAL CONFIDENTIAL - For Evaluation Purposes only */
/**
 * @author klakhwani
 */

package com.paypal.infra.ssl.sessioncacheprovider;

import com.paypal.infra.ssl.PayPalJSSEUtil;

public class SessionCacheParameters {

		private volatile int protocolVersion;
		private volatile int cipher;
		private volatile byte[] session_id;
		private volatile byte[] master_key;
		// Session object that are constructed by loading session data from external session cache (session cdb) need to keep time to live to handle session expire properly.  
	    // a session cached can get extended life when timeout value being set to new value while loading new bunch of sessions. for e.g: a peer session loaded in the middle of week
	    // and the session for same peer missing for next week, in this case life of  session loaded in the middle of week get extended life of extra week. These scenarios happens
	    // only when session missing intermittently.
	    private volatile long sessionTTL;
	    private volatile boolean	sessionFromCache;
	    /*private X509Certificate[] peerCerts;
	    
	    public X509Certificate[] getPeerCerts() {
			return peerCerts;
		}

		public void setPeerCerts(X509Certificate[] peerCerts) {
			this.peerCerts = peerCerts;
		}*/

		public long getTTL() {
			return sessionTTL;
		}

		public void setTTL(long sessionTTL) {
			this.sessionTTL = sessionTTL;
		}
		
		/**
	     * Returns the time this session was created.
	     */
		private long creationTime = PayPalJSSEUtil.getCurrentTimeMillies();
		
	    public long getCreationTime() {
	        return creationTime;
	    }

	    public void setCreationTime(long creationTime) {
	        this.creationTime = creationTime;
	    }
		
	    public int getProtocolVersion() {
			return protocolVersion;
		}

		public void setProtocolVersion(int protocolVersion) {
			this.protocolVersion = protocolVersion;
		}

		public int getCipher() {
			return cipher;
		}

		public void setCipher(int cipher) {
			this.cipher = cipher;
		}

		public byte[] getSession_id() {
			return session_id;
		}

		public void setSession_id(byte[] session_id) {
			this.session_id = session_id;
		}

		public byte[] getMaster_key() {
			return master_key;
		}

		public void setMaster_key(byte[] master_key) {
			this.master_key = master_key;
		}
		
		public void setCacheFlag(boolean b) {
	    	sessionFromCache = b;
	    }
		
		/*public SessionCacheParameters(int protocolVersion, int cipher, byte[] session_id, byte[] master_key, X509Certificate[] peerCerts) {
			this.protocolVersion = protocolVersion;
			this.cipher = cipher;
			this.session_id = session_id;
			this.master_key = master_key;
			this.peerCerts = peerCerts;
		}*/
		
		public SessionCacheParameters(int protocolVersion, int cipher, byte[] session_id, byte[] master_key) {
			this.protocolVersion = protocolVersion;
			this.cipher = cipher;
			this.session_id = session_id;
			this.master_key = master_key;
		}
		//default contructor
		SessionCacheParameters(){}
}
