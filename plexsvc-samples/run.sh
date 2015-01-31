#keytool -genkey -keyalg RSA -alias selfsigned -keystore keystore.jks -storepass password -validity 1360 -keysize 2048
#./gradlew distZip 
./gradlew jettyRun
./gradlew compileJava dist
#./gradlew run -Pargs="bugger.properties http_jms_services_mapping.json" 
#curl -H "Accept: application/json" http://localhost:8080/plexsvc-samples/array
./gradlew bugger -Pargs="bugger.properties http_jms_services_mapping.json"
