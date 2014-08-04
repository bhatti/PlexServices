#curl -k -H "Content-Type: application/json" "http://localhost:8080/users"   
#curl -k -H "Content-Type: application/json" -X POST "http://localhost:8080/projects/2/bugreports" -d "{\"title\":\"As an administrator, I would like to assign roles to users so that they can perform required actions.\",\"description\":\"As an administrator, I would like to assign roles to users so that they can perform required actions.\",\"bugNumber\":\"story-201\",\"assignedTo\":\"mike\",\"developedBy\":\"mike\"}"
#curl -k -H "Content-Type: application/json" "http://localhost:8080/bugreports"   
#curl -k -H "Content-Type: application/json" -X POST "http://localhost:8080/projects/2/bugreports/2" -d "{\"title\":\"As an awesome  user I would like to login so that I can access Bugger System\",\"assignedTo\":\"scott\",\"developedBy\":\"erica\"}"
#curl -k -H "Content-Type: application/json" -X POST "http://localhost:8080/users/2/delete" 
#curl -k -H "Content-Type: application/json" -X POST "http://localhost:8080/projects/2/bugreports/2/assign?assignedTo=scott"
#curl -k -H "Content-Type: application/json" "http://localhost:8080/bugreports"   
#curl -k -H "Content-Type: application/json" "http://localhost:8080/projects/2/bugreports"   
#curl -k -H "Content-Type: application/json" "http://localhost:8080/projects"   
#curl -k -H "Content-Type: application/json" -X POST "http://localhost:8080/projects/2/membership/remove?projectLead=true&assignedTo=scott"
#curl -k -H "Content-Type: application/json" -X POST "http://localhost:8080/projects/2/membership/add?projectLead=true&assignedTo=scott"
#curl -k -H "Content-Type: application/json" -X POST "http://localhost:8080/projects/2" -d "{\"title\":\"Bugger cool\",\"projectLead\":\"alex\",\"members\":[\"erica\"]}"
#curl -k -H "Content-Type: application/json" -X POST "http://localhost:8080/projects" -d "{\"title\":\"To do\",\"description\":\"To do Desc\",\"projectCode\":\"todo\",\"projectLead\":\"erica\",\"members\":[\"alex\"]}"
#curl -k -H "Content-Type: application/json" "http://localhost:8080/projects"   
#curl -k -H "Content-Type: application/json" -X POST "http://localhost:8080/users" -d "{\"username\":\"david\",\"password\":\"pass\",\"email\":\"david@plexobject.com\",\"roles\":[\"Employee\"]}"
curl -k -H "Content-Type: application/json" "http://localhost:8080/users"   
exit








