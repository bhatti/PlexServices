import unittest
import random
import string
import base_auth_test
from websocket import create_connection

#
### Functional test for ping service
#
class PingTest(base_auth_test.BaseAuthTest):
  def setUp(self):
    super(PingTest, self).setUp()
    #self._base_url = 'https://localhost:8181'
    #self._ws_url = 'wss://localhost:8181/ws'

  def test_ping(self):
    resp = self.get('/ping?data=abc')
    self.assertTrue(resp.find('abc') >= 0, resp)

  def test_ping_ws(self):
    resp = self.ws_send_recv('{"data":"abc", "endpoint":"/ping"}')
    self.assertTrue(resp['payload'] == 'abc', resp)

  def test_ping_ws_loop(self):
    return
    ws = self.ws_connect()
    for i in range(10):
      ln = random.randint(100,1000)
      data=''.join(random.choice(string.ascii_uppercase) for i in range(ln))
      req = '{"data":"%s", "endpoint":"/ping"}' % data
      ws.send(req)
      result =  ws.recv()
      try:
        result = json.loads(result)
      except: 
        pass
    ws.close()

if __name__ == '__main__':
  ## Invoking test suite ##
  #suite = unittest.TestLoader().loadTestsFromTestCase(PingTest)
  #unittest.TextTestRunner(verbosity=2).run(suite)
  unittest.main()

