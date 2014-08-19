#gradle distZip
gradle compileJava dist
#gradle run -Pargs="bugger.properties http_jms_services_mapping.json"
gradle bugger -Pargs="bugger.properties http_jms_services_mapping.json"
