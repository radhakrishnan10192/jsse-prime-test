/* PAYPAL CONFIDENTIAL - For Evaluation Purposes only */
package com.paypal.infra.ssl.sessioncacheprovider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.infra.ssl.SessionID;
import com.paypal.infra.ssl.util.MockDataHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSession;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SSLSessionCacheProviderTestImpl implements  SSLSessionCacheProvider{
    
    private volatile boolean protectedPackageLoaded;

    private ConcurrentHashMap<SessionID, SessionCacheParameters> serverSideSessionCache = new ConcurrentHashMap<SessionID, SessionCacheParameters>();

    private ConcurrentHashMap<String, SessionCacheParameters> clientSide_serviceNameToSessionCache = new ConcurrentHashMap<String, SessionCacheParameters>();

    private ConcurrentHashMap<SessionID, SessionCacheParameters> clientSide_SessionIDToSessionCache = new ConcurrentHashMap<SessionID, SessionCacheParameters>();

    final static String PROPERTIES_FILE = "mockData.properties";

    private String SERVICE_NAME = "universalserv";

    private static MockDataHelper mockDataHelper = new MockDataHelper();

    private static volatile SSLSessionCacheProviderTestImpl s_instance = null;

    private static Logger LOG = LoggerFactory.getLogger(SSLSessionCacheProviderTestImpl.class);

    public static SSLSessionCacheProvider getInstance() {

        s_instance = new SSLSessionCacheProviderTestImpl();

        return s_instance;
    }

    public ConcurrentHashMap<SessionID, SessionCacheParameters> getServerSideSessionCache() {
        return serverSideSessionCache;
    }

    public int getServerSideSessionCacheCount() {
        return serverSideSessionCache.size();
    }

    public ConcurrentHashMap<String, SessionCacheParameters> getClientSide_serviceNameToSessionCache() {
        return clientSide_serviceNameToSessionCache;
    }

    public int getClientSide_serviceNameToSessionCacheCount() {
        return clientSide_serviceNameToSessionCache.size();
    }

    public SessionCacheParameters getSession(byte[] id)
    {
        if(!serverSideSessionCache.isEmpty()){
            serverSideSessionCache.clear();
        }
        SessionID sessionId = new SessionID(id);
        if(mockDataHelper.getBoolean("LoadMultipleSessionsOnServerSide")) {
            loadSessions(0, true);
            return (SessionCacheParameters) this.serverSideSessionCache.get(sessionId);
        }else{
            try {
                SessionCacheParameters serverSessionCacheParameter = (SessionCacheParameters) mockDataHelper.getPropertyAsObject(
                        "ServerSessionCacheParameters", SessionCacheParameters.class);
                put(null, serverSessionCacheParameter, true);
                return serverSideSessionCache.get(sessionId);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public SessionCacheParameters get(String hostname, int port) {

        if(mockDataHelper.getBoolean("LoadMultipleSessionsOnClientSide")){

            loadSessions(0, false);
            if(clientSide_serviceNameToSessionCache.get(SERVICE_NAME) != null){
                return clientSide_serviceNameToSessionCache.get(SERVICE_NAME);
            }else{
                return clientSide_serviceNameToSessionCache.get(hostname + ":" + port);
            }
        }else {
            try {
                return (SessionCacheParameters) mockDataHelper.getPropertyAsObject(
                        "ClientSessionCacheParameters", SessionCacheParameters.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    

    boolean hasProtectedPackageLoaded() {

        
        return protectedPackageLoaded;
    }

    boolean shouldLoadNextSessions(boolean bServer) {
        
        return bServer;
    }

    private void loadSessions() {
        loadSessions(0, false);
    }

    // Making loadSessions() public to enable session refresh from background thread. Call must be synchronized
    public void loadSessions(long time, boolean bServer) {


        if(bServer) {
            LOG.debug("Loading the sessions from properties file into server cache ..");
            try {
                List<SessionCacheParameters> sessionCacheParametersList = new ObjectMapper().convertValue
                        (mockDataHelper.getPropertiesAsListOfObjects("SessionCacheParametersList"),
                                new TypeReference<List<SessionCacheParameters>>() {});
                for (SessionCacheParameters sessionCacheParameter : sessionCacheParametersList) {
                    //serverside cache doesn't require the key where as the client side cache requires.
                    put(null, sessionCacheParameter, bServer);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }else{
        // if non-empty, we are returning, so that means cache remains same
            if(!clientSide_serviceNameToSessionCache.isEmpty()){
                clientSide_serviceNameToSessionCache.clear();
            }
            LOG.debug("Loading the sessions from properties file into client cache ..");
            try {
                List<SessionCacheParameters> sessionCacheParametersList = new ObjectMapper().convertValue
                        (mockDataHelper.getPropertiesAsListOfObjects("SessionCacheParametersList"),
                                new TypeReference<List<SessionCacheParameters>>() {});
                clientSide_serviceNameToSessionCache.put(SERVICE_NAME, sessionCacheParametersList.get(0));
                clientSide_serviceNameToSessionCache.put(mockDataHelper.getString("host") + ":" +
                        mockDataHelper.getString("port"), sessionCacheParametersList.get(1));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    private void put(String key, SessionCacheParameters s, boolean bServer) {
        // this method will be called only by loadSessions method
        if(bServer) {
            serverSideSessionCache.put(new SessionID(s.getSession_id()), s);
        }
    }

    public void preLoadSessions(boolean bServer) {
        
    }

    private Map<String, SessionCacheParameters> getPreLoadedSessions(boolean bServer) {
       
            return null;
    }

    /*
    Azul change
    commented due to deletion of SessionBuilder method
    private SessionBuilder getSessionBuilder(boolean bServer) {
        SessionBuilder sessionBuilder = null;
        return sessionBuilder ;
    }
    */

    private static SecureRandom createSecureRandom() {
        SecureRandom sr = null;
        return sr;
    }

    private void invalidateExpiredServerSideCachedSessions() {
       
    }

    boolean isTimedout(SessionCacheParameters sess) {
        boolean timedout = false;

        return timedout;

    }

    SessionCacheParameters checkTimeValidity(SessionCacheParameters sess) {
        if(sess == null)
            return null;
       
        return sess;
    }

    public boolean isSessionCached(byte[] sessionId, boolean client) {
        return client ;
    }

    /*
     * allocate a separate thread to pre-load sessions. this thread always loads session for advanced time
     */
    private static class SessionLoaderThread extends Thread {

        
        private boolean bServer;
        private long time; // time for which sessions to be preloaded
        private long sessionActivationTime; // time at which preloaded sessions become active. set this time as session create time
        

        public void run() {
            
        }
    }
    

    public void handShakeCompleted(SSLSession s, boolean isClient) {
    }
}
