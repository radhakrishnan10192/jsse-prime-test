package com.paypal.jsse.tester.config;

import com.paypal.jsse.tester.client.ApacheHttpsClient;
import com.paypal.jsse.tester.client.HttpsClient;
import com.paypal.jsse.tester.client.ReactorNettyHttpsClient;
import com.paypal.jsse.tester.client.metrics.MetricsRegistry;
import com.paypal.jsse.tester.server.HttpsServer;
import com.paypal.jsse.tester.server.ReactorNettyHttpsServer;
import com.paypal.jsse.tester.tests.jmh.HttpsCallBenchmark;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.paypal.jsse.test.config.SysPropsReader.readProperty;

public class JsseTestSysProps {

    public static class ServerConfig {

        private static final String DEFAULT_SERVER_HOST = "127.0.0.1";
        private static final int DEFAULT_SERVER_PORT = 6443;
        private static final String SERVER_HOST_PROP = "test.server.host";
        private static final String SERVER_PORT_PROP = "test.server.port";

        private static final String START_EMBEDDED_SVR_PROD = "start.embedded.server";

        private final String host;
        private final int port;

        private final boolean startEmbeddedServer;

        public ServerConfig() {
            this.host = readProperty(SERVER_HOST_PROP, String.class, DEFAULT_SERVER_HOST);
            this.port = readProperty(SERVER_PORT_PROP, Integer.class, DEFAULT_SERVER_PORT);
            this.startEmbeddedServer = readProperty(START_EMBEDDED_SVR_PROD, Boolean.class, false);
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public boolean isStartEmbeddedServer() {
            return startEmbeddedServer;
        }
    }

    public enum ServerType {

        REACTOR_NETTY_SERVER("RN-SERVER", ReactorNettyHttpsServer::new);

        private final String shortName;
        private final Supplier<HttpsServer<?>> server;

        public static final String SERVER_TYPE_PARAM = "server.type";

        ServerType(final String shortName,
                   final Supplier<HttpsServer<?>> server) {
            this.shortName = shortName;
            this.server = server;
        }

        public String getShortName() {
            return shortName;
        }

        public Supplier<HttpsServer<?>> getServer() {
            return server;
        }

        public static Optional<ServerType> getServerType(final String shortName) {
            return Arrays.stream(ServerType.values())
                    .filter(serverType -> serverType.getShortName().equalsIgnoreCase(shortName))
                    .findFirst();
        }

        public static String serverTypePropVal() {
            return readProperty(SERVER_TYPE_PARAM, String.class, REACTOR_NETTY_SERVER.shortName);
        }
    }

    public enum ClientType {
        REACTOR_NETTY_CLIENT(
                "REACTOR_NETTY",
                ReactorNettyHttpsClient::new
        ),
        APACHE_HTTP_CLIENT(
                "APACHE",
                ApacheHttpsClient::new
        );

        private final String shortName;
        private final BiFunction<MetricsRegistry, Boolean, HttpsClient<?>> client;

        private static final String CLIENT_TYPE_PARAM = "client.type";

        ClientType(final String shortName,
                   final BiFunction<MetricsRegistry, Boolean, HttpsClient<?>> client) {
            this.shortName = shortName;
            this.client = client;
        }

        public String getShortName() {
            return shortName;
        }

        public BiFunction<MetricsRegistry, Boolean, HttpsClient<?>> getClient() {
            return client;
        }

        public static String clientTypePropVal() {
            return readProperty(CLIENT_TYPE_PARAM, String.class, REACTOR_NETTY_CLIENT.shortName);
        }

        public static Optional<ClientType> getClientType(final String shortName) {
            return Arrays.stream(ClientType.values())
                    .filter(clientType -> clientType.getShortName().equalsIgnoreCase(shortName))
                    .findFirst();
        }
    }


    public static class JMHConfig {
        final int forks;
        final int warmupTime;
        final int warmupIterations;
        final int warmupBatchSize;
        final int measurementTime;
        final int measurementIterations;
        final int measurementBatchSize;
        final String benchmarkMode;
        final boolean enableJFRProfiler;
        final boolean enableGCProfiler;
        final int threads;
        final String benchmarkTester;

        public JMHConfig() {
            this.forks = readProperty("jmh.forks", Integer.class, 1);
            this.warmupTime = readProperty("jmh.warmup.time.ms", Integer.class, 5000);
            this.warmupIterations = readProperty("jmh.warmup.iterations", Integer.class, 1);
            this.warmupBatchSize = readProperty("jmh.warmup.batch.size", Integer.class, -1);
            this.measurementTime = readProperty("jmh.measurement.time.ms", Integer.class, 60000);
            this.measurementIterations = readProperty("jmh.measurement.iterations", Integer.class, 3);
            this.measurementBatchSize = readProperty("jmh.measurement.batch.size", Integer.class, -1);
            this.benchmarkMode = readProperty("jmh.benchmark.mode", String.class, "AverageTime");
            this.enableJFRProfiler = readProperty("jmh.enable.jfr.profiler", Boolean.class, false);
            this.enableGCProfiler = readProperty("jmh.enable.gc.profiler", Boolean.class, false);
            this.threads = readProperty("jmh.threads", Integer.class, Runtime.getRuntime().availableProcessors());
            this.benchmarkTester = readProperty("jmh.benchmark.testClass", String.class, HttpsCallBenchmark.class.getSimpleName());
        }

