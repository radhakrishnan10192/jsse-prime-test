package com.paypal.jsse.tester.tests.sslr;

import com.paypal.jsse.tester.client.HttpsClient;
import com.paypal.jsse.tester.tests.TestExecutor;

public class SslResumptionValidator implements TestExecutor {

    public SslResumptionValidator() {
        initializeTestServer();
        final HttpsClient<?> client =  initializeTestClient(null, true);
        client.executeHttpsCall();
    }
}
