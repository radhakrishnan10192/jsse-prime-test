package com.paypal.jsse.benchmark.client.lnp;

import com.paypal.jsse.benchmark.config.JsseTestSysProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@FunctionalInterface
public interface LoadSimulator {

    Logger logger = LoggerFactory.getLogger(LoadSimulator.class);

    Runnable loadFn();

    default void execute() {
        JsseTestSysProps.LoadSimulatorConfig loadSimulatorConfig = new JsseTestSysProps.LoadSimulatorConfig();
        try {
            // Number of concurrent users
            int concurrentUsers = loadSimulatorConfig.getNumberOfConcurrentUsers();
            // Execution time in seconds
            long executionTimeInMillis = loadSimulatorConfig.getExecutionTimeInSecs() * 1000L;

            final ExecutorService executorService = Executors.newFixedThreadPool(concurrentUsers);

            // Simulate load with concurrent users
            for (int iteration = 0; iteration < concurrentUsers; iteration++) {
                executorService.submit(() -> {
                    long startTime = System.currentTimeMillis();
                    while (System.currentTimeMillis() - startTime < executionTimeInMillis) { // Run for 10 seconds
                        loadFn().run();
                    }
                });
            }

            logProgress(executionTimeInMillis, 5000);
            logger.info("Load Test Progress: 100%");

            // Shutdown the executor
            executorService.shutdown();
            boolean executionStatus = executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            logger.debug("Load test execution successful: " + executionStatus);
        } catch (Exception e) {
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
}
