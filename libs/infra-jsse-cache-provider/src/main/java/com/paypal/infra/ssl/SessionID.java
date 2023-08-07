/* PAYPAL CONFIDENTIAL - For Evaluation Purposes only */
package com.paypal.infra.ssl;

import java.util.Arrays;


public class SessionID
{
    private final byte sessionId [];          // max 32 bytes

    /** Constructs a session ID from a byte array (max size 32 bytes) */
    public SessionID (byte sessionId []) { this.sessionId = sessionId; }

    /** Returns the length of the ID, in bytes */
    int length () { return sessionId.length; }

    /** Returns the bytes in the ID.  May be an empty array.  */
    public byte [] getId ()
    {
        return (byte []) sessionId.clone ();
    }

    /** Returns the ID as a string */
    public String toString ()
    {
        int             len = sessionId.length;
        StringBuilder    s = new StringBuilder (10 + 2 * len);

        s.append ("{");
        for (int i = 0; i < len; i++) {
            s.append (0x0ff & sessionId [i]);
            if (i != (len - 1))
                s.append (", ");
        }
        s.append ("}");
        return s.toString ();
    }


    /** Returns a value which is the same for session IDs which are equal */
    public int hashCode ()
    {
        return Arrays.hashCode(sessionId);
    }

    /** Returns true if the parameter is the same session ID */
    public boolean equals (Object obj)
    {
        if (!(obj instanceof SessionID)) {
            return false;
        }
        return Arrays.equals(sessionId, ((SessionID)obj).sessionId);
    }
}
