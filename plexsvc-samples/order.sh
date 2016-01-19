export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_05.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
#export JAVA_OPTS="-server -XX:BCEATraceLevel=3 -XX:+PrintCompilation -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining -verbose:gc -XX:MaxInlineSize=256 -XX:FreqInlineSize=1024 -XX:MaxBCEAEstimateSize=1024 -XX:MaxInlineLevel=22 -XX:CompileThreshold=10 -Xmx4g -Xms4g"
export JAVA_OPTS="-server -verbose:gc -Xmx4g -Xms4g"
./gradlew compileJava
./gradlew order
