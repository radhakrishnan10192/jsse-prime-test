package com.paypal.jsse.benchmark;

import com.paypal.jsse.benchmark.client.JmhHttpsClient;
import com.paypal.jsse.benchmark.client.ReactorNettyJmhHttpsClient;
import com.paypal.jsse.benchmark.jmh.HttpsCallBenchmark;
import com.paypal.jsse.benchmark.server.HttpsServer;
import com.paypal.jsse.benchmark.server.ReactorNettyHttpsServer;

import java.util.Arrays;
import java.util.Optional;
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
        REACTOR_NETTY_CLIENT("RN-CLIENT", ReactorNettyJmhHttpsClient::new);

        private final String shortName;
        private final Supplier<JmhHttpsClient<?>> client;

        private static final String CLIENT_TYPE_PARAM = "client.type";

        ClientType(final String shortName,
                   final Supplier<JmhHttpsClient<?>> client) {
            this.shortName = shortName;
            this.client = client;
        }

        public String getShortName() {
            return shortName;
        }

        public Supplier<JmhHttpsClient<?>> getClient() {
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

    public static class HttpCallBenchmarkConfig {

        final boolean startEmbeddedServer;

        public HttpCallBenchmarkConfig() {
            this.startEmbeddedServer = readProperty("start.embedded.server", Boolean.class, true);
        }

        public boolean isStartEmbeddedServer() {
            return startEmbeddedServer;
        }
    }

    public static class JMHConfig {
        final int forks;
        final int warmupTime;
        final int warmupIterations;
        final int measurementTime;
        final int measurementIterations;
        final String benchmarkMode;
        final boolean enableJFRProfiler;
        final boolean enableGCProfiler;
        final int threads;
        final String benchmarkTester;

        public JMHConfig() {
            forks = readProperty("jmh.forks", Integer.class, 1);
            warmupTime = readProperty("jmh.warmup.time.ms", Integer.class, 5000);
            warmupIterations = readProperty("jmh.warmup.iterations", Integer.class, 1);
            measurementTime = readProperty("jmh.measurement.time.ms", Integer.class, 60000);
            measurementIterations = readProperty("jmh.measurement.iterations", Integer.class, 3);
            benchmarkMode = readProperty("jmh.benchmark.mode", String.class, "AverageTime");
            enableJFRProfiler = readProperty("jmh.enable.jfr.profiler", Boolean.class, false);
            enableGCProfiler = readProperty("jmh.enable.gc.profiler", Boolean.class, false);
            threads = readProperty("jmh.threads", Integer.class, Runtime.getRuntime().availableProcessors());
            benchmarkTester = readProperty("jmh.benchmark.testClass", String.class, HttpsCallBenchmark.class.getSimpleName());
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
