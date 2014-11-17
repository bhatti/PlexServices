import unittest
import requests
import json
from websocket import create_connection

#
### This is base test class that provides common HTTP methods for posting requests and authentication
#
class BaseAuthTest(unittest.TestCase):
  def authenticate(self, username='erica', password='pass'):
    payload = {'password': password, 'username': username}
    r = requests.post(self.base_ur_l + '/login', data=payload)
    self._cookie = r.headers['Set-Cookie']
    r.text

  # default setup method
  def setUp(self):
    self._cookie = ''
    self._base_url = 'http://localhost:8181'
    self._ws_url = 'ws://localhost:8181/ws'

  @property
  def cookie(self):
    return self._cookie

  def post(self, path, data):
    headers = {'Content-Type': 'application/json','cookie': self._cookie}
    #print headers
    r = requests.post(self._base_url + path, data=data, headers=headers)
    return json.loads(r.text)

  def get(self, path):
    headers = {'Content-Type': 'application/json','cookie': self._cookie}
    #print headers
    r = requests.get(self._base_url + path, headers=headers)
    try:
      return json.loads(r.text)
    except: 
      return r.text

  def ws_connect(self):
    # https://pypi.python.org/pypi/websocket-client/
    ws = create_connection(self._ws_url) 
    return ws

  def ws_send_recv(self, msg):
    ws = self.ws_connect()
    ws.send(msg)
    result =  ws.recv()
    try:
      result = json.loads(result)
    except: 
      pass
    ws.close()
    return result
