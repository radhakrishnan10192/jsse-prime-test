package com.paypal.jsse.benchmark.client.metrics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Metric {

    Logger logger = LoggerFactory.getLogger(Metric.class);

    void addMetric(final double data);

    void report();

    void reset();


    abstract class HistogramMetric implements Metric {
        private final DescriptiveStatistics statistics;

        public HistogramMetric() {
            this.statistics = new DescriptiveStatistics();
        }

        abstract String metricName();

        @Override
        public void addMetric(final double data) {
            this.statistics.addValue(data);
        }

        @Override
        public void reset() {
            this.statistics.clear();
        }

        @Override
        public void report() {
            if(statistics.getN() > 0) {
                logger.info("\n\nMetrics for {}", metricName());
                logger.info("\n25th percentile: {}", statistics.getPercentile(25));
                logger.info("50th percentile: {}", statistics.getPercentile(50));
                logger.info("75th percentile: {}", statistics.getPercentile(75));
                logger.info("90th percentile: {}", statistics.getPercentile(90));
                logger.info("95th percentile: {}", statistics.getPercentile(95));
                logger.info("99th percentile: {}", statistics.getPercentile(99));
                logger.info("Average time: {}", statistics.getMean());
            }
        }
    }


    class SSLMetric extends HistogramMetric {
        @Override
        String metricName() {
            return "SSLHandshakeTimeInMs";
        }
    }
}
