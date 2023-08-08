package com.paypal.jsse.benchmark.client.metrics;

import org.openjdk.jmh.annotations.TearDown;

import java.util.LinkedList;
import java.util.List;

public class MetricsRegistry {
    private final List<Metric> metrics;

    public MetricsRegistry() {
        metrics = new LinkedList<>();
    }

    public void addMetric(final Metric metric) {
        this.metrics.add(metric);
    }

    public void resetAll() {
        this.metrics.forEach(Metric::reset);
    }

    public void report() {
        this.metrics.forEach(Metric::report);
    }
}
