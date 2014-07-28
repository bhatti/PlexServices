curl -k -H "Content-Type: application/json" -X POST "http://localhost:8080/users" -d "{\"username\":\"david\",\"password\":\"pass\",\"email\":\"david@plexobject.com\",\"roles\":[\"Employee\"]}"
curl -k -H "Content-Type: application/json" "http://localhost:8080/users"  
