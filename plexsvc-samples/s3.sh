gradle compileJava dist
CLASSPATH=build/classes/main
for j in build/dist/lib/*jar 
do 
  CLASSPATH=$CLASSPATH:$j
done
export CLASSPATH 
echo after starting this server, open browser with URL http://localhost:8181/static/s3upload.html
java com.plexobject.s3.Main s3.properties 
