echo curl -X POST http://localhost:8181/mail -d "methodName=sendEmailx&from=bhatti_plexobject.com&to=support_plexobject.com&subject=test&text=test text"
echo curl -H "Content-Type: application/json" --data @mail.json http://localhost:8181/mail
#echo curl -X POST http://localhost:8080/plexsvc-samples/mail -d 
./gradlew mail
#./gradlew compileJava dist
CLASSPATH=build/classes/main
for j in build/dist/lib/*jar 
do 
  CLASSPATH=$CLASSPATH:$j
done
export CLASSPATH 
#java -Dlog4j.configuration=log4j.xml com.plexobject.bugger.mail.Main mail.properties
