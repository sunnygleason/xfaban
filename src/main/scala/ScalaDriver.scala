import java.io.InputStream;
import java.io.PrintStream;
import java.io.IOException;

import com.sun.faban.driver.CycleType;
import com.sun.faban.driver.Timing;

import java.util.concurrent.TimeUnit;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.sun.faban.driver.BenchmarkDefinition;
import com.sun.faban.driver.BenchmarkDriver;
import com.sun.faban.driver.BenchmarkOperation;
import com.sun.faban.driver.DriverContext;
import com.sun.faban.driver.FlatMix;
import com.sun.faban.driver.NegativeExponential;

@BenchmarkDefinition { val name = "SampleDriver", val version = "1.0" }
@BenchmarkDriver { val name = "SampleDriver", val responseTimeUnit = TimeUnit.MICROSECONDS }
@FlatMix {val operations = Array("Foo"), val mix = Array(1.0), val deviation = 1.0 }
class ScalaDriver extends AbstractModule {
    @Override
    def configure() {
        bind(Key.get(classOf[Object], classOf[BenchmarkDriver])).to(
                classOf[ScalaDriver]);
    }
    
    @BenchmarkOperation { val name = "Foo", val max90th = 1000000, val timing = Timing.MANUAL }
    @NegativeExponential { val cycleType = CycleType.THINKTIME, val cycleMean = 0, val cycleDeviation = 0.0 }
    def doFoo() {
        DriverContext.getContext().recordTime();
        for (i <- 0 until 100000) {
            val foo = Integer.toHexString(i);
        }
        DriverContext.getContext().recordTime();
    }
}

object ScalaRunner {
    def pipeOutput(process:Process) {
        pipe(process.getErrorStream(), System.err);
        pipe(process.getInputStream(), System.out);
    }

    def pipe(src:InputStream, dest:PrintStream) {
        new Thread(new Runnable() {
            def run() {
                try {
                    val buffer = new Array[byte](1024);
                    var n = 0;
                    while (n != -1) {
                        dest.write(buffer, 0, n);
                        n = src.read(buffer);
                    }
                } catch { // just exit
                case e:IOException => 0; 
                }
            }
        }).start();
    }

    def main(args: Array[String]) {
        val THREAD_PARAMS = List(1);
        // val THREAD_PARAMS = List(4, 4, 4, 4, 16, 16, 16, 16);
        // val THREAD_PARAMS = List(1, 1, 2, 4);
        // val THREAD_PARAMS = List(1, 1, 2, 4, 6, 8, 12, 16, 24, 32, 64);

        val THE_OPTS = new java.util.ArrayList[String]();
        THE_OPTS.add("-Xmx200m");
        THE_OPTS.add("-DoutputDir=/tmp/faban");
        THE_OPTS.add("-DdriverModule=ScalaDriver");
        THE_OPTS.add("-DrampUp=10");
        THE_OPTS.add("-DsteadyState=10");
        THE_OPTS.add("-DrampDown=10");

        THREAD_PARAMS.foreach((n:Int) => ({
            var cmd = new java.util.ArrayList[String]();
            cmd.add("java");
            cmd.addAll(THE_OPTS);
            cmd.add(String.format("-Dthreads=%s", Integer.toString(n)));
            cmd.add("com.sun.faban.driver.engine.GuiceMasterImpl");

            println(cmd.toString());
            val p = (new ProcessBuilder(cmd)).start();
            pipeOutput(p);
        }));
    }
}

