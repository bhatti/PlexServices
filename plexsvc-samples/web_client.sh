#curl --cookie cookies.txt -k -H "Content-Type: application/json" "http://127.0.0.1:8181/users"   
#curl --cookie cookies.txt -k -H "Content-Type: application/json" -X POST "http://127.0.0.1:8181/projects/2/bugreports" -d "{\"title\":\"As an administrator, I would like to assign roles to users so that they can perform required actions.\",\"description\":\"As an administrator, I would like to assign roles to users so that they can perform required actions.\",\"bugNumber\":\"story-201\",\"assignedTo\":\"mike\",\"developedBy\":\"mike\"}"
#curl --cookie cookies.txt -k -H "Content-Type: application/json" "http://127.0.0.1:8181/bugreports"   
#curl --cookie cookies.txt -k -H "Content-Type: application/json" -X POST "http://127.0.0.1:8181/projects/2/bugreports/2" -d "{\"title\":\"As an awesome  user I would like to login so that I can access Bugger System\",\"assignedTo\":\"scott\",\"developedBy\":\"erica\"}"
#curl --cookie cookies.txt -k -H "Content-Type: application/json" -X POST "http://127.0.0.1:8181/users/2/delete" 
#curl --cookie cookies.txt -k -H "Content-Type: application/json" -X POST "http://127.0.0.1:8181/projects/2/bugreports/2/assign?assignedTo=scott"
#curl --cookie cookies.txt -k -H "Content-Type: application/json" "http://127.0.0.1:8181/bugreports"   
#curl --cookie cookies.txt -k -H "Content-Type: application/json" "http://127.0.0.1:8181/projects/2/bugreports"   
#curl --cookie cookies.txt -k -H "Content-Type: application/json" "http://127.0.0.1:8181/projects"   
#curl --cookie cookies.txt -k -H "Content-Type: application/json" -X POST "http://127.0.0.1:8181/projects/2/membership/remove?projectLead=true&assignedTo=scott"
#curl --cookie cookies.txt -k -H "Content-Type: application/json" -X POST "http://127.0.0.1:8181/projects/2/membership/add?projectLead=true&assignedTo=scott"
#curl --cookie cookies.txt -k -H "Content-Type: application/json" -X POST "http://127.0.0.1:8181/projects/2" -d "{\"title\":\"Bugger cool\",\"projectLead\":\"alex\",\"members\":[\"erica\"]}"
#curl --cookie cookies.txt -k -H "Content-Type: application/json" -X POST "http://127.0.0.1:8181/projects" -d "{\"title\":\"To do\",\"description\":\"To do Desc\",\"projectCode\":\"todo\",\"projectLead\":\"erica\",\"members\":[\"alex\"]}"
#curl --cookie cookies.txt -k -H "Content-Type: application/json" "http://127.0.0.1:8181/projects"   
#curl --cookie cookies.txt -k -H "Content-Type: application/json" -X POST "http://127.0.0.1:8181/users" -d "{\"username\":\"david\",\"password\":\"pass\",\"email\":\"david@plexobject.com\",\"roles\":[\"Employee\"]}"
#curl --cookie cookies.txt -k -H "Content-Type: application/json" "http://127.0.0.1:8181/users"   
curl --cookie-jar cookies.txt -v -k -H "Content-Type: application/json" -X POST "http://127.0.0.1:8181/login?username=scott&password=pass"
curl -v --cookie cookies.txt -k -H "Content-Type: application/json" "http://127.0.0.1:8181/users"   
#curl --cookie cookies.txt -v -k -H "Content-Type: application/json" -X POST "http://127.0.0.1:8181/login?username=scott&password=xxx"
exit








