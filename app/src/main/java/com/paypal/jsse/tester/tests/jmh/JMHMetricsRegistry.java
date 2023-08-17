package com.paypal.jsse.tester.tests.jmh;


import com.paypal.jsse.tester.client.metrics.MetricsRegistry;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class JMHMetricsRegistry extends MetricsRegistry {

    @TearDown
    @Override
    public void report() {
        super.report();
    }
}
