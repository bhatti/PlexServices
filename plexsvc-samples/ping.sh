echo curl http://localhost:8080/plexsvc-samples/ping?data=hello
echo curl http://localhost:8080/plexsvc-samples/reverse -d "data=hello"
echo curl -X POST http://localhost:8080/plexsvc-samples/person -d "{\"name\":\"me\"}"
if [ $# -eq 0 ];
then
./gradlew compileJava dist
else
./gradlew jettyRun
fi;
CLASSPATH=build/classes/main
for j in build/dist/lib/*jar 
do 
  CLASSPATH=$CLASSPATH:$j
done
export CLASSPATH 
#java -Dlog4j.configuration=log4j.xml com.plexobject.basic.Main ping.properties web 
java -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=9192 -Dlog4j.configuration=log4j.xml com.plexobject.basic.Main ping.properties web
  
    
#openssl genrsa -des3 -out server.key 1024
#cp server.key server.key.org
#openssl rsa -in server.key.org -out server.key
#openssl req -new -key server.key -out server.csr
#openssl x509 -req -days 365 -in server.csr -signkey server.key -out server.crt
