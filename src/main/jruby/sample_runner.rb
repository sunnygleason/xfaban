
CLASSPATH=ENV["CLASSPATH"]

OPTS = [
  "-Xmx200m",
  "-DoutputDir=/tmp/faban",
  "-DdriverModule=SampleDriver\\$GuiceModule",
  "-DrampUp=10",
  "-DsteadyState=10",
  "-DrampDown=10",
].join(" ")


THREAD_PARAMS = [1]

# THREAD_PARAMS = [4, 4, 4, 4, 16, 16, 16, 16]

# THREAD_PARAMS = [1, 1, 2, 4]

# THREAD_PARAMS = [1, 1, 2, 4, 6, 8, 12, 16, 24, 32, 64]

THREAD_PARAMS.each do |n|
  cmd = "java -cp \"#{CLASSPATH}\" #{OPTS} -Dthreads=#{n} com.sun.faban.driver.engine.GuiceMasterImpl"
  puts cmd
  `#{cmd}`
end

