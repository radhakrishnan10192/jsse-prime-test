package com.paypal.jsse.tester.config;

import com.paypal.jsse.tester.tests.jmh.HttpsCallBenchmark;

import static com.paypal.jsse.test.config.SysPropsReader.readProperty;

public class JsseTestSysProps {



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
        final String timeUnit;

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
            this.timeUnit = readProperty("jmh.time.unit", String.class, "MILLISECONDS");
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

        public String getTimeUnit() {
            return timeUnit;
        }
    }
}
