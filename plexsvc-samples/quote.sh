#./gradlew distZip
./gradlew compileJava dist
CLASSPATH=build/classes/main
for j in build/dist/lib/*jar 
do 
  CLASSPATH=$CLASSPATH:$j
done
export CLASSPATH 

#java -cp $CLASSPATH com.plexobject.deploy.AutoDeployer quote.properties
java -cp $CLASSPATH com.plexobject.stock.QuoteServer quote.properties
