from locust import HttpLocust, TaskSet, task

import datetime
import time

session_id = 4

def login(l):
  response = l.client.post("/login", {"username":"scott", "password":"pass"}) 
  if len(response.headers) > 0:
    session_id = response.headers['PlexSessionId']
  else:
    print "failed to login %s" % (response,) 

def assign_bugreport(l):
  l.client.post("/projects/2/bugreports/2/assign", {"assignedTo":"scott"}, headers={"PlexSessionId":session_id}) 

def query_users(l):
  response = l.client.get("/users", headers={"PlexSessionId":session_id})
  #print "query_users %s" % (response,) 

def query_bugreports(l):
  response = l.client.get("/bugreports", headers={"PlexSessionId":session_id})
  #print "query_users %s" % (response,) 

def query_project_bugreports(l):
  response = l.client.get("/projects/2/bugreports", headers={"PlexSessionId":session_id})
  #print "query_users %s" % (response,) 

def query_projects(l):
  response = l.client.get("/projects", headers={"PlexSessionId":session_id})
  #print "query_users %s" % (response,) 

def add_project(l):
  response = l.client.post("/projects/2/bugreports", "{\"title\":\"As an administrator, I would like to assign roles to users so that they can perform required actions.\",\"description\":\"As an administrator, I would like to assign roles to users so that they can perform required actions.\",\"bugNumber\":\"story-201\",\"assignedTo\":\"mike\",\"developedBy\":\"mike\"}", headers={"PlexSessionId":session_id})

def update_project(l):
  response = l.client.post("/projects/2/bugreports/2", "{\"title\":\"As an awesome  user I would like to login so that I can access Bugger System\",\"assignedTo\":\"scott\",\"developedBy\":\"erica\"}", headers={"PlexSessionId":session_id})

def add_user(l):
  #response = l.client.post("/users", "{\"username\":\"david\",\"password\":\"pass\",\"email\":\"david@plexobject.com\",\"roles\":[\"Employee\"]}", headers={"PlexSessionId":session_id})
  None

def delete_user(l):
  response = l.client.post("/users/3/delete", headers={"PlexSessionId":session_id})

def add_member(l):
  response = l.client.post("/projects/2/membership/add", {"projectLead":"true", "assignedTo":"scott"}, headers={"PlexSessionId":session_id}) 

def remove_member(l):
  response = l.client.post("/projects/2/membership/remove", {"projectLead":"true", "assignedTo":"scott"}, headers={"PlexSessionId":session_id}) 


class LoadTestTask(TaskSet):
  #tasks = [login, query_users, add_user, delete_user, add_project, update_project, query_projects, query_project_bugreports, query_bugreports, assign_bugreport, add_member, remove_member]
  tasks = [login]
#  @task
#  def page404(self):
#    self.client.get("/does_not_exist") 

class LoadTestUser(HttpLocust):
  host = "http://127.0.0.1:8089"
  min_wait = 1000
  max_wait = 10000 
  task_set = LoadTestTask 


