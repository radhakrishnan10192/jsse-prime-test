package com.paypal.jsse.benchmark.client.jmh;

import com.paypal.jsse.benchmark.SysProps;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.JavaFlightRecorderProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.openjdk.jmh.runner.options.TimeValue.milliseconds;

public class JmhExecutor {
    private static final Logger logger = LoggerFactory.getLogger(JmhExecutor.class);

    public JmhExecutor()  {
        final SysProps.JMHConfig jmhConfig = new SysProps.JMHConfig();
        try {
            final ChainedOptionsBuilder optionsBuilder = new OptionsBuilder()
                    .include(jmhConfig.getBenchmarkTester())
                    .forks(jmhConfig.getForks())
                    .threads(jmhConfig.getThreads())
                    .warmupIterations(jmhConfig.getWarmupIterations())
                    .warmupTime(milliseconds(jmhConfig.getWarmupTime()))
                    .measurementIterations(jmhConfig.getMeasurementIterations())
                    .measurementTime(milliseconds(jmhConfig.getMeasurementTime()))
                    .mode(Mode.valueOf(jmhConfig.getBenchmarkMode()))
                    .resultFormat(ResultFormatType.JSON)
                    .result(String.format("benchmark-%s-%s.json",
                            jmhConfig.getBenchmarkTester(),
                            System.getProperty("java.version")))
                    .timeUnit(TimeUnit.MILLISECONDS);
            if(jmhConfig.getWarmupBatchSize() > 0) {
                optionsBuilder.warmupBatchSize(jmhConfig.getWarmupBatchSize());
            }
            if(jmhConfig.getMeasurementBatchSize() > 0) {
                optionsBuilder.measurementBatchSize(jmhConfig.getMeasurementBatchSize());
            }
            if(jmhConfig.isEnableJFRProfiler()) {
                optionsBuilder.addProfiler(JavaFlightRecorderProfiler.class);
            }
            if(jmhConfig.isEnableGCProfiler()) {
                optionsBuilder.addProfiler(GCProfiler.class);
            }
            final Options opt = optionsBuilder.build();
            new Runner(opt).run();
        } catch (RunnerException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
