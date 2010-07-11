import static com.sun.faban.driver.CycleType.THINKTIME;
import static com.sun.faban.driver.Timing.MANUAL;

import java.util.concurrent.TimeUnit;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.sun.faban.driver.BenchmarkDefinition;
import com.sun.faban.driver.BenchmarkDriver;
import com.sun.faban.driver.BenchmarkOperation;
import com.sun.faban.driver.DriverContext;
import com.sun.faban.driver.FlatMix;
import com.sun.faban.driver.NegativeExponential;

@BenchmarkDefinition(name = "SampleDriver", version = "1.0")
@BenchmarkDriver(name = "SampleDriver", responseTimeUnit = TimeUnit.MICROSECONDS)
@FlatMix(operations = { "Foo" }, mix = { 1.0 }, deviation = 1.0)
public class SampleDriver {
    @BenchmarkOperation(name = "Foo", max90th = 1000000, timing = MANUAL)
    @NegativeExponential(cycleType = THINKTIME, cycleMean = 0, cycleDeviation = 0.0)
    public void doFoo() {
        DriverContext.getContext().recordTime();
        for (int i = 0; i < 100000; i++) {
            String foo = Integer.toHexString(i);
        }
        DriverContext.getContext().recordTime();
    }

    public static class GuiceModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Key.get(Object.class, BenchmarkDriver.class)).to(
                    SampleDriver.class);
        }
    }
}
