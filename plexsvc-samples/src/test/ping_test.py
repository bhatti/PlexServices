import unittest
import random
import base_auth_test
import websocket

#
### Functional test for ping service
#
class PingTest(base_auth_test.BaseAuthTest):
  def setUp(self):
    super(PingTest, self).setUp()

  def test_ping(self):
    resp = self.get('/ping?data=abc')
    self.assertTrue(resp.find('abc') > 0, resp)

  #def test_ping_ws(self):
  #  resp = self.ws_send_recv('{"data":"abc", "endpoint":"/ping"}')
  #  self.assertTrue(resp['payload'] == 'abc', resp)


if __name__ == '__main__':
  ## Invoking test suite ##
  suite = unittest.TestLoader().loadTestsFromTestCase(PingTest)
  unittest.TextTestRunner(verbosity=2).run(suite)

