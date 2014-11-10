#./gradlew distZip
./gradlew compileJava dist
#./gradlew run -Pargs="bugger.properties http_jms_services_mapping.json"
./gradlew bugger -Pargs="bugger.properties http_jms_services_mapping.json"
