package com.paypal.jsse.benchmark;

import com.paypal.jsse.benchmark.client.HttpsClient;
import com.paypal.jsse.benchmark.client.ReactorNettyHttpsClient;
import com.paypal.jsse.benchmark.client.jmh.HttpsCallBenchmark;
import com.paypal.jsse.benchmark.client.lnp.HttpsClientLoadSim;
import com.paypal.jsse.benchmark.client.lnp.ReactorNettyHttpsClientLoadSim;
import com.paypal.jsse.benchmark.client.metrics.MetricsRegistry;
import com.paypal.jsse.benchmark.server.HttpsServer;
import com.paypal.jsse.benchmark.server.ReactorNettyHttpsServer;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class SysProps {

    public static class ServerConfig {
        String DEFAULT_SERVER_HOST = "127.0.0.1";
        int DEFAULT_SERVER_PORT = 6443;
        String SERVER_HOST_PROP = "test.server.host";
        String SERVER_PORT_PROP = "test.server.port";

        private final String host;
        private final int port;

        public ServerConfig() {
            this.host = readProperty(SERVER_HOST_PROP, String.class, DEFAULT_SERVER_HOST);
            this.port = readProperty(SERVER_PORT_PROP, Integer.class, DEFAULT_SERVER_PORT);
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
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
                "RN-CLIENT",
                ReactorNettyHttpsClient::new,
                ReactorNettyHttpsClientLoadSim::new
        );

        private final String shortName;
        private final Function<MetricsRegistry, HttpsClient<?>> client;

        private final Supplier<HttpsClientLoadSim<?>> loadSim;

        private static final String CLIENT_TYPE_PARAM = "client.type";

        ClientType(final String shortName,
                   final Function<MetricsRegistry, HttpsClient<?>> client,
                   final Supplier<HttpsClientLoadSim<?>> loadSim) {
            this.shortName = shortName;
            this.client = client;
            this.loadSim = loadSim;
        }

        public String getShortName() {
            return shortName;
        }

        public Function<MetricsRegistry, HttpsClient<?>> getClient() {
            return client;
        }

        public Supplier<HttpsClientLoadSim<?>> getLoadSim() {
            return loadSim;
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

    public static class HttpCallBenchmarkConfig {

        final boolean startEmbeddedServer;

        public HttpCallBenchmarkConfig() {
            this.startEmbeddedServer = readProperty("start.embedded.server", Boolean.class, false);
        }

        public boolean isStartEmbeddedServer() {
            return startEmbeddedServer;
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

    public static class SSLConfig {
        private final boolean paypalJsseEnabled;

        public SSLConfig() {
            paypalJsseEnabled = readProperty("paypal.jsse.enable", Boolean.class, true);
        }

        public boolean isPaypalJsseEnabled() {
            return paypalJsseEnabled;
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

    private static <T> T readProperty(final String propertyName,
                                     final Class<T> targetType,
                                     final T defaultValue) {
        final String propertyValue = System.getProperty(propertyName, String.valueOf(defaultValue));
        return convertToSpecificType(propertyValue, targetType);
    }

    @SuppressWarnings("unchecked")
    static <T> T convertToSpecificType(final String propertyValue,
                                       final Class<T> targetType) {
        if (targetType == null || targetType.equals(String.class)) {
            return (T) propertyValue;
        } else if (targetType.equals(Boolean.class)) {
            return (T) Boolean.valueOf(propertyValue);
        } else if (targetType.equals(Integer.class)) {
            return (T) Integer.valueOf(propertyValue);
        } else if (targetType.equals(Double.class)) {
            return (T) Double.valueOf(propertyValue);
        }
        throw new RuntimeException("Unsupported target type: " + targetType);
    }
}
