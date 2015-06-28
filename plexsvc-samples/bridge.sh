./gradlew compileJava dist
CLASSPATH=build/classes/main
for j in build/dist/lib/*jar 
do 
  CLASSPATH=$CLASSPATH:$j
done
export CLASSPATH 
java -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=9191 com.plexobject.bugger.Main bugger.properties src/test/resources/http_jms_services_mapping.json

