./gradlew compileJava dist
CLASSPATH=build/classes/main
for j in build/dist/lib/*jar 
do 
  CLASSPATH=$CLASSPATH:$j
done
export CLASSPATH 
java -Dlog4j.configuration=log4j.xml com.plexobject.ping.Main ping.properties web 
