import unittest 
import devices_test
import push_alerts_test

#
### This is helper class to run all test suites for notification
#
suite1 = unittest.TestLoader().loadTestsFromTestCase(bugs_test.BugsTest)
suite2 = unittest.TestLoader().loadTestsFromTestCase(ping_test.PingTest)
alltests = unittest.TestSuite([suite1, suite2])
unittest.TextTestRunner(verbosity=2).run(alltests)
