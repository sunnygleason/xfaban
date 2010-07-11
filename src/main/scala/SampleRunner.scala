import java.io.InputStream;
import java.io.PrintStream;
import java.io.IOException;

object SampleRunner {
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
        THE_OPTS.add("-DdriverModule=SampleDriver$GuiceModule");
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

