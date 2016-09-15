echo curl -X POST http://localhost:8181/log -d "{\"level\":\"INFO\", \"message\":\"hello\", \"application\":\"test\"}"
echo curl -X POST http://localhost:8080/plexsvc-samples/log -d "{\"level\":\"INFO\", \"message\":\"hello\", \"application\":\"test\"}"
./gradlew log
#./gradlew compileJava dist
CLASSPATH=build/classes/main
for j in build/dist/lib/*jar 
do 
  CLASSPATH=$CLASSPATH:$j
done
export CLASSPATH 
#java -Dlog4j.configuration=log4j.xml com.plexobject.basic.LogService log.properties web 
