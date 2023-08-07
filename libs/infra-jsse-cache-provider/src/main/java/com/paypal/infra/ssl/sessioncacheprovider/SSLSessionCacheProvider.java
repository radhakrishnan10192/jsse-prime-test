/* PAYPAL CONFIDENTIAL - For Evaluation Purposes only */
package com.paypal.infra.ssl.sessioncacheprovider;

import com.paypal.infra.ssl.SessionID;

import javax.net.ssl.SSLSession;
import java.util.concurrent.ConcurrentHashMap;

public interface SSLSessionCacheProvider {

	public SessionCacheParameters   getSession(byte[] id);
	public SessionCacheParameters get(String hostname, int port);
	public boolean isSessionCached(byte[] sessionId, boolean client);
	public void handShakeCompleted(SSLSession s, boolean isClient);
	public ConcurrentHashMap<SessionID, SessionCacheParameters> getServerSideSessionCache();
	public ConcurrentHashMap<String, SessionCacheParameters> getClientSide_serviceNameToSessionCache();
	public void loadSessions(long time, boolean bServer);
}
