package com.paypal.jsse.benchmark.client;

import com.paypal.jsse.benchmark.SysProps;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class JmhHttpsClient<C> {

    private static final Logger logger = LoggerFactory.getLogger(JmhHttpsClient.class);

    protected final C client;

    public JmhHttpsClient() {
        final SysProps.ServerConfig serverConfig = new SysProps.ServerConfig();
        final String host = serverConfig.getHost();
        final int port = serverConfig.getPort();
        client = createHttpsClient(host, port);
    }

    abstract C createHttpsClient(final String host, final int port);

    public abstract void executeHttpsCall(final Metrics metrics);


    @State(Scope.Benchmark)
    public static class Metrics {
        private SSLMetrics sslMetrics;

        @Setup
        public void setup() {
            sslMetrics = new SSLMetrics();
        }

        public SSLMetrics getSslMetrics() {
            return sslMetrics;
        }

        @TearDown
        public void report() {
            sslMetrics.report();
        }
    }

    public static class SSLMetrics {
        private final DescriptiveStatistics sslHandshakeTimes = new DescriptiveStatistics();

        public void addSSLHandshakeTime(final double sslHandshakeTimeInMs) {
            this.sslHandshakeTimes.addValue(sslHandshakeTimeInMs);
        }

        public void report() {
            if(sslHandshakeTimes.getN() > 0) {
                logger.info("\n\n25th percentile: {}", sslHandshakeTimes.getPercentile(25));
                logger.info("50th percentile: {}", sslHandshakeTimes.getPercentile(50));
                logger.info("75th percentile: {}", sslHandshakeTimes.getPercentile(75));
                logger.info("90th percentile: {}", sslHandshakeTimes.getPercentile(90));
                logger.info("95th percentile: {}", sslHandshakeTimes.getPercentile(95));
                logger.info("99th percentile: {}", sslHandshakeTimes.getPercentile(99));
                logger.info("Average time: {}", sslHandshakeTimes.getMean());
                logger.info("Total number of handshakes: {}", sslHandshakeTimes.getN());
            }
        }
    }

    /**
     * Used when {@link Metrics} object is used in Thread scope.
     */
    @State(Scope.Thread)
    private static class Shared {
        List<SSLMetrics> all;
        Queue<SSLMetrics> available;

        @Setup
        public synchronized void setup() {
            all = new ArrayList<>();
            for (int index = 0; index < 10; index++) {
                all.add(new SSLMetrics());
            }

            available = new LinkedList<>();
            available.addAll(all);
        }

        @TearDown
        public synchronized void tearDown() {
            for (final SSLMetrics sslMetrics : all) {
                sslMetrics.report();
            }
        }

        public synchronized SSLMetrics getMine() {
            return available.poll();
        }
    }

    BigDecimal elapsedTime(final long startTimeNS) {
        final double elapsedTime = (System.nanoTime() - startTimeNS) / 1.0e06;
        return BigDecimal.valueOf(elapsedTime).setScale(3, RoundingMode.FLOOR);
    }

}
