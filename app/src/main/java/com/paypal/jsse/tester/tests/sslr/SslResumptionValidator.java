package com.paypal.jsse.tester.tests.sslr;

import com.paypal.jsse.tester.client.HttpsClient;
import com.paypal.jsse.tester.tests.TestExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SslResumptionValidator implements TestExecutor {

    private static final Logger logger = LoggerFactory.getLogger(SslResumptionValidator.class);

    public SslResumptionValidator() {
        initializeTestServer();
        final HttpsClient<?> client =  initializeTestClient(null, true);
        client.executeHttpsCall();
    }
}
