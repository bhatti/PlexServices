import unittest
import random
import base_auth_test
 
#
### Functional test for ping service
#
class PingTest(base_auth_test.BaseAuthTest):
    def setUp(self):
        super(PingTest, self).setUp()

    def test_ping(self):
        json_resp = self.get('/ping?data=abc')
        print json_resp
        #self.assertTrue(response_xml.find('<symbol>AAPL</symbol') > 0, response_xml)


if __name__ == '__main__':
    ## Invoking test suite ##
    suite = unittest.TestLoader().loadTestsFromTestCase(PingTest)
    unittest.TextTestRunner(verbosity=2).run(suite)

