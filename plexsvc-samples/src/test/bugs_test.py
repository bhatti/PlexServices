import unittest
import random
import base_auth_test
import json

#
### Functional test for Bug Reports
#
class BugsTest(base_auth_test.BaseAuthTest):
  def setUp(self):
    super(BugsTest, self).setUp()

  def test_get_users_with_invalid_rule(self):
    self.authenticate('erica', 'pass')
    json_resp = self.get('/users')
    self.assertTrue(json_resp['status'] == 401, json_resp)

  def test_get_users_with_valid_rule(self):
    self.authenticate('scott', 'pass')
    json_resp = self.get('/users')
    self.assertTrue(len(json_resp) >= 0)
    for d in json_resp:
      self.assertTrue('username' in d)
      #for n,v in d.iteritems():
      #  print "name %s, value %s" % (n,v)

  def test_create_bug_report(self):
    self.authenticate('erica', 'pass')
    request = '{"title":"As an administrator, I would like to assign roles to users so that they can perform required actions.","description":"As an administrator, I would like to assign roles to users so that they can perform required actions.","bugNumber":"story-201","assignedTo":"mike","developedBy":"mike"}'
    json_resp = self.post('/projects/2/bugreports', request)
    self.assertTrue("createdAt" in json_resp, json_resp)
    self.assertTrue("bugNumber" in json_resp, json_resp)


  def test_update_bug_report(self):
    self.authenticate('erica', 'pass')
    request = '{"title":"As an awesome user I would like to login so that I can access Bugger System","assignedTo":"scott","developedBy":"erica"}'
    json_resp = self.post('/projects/2/bugreports/2', request)
    self.assertTrue("createdAt" in json_resp, json_resp)
    title = json_resp['title']
    self.assertTrue(title.find('As an awesome user') >= 0, title)

  def test_list_bug_report(self):
    self.authenticate('erica', 'pass')
    json_resp = self.get('/bugreports')
    for d in json_resp:
      self.assertTrue("createdAt" in d, d)

  def test_delete_user_with_invalid_role(self):
    self.authenticate('erica', 'pass')
    json_resp = self.post('/users/2/delete', '')
    self.assertTrue(json_resp['status'] == 401, json_resp)

  def test_create_delete_user_with_valid_role(self):
    self.authenticate('scott', 'pass')
    json_resp = self.post('/users', '{"username":"david","password":"pass","email":"david@plexobject.com","roles":["Employee"]}')
    uid = json_resp['id']
    json_resp = self.post('/users/%s/delete' % uid, '')
    self.assertTrue(json_resp['deleted'], json_resp)


  def test_assign_bugreport(self):
    self.authenticate('scott', 'pass')
    request = '{"title":"As an administrator, I would like to assign roles to users so that they can perform required actions.","description":"As an administrator, I would like to assign roles to users so that they can perform required actions.","bugNumber":"story-201","assignedTo":"mike","developedBy":"mike"}'
    json_resp = self.post('/projects/2/bugreports', request)
    bid = json_resp['id']
    json_resp = self.post('/projects/2/bugreports/%s/assign' % bid, 'assignedTo=scott')
    self.assertTrue(json_resp['assignedTo'] == 'scott', json_resp)


  def test_add_member_project(self):
    self.authenticate('scott', 'pass')
    json_resp = self.post('/projects/2/membership/add', 'projectLead=true&assignedTo=scott')
    self.assertTrue(json_resp['projectLead'] == 'scott', json_resp)


  def test_remove_member_project(self):
    self.authenticate('scott', 'pass')
    json_resp = self.post('/projects/2/membership/remove', 'projectLead=true&assignedTo=scott')
    self.assertTrue('projectLead' not in json_resp, json_resp)

  def test_list_project_bug_report(self):
    self.authenticate('erica', 'pass')
    json_resp = self.get('/projects/2/bugreports')
    for d in json_resp:
      self.assertTrue("createdAt" in d, d)

  def test_list_projects(self):
    self.authenticate('erica', 'pass')
    json_resp = self.get('/projects')
    for d in json_resp:
      self.assertTrue("createdAt" in d, d)

  def test_create_project(self):
    self.authenticate('scott', 'pass')
    json_resp = self.post('/projects', '{"title":"To do","description":"T  o do Desc","projectCode":"todo","projectLead":"erica","members":["alex"]}')
    self.assertTrue("createdAt" in json_resp, json_resp)

  def test_update_project(self):
    self.authenticate('scott', 'pass')
    json_resp = self.post('/projects/2', '"title":"Bugger cool","projectLead":"alex","members":["erica"]}')
    self.assertTrue("createdAt" in json_resp, json_resp)

if __name__ == '__main__':
  ## Invoking test suite ##
  unittest.main()



