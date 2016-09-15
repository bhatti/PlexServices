#export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_05.jdk/Contents/Home
export JAVA_HOME=/Library//Java/JavaVirtualMachines/jdk1.8.0_101.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
#export JAVA_OPTS="-server -XX:BCEATraceLevel=3 -XX:+PrintCompilation -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining -verbose:gc -XX:MaxInlineSize=256 -XX:FreqInlineSize=1024 -XX:MaxBCEAEstimateSize=1024 -XX:MaxInlineLevel=22 -XX:CompileThreshold=10 -Xmx4g -Xms4g"
#JAVA_OPTS="-Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=true -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.password.file=/et/jmxremote.password"
#export JAVA_OPTS="-server -Xms20G -Xmx20G -XX:+UseG1GC -XX:PermSize=1024m -XX:MaxPermSize=1024m -Xss512k -XX:+PrintClassHistogram -XX:+DisableExplicitGC -Xnoclassgc -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:/tmp/gc.log -XX:MaxGCPauseMillis=400 -XX:G1HeapRegionSize=4M"
export JAVA_OPTS="-server -verbose:gc -Xmx4g -Xms4g  -XX:+UseG1GC -XX:PermSize=1024m -XX:MaxPermSize=1024m -Xss512k -XX:+PrintClassHistogram -XX:+DisableExplicitGC -Xnoclassgc -XX:+PrintGCDateStamps -Xloggc:/tmp/gc.log"
./gradlew compileJava
./gradlew order
