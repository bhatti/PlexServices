from locust import HttpLocust, TaskSet, task

import datetime
import time
import string
import random

def ping(l):
  ln = random.randint(100,1000)
  data=''.join(random.choice(string.ascii_uppercase) for i in range(ln))
  response = l.client.get("/ping?data=%s"%data, headers={})

class PingTestTask(TaskSet):
  tasks = [ping]

class PingTestUser(HttpLocust):
  host = "http://127.0.0.1:8089"
  min_wait = 1000
  max_wait = 10000 
  task_set = PingTestTask 

