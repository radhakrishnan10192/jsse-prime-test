/* PAYPAL CONFIDENTIAL - For Evaluation Purposes only */
package com.paypal.infra.ssl;

import sun.security.action.GetPropertyAction;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@SuppressWarnings("restriction")
public class PayPalJSSEUtil {

	public enum SSLREventEnum {
		ClientSessionLoadEvent(1);  // Adding  server  session into client cache
		
		
		private final int event;
	 
		private SSLREventEnum(int s) {
			event = s;
		}
		public int getId() {
			return event;
		}
		
		public String getEventName() {
			String evtName = "";
			
			return evtName;
		}
	 
	}
	
	private static volatile Boolean resumptionLogEnabled = null;
	private static volatile Boolean mockEnabled = null;
	private static volatile Boolean detailedCalLogEnabled = null;
	private static volatile Boolean calLogOn = null;
	
	public  static boolean isResumptionLogOn() {
		//we were getting null when session was getting created.
		if(resumptionLogEnabled == null) {
			// read system prop only once. reading system prop every time is not a good idea
			// in a multi threaded environment as it executed in synchronous block.
			// JDK uses HashMap for system property which inherently thread safe
			String v = java.security.AccessController.doPrivileged(
					new GetPropertyAction("com.paypal.appsec.sslr.logevents", "false"));

			if(v == null)
				resumptionLogEnabled = false;
			else {
				resumptionLogEnabled = (v.compareToIgnoreCase("true") == 0) ? true	: false;
			}
		}
		return resumptionLogEnabled;
    }
	
	public  static boolean isDetailedCalLogOn() {
		
		return detailedCalLogEnabled;
    }
	
	public  static boolean isCalLogOn() {
		
		return calLogOn;
    }
	
	public  static boolean isSSLREventOn() {
		
		return resumptionLogEnabled;
    }
	
	public  static boolean isMockOn() {
		
		return mockEnabled;
    }
	
	public  static String getDateTime() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(getCurrentTimeMillies());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss.SSS");
		String str = sdf.format(calendar.getTime());
		return str;
	}
	
	
	public static void logEvent(SSLREventEnum event, String message,  byte[] sessionId, boolean includeTime) {
		
	}
	
	
	
	
	public static void logMessage(String message) {
		System.out.println(getDateTime() + " : " + "[Thread-" + Thread.currentThread().getId() + "] " +  message );
	}
	
	/* this class isnt been used in any of the latest library code or tests
	// Clock should be set only for test automation
	public static void setMockClock(MockClock c) {
		
	}
	 */
	
	public static long getCurrentTimeMillies() {
			return System.currentTimeMillis();
	}
}
