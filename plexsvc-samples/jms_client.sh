gradle compileJava dist
CLASSPATH=build/classes/main
for j in build/dist/lib/*jar 
do 
  CLASSPATH=$CLASSPATH:$j
done
export CLASSPATH 
#AddProjectMemberService queue:bhatti-add-project-member-service-queue
#AssignBugReportService queue:bhatti-assign-bugreport-service-queue
#CreateBugReportService queue:bhatti-create-bugreport-service-queue
#CreateProjectService queue:bhatti-create-projects-service-queue
#CreateUserService queue:bhatti-create-user-service-queue
#DeleteUserService queue:bhatti-delete-user-service-queue
#QueryBugReportService queue:bhatti-bugreports-service-queue
#QueryProjectBugReportService queue:bhatti-query-project-bugreport-service-queue
#QueryProjectService queue:bhatti-query-projects-service
#QueryUserService queue:bhatti-query-user-service-queue
#RemoveProjectMemberService queue:bhatti-remove-project-member-service-queue
#UpdateBugReportService queue:bhatti-update-bugreport-service-queue
#UpdateProjectService queue:bhatti-update-project-service-queue
#UpdateUserService queue:bhatti-update-user-service-queue 

#java com.plexobject.bugger.JmsSender bugger.properties queue:bhatti-query-projects-service
#java com.plexobject.bugger.JmsSender bugger.properties queue:bhatti-create-bugreport-service-queue "{\"projectId\":2,\"title\":\"As an administrator, I would like to assign roles to users so that they can perform required actions.\",\"description\":\"As an administrator, I would like to assign roles to users so that they can perform required actions.\",\"bugNumber\":\"story-201\",\"assignedTo\":\"mike\",\"developedBy\":\"mike\"}"
#java com.plexobject.bugger.JmsSender bugger.properties queue:bhatti-update-bugreport-service-queue "{\"projectId\":2, \"id\":2, \"title\":\"As an awesome  user I would like to login so that I can access Bugger System\",\"assignedTo\":\"scott\",\"developedBy\":\"erica\"}"

#java com.plexobject.bugger.JmsSender bugger.properties queue:bhatti-bugreports-service-queue
#java com.plexobject.bugger.JmsSender bugger.properties queue:bhatti-query-user-service-queue
#java com.plexobject.bugger.JmsSender bugger.properties queue:bhatti-delete-user-service-queue id=2 
#java com.plexobject.bugger.JmsSender bugger.properties queue:bhatti-assign-bugreport-service-queue projectId=2 id=2 assignedTo=scott

#java com.plexobject.bugger.JmsSender bugger.properties queue:bhatti-query-project-bugreport-service-queue projectId=2

#java com.plexobject.bugger.JmsSender bugger.properties queue:bhatti-query-projects-service
#java com.plexobject.bugger.JmsSender bugger.properties queue:bhatti-add-project-member-service-queue projectLead=true assignedTo=scott id=2
#java com.plexobject.bugger.JmsSender bugger.properties queue:bhatti-update-project-service-queue "{\"id\":2, \"title\":\"Bugger cool\",\"projectLead\":\"alex\",\"members\":[\"erica\"]}"
#java com.plexobject.bugger.JmsSender bugger.properties queue:bhatti-create-projects-service-queue "{\"title\":\"To do\",\"description\":\"To do Desc\",\"projectCode\":\"todo\",\"projectLead\":\"erica\",\"members\":[\"alex\"]}"
#java com.plexobject.bugger.JmsSender bugger.properties queue:bhatti-query-projects-service
#java com.plexobject.bugger.JmsSender bugger.properties queue:bhatti-create-user-service-queue "{\"username\":\"david\",\"password\":\"pass\",\"email\":\"david@plexobject.com\",\"roles\":[\"Employee\"]}"
java com.plexobject.bugger.JmsSender bugger.properties queue:bhatti-query-user-service-queue
exit

