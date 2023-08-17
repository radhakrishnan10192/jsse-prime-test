package com.paypal.jsse.tester.tests.lnp;

import com.paypal.jsse.tester.config.JsseTestSysProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@FunctionalInterface
public interface LoadSimulator {

    Logger logger = LoggerFactory.getLogger(LoadSimulator.class);

    Runnable loadFn();

    default void execute(TestType testType) {
        final JsseTestSysProps.LoadSimulatorConfig loadSimulatorConfig = new JsseTestSysProps.LoadSimulatorConfig();
        this.execute(testType,
                loadSimulatorConfig.getNumberOfConcurrentUsers(),
                testType == TestType.Warmup ? loadSimulatorConfig.getWarmupTimeInSecs() : loadSimulatorConfig.getExecutionTimeInSecs(),
                loadSimulatorConfig.getDelayBetweenCallsInMs());
    }

    default void execute(final TestType testType,
                         final int noOfConcurrentUsers,
                         final int executionTimeInSecs,
                         final int delayBetweenCalls) {
        try {
            // Execution time in seconds
            long executionTimeInMillis = executionTimeInSecs * 1000L;

            final ExecutorService executorService = Executors.newFixedThreadPool(noOfConcurrentUsers);

            // Simulate load with concurrent users
            for (int iteration = 0; iteration < noOfConcurrentUsers; iteration++) {
                executorService.submit(() -> {
                    long startTime = System.currentTimeMillis();
                    while (System.currentTimeMillis() - startTime < executionTimeInMillis) {
                        if(delayBetweenCalls > 0) sleep(delayBetweenCalls);
                        loadFn().run();
                    }
                });
            }
            logProgress(executionTimeInMillis, 5000);
            logger.info("{} Test Progress: 100%", testType);

            // Shutdown the executor
            executorService.shutdown();
            boolean executionStatus = executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            logger.debug("{} test execution successful: {}" , noOfConcurrentUsers, executionStatus);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void sleep(final int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static void logProgress(final long executionTimeInMillis,
                                    final long intervalInMillis) throws InterruptedException {
        // Print progress indicators
        long elapsedTime = 0;
        while (elapsedTime < executionTimeInMillis) {
            logger.info("Load Test Progress: {}%", (int) ((double) elapsedTime / executionTimeInMillis * 100));
            Thread.sleep(intervalInMillis);
            elapsedTime += intervalInMillis;
        }
    }

    enum TestType {
        Warmup, Load
    }
}
