package com.paypal.jsse.benchmark;


import com.paypal.jsse.benchmark.jmh.JmhExecutor;
import com.paypal.jsse.benchmark.server.StandaloneServer;
import com.paypal.jsse.benchmark.sslr.SslResumptionValidator;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class JsseTestApp {

    public static void main(String[] args) {
        final String testModeValue = System.getProperty("test.mode");
        if(isBlank(testModeValue)) {
            throw new RuntimeException("System property test.mode is mandatory.");
        }
        TestMode.getTestMode(testModeValue)
                .orElseThrow(() -> new RuntimeException("Invalid test mode: " + testModeValue))
                .getImplementation()
                .get();
    }

    public enum TestMode {
        JMH_CLIENT_CALLS("jmh-client-calls", JmhExecutor::new),
        START_SERVER("start-server", StandaloneServer::new),
        SSLR("sslr", SslResumptionValidator::new);

        private final String testMode;
        private final Supplier<?> implementation;

        TestMode(final String testMode, final Supplier<?> implementation) {
            this.testMode = testMode;
            this.implementation = implementation;
        }

        public Supplier<?> getImplementation() {
            return implementation;
        }

        static Optional<TestMode> getTestMode(final String testMode) {
            return Arrays.stream(TestMode.values())
                    .filter(mode -> mode.testMode.equalsIgnoreCase(testMode))
                    .findFirst();
        }
    }

}
