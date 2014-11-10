./gradlew compileJava dist
CLASSPATH=build/classes/main
for j in build/dist/lib/*jar 
do 
  CLASSPATH=$CLASSPATH:$j
done
export CLASSPATH 
java com.plexobject.bridge.HttpToJmsBridge bugger.properties http_jms_services_mapping.json