        public int getForks() {
            return forks;
        }

        public int getWarmupTime() {
            return warmupTime;
        }

        public int getWarmupIterations() {
            return warmupIterations;
        }

        public int getMeasurementTime() {
            return measurementTime;
        }

        public int getMeasurementIterations() {
            return measurementIterations;
        }

        public String getBenchmarkMode() {
            return benchmarkMode;
        }

        public boolean isEnableJFRProfiler() {
            return enableJFRProfiler;
        }

        public boolean isEnableGCProfiler() {
            return enableGCProfiler;
        }

        public int getThreads() {
            return threads;
        }

        public String getBenchmarkTester() {
            return benchmarkTester;
        }

        public int getWarmupBatchSize() {
            return warmupBatchSize;
        }

        public int getMeasurementBatchSize() {
            return measurementBatchSize;
        }
    }


    public static class HttpClientLoadConfig extends ServerConfig {
        private static final String WARMUP_COUNT_PROP = "client.warmup.count";
        private static final String WARMUP_BUCKET_SIZE_PROP = "client.warmup.bucket.size";
        private static final String WARMUP_DELAY_FOR_EACH_BUCKET_PROP = "client.warmup.bucket.delay.ms";
        private static final String TOTAL_CALLS_PROP = "client.total.calls";
        private static final String BUCKET_SIZE_PROP = "client.bucket.size";
        private static final String DELAY_FOR_EACH_BUCKET_PROP = "client.bucket.delay.ms";

        private static final Integer DEFAULT_WARMUP_COUNT = 2000;
        private static final Integer DEFAULT_WARMUP_BUCKET_SIZE = 100;
        private static final Integer DEFAULT_WARMUP_DELAY_FOR_EACH_BUCKET_MS = 100;
        private static final Integer DEFAULT_TOTAL_CALLS = 60000;
        private static final Integer DEFAULT_BUCKET_SIZE = 100;
        private static final Integer DEFAULT_DELAY_FOR_EACH_BUCKET_MS = 100;

        private final int warmupCount;

        private final int totalNumberOfCalls;

        private final int bucketCount;

        private final int delayForEachBucketInMs;

        private final int warmupBucketSize;

        private final int warmupDelayForEachBucket;

        public HttpClientLoadConfig() {
            super();
            this.warmupCount = readProperty(WARMUP_COUNT_PROP, Integer.class, DEFAULT_WARMUP_COUNT);
            this.warmupBucketSize = readProperty(WARMUP_BUCKET_SIZE_PROP, Integer.class, DEFAULT_WARMUP_BUCKET_SIZE);
            this.warmupDelayForEachBucket = readProperty(WARMUP_DELAY_FOR_EACH_BUCKET_PROP, Integer.class, DEFAULT_WARMUP_DELAY_FOR_EACH_BUCKET_MS);
            this.totalNumberOfCalls = readProperty(TOTAL_CALLS_PROP, Integer.class, DEFAULT_TOTAL_CALLS);
            this.bucketCount = readProperty(BUCKET_SIZE_PROP, Integer.class, DEFAULT_BUCKET_SIZE);
            this.delayForEachBucketInMs = readProperty(DELAY_FOR_EACH_BUCKET_PROP, Integer.class, DEFAULT_DELAY_FOR_EACH_BUCKET_MS);
        }

        public int getWarmupCount() {
            return warmupCount;
        }

        public int getTotalNumberOfCalls() {
            return totalNumberOfCalls;
        }

        public int getBucketCount() {
            return bucketCount;
        }

        public int getDelayForEachBucketInMs() {
            return delayForEachBucketInMs;
        }

        public int getWarmupBucketSize() {
            return warmupBucketSize;
        }

        public int getWarmupDelayForEachBucket() {
            return warmupDelayForEachBucket;
        }
    }

    public static class LoadSimulatorConfig {

        private static final String NO_OF_CONCURRENT_USERS = "concurrent.users";
        private static final String EXECUTION_TIME_IN_SECS = "execution.time.secs";
        private static final String WARMUP_TIME_IN_SECS = "warmup.time.secs";
        private static final String DELAY_BETWEEN_CALLS_IN_MS = "delay.between.call.ms";

        private final int numberOfConcurrentUsers;
        private final int executionTimeInSecs;
        private final int warmupTimeInSecs;
        private final int delayBetweenCallsInMs;

        public LoadSimulatorConfig() {
            this.numberOfConcurrentUsers = readProperty(NO_OF_CONCURRENT_USERS, Integer.class, 2);
            this.executionTimeInSecs = readProperty(EXECUTION_TIME_IN_SECS, Integer.class, 60);
            this.warmupTimeInSecs = readProperty(WARMUP_TIME_IN_SECS, Integer.class, 30);
            this.delayBetweenCallsInMs = readProperty(DELAY_BETWEEN_CALLS_IN_MS, Integer.class, 0);
        }

        public int getNumberOfConcurrentUsers() {
            return numberOfConcurrentUsers;
        }

        public int getExecutionTimeInSecs() {
            return executionTimeInSecs;
        }

        public int getWarmupTimeInSecs() {
            return warmupTimeInSecs;
        }
        public int getDelayBetweenCallsInMs() {
            return delayBetweenCallsInMs;
        }
    }
}
