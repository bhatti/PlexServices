Install python libraries:
    sudo apt-get install python-pip python-dev build-essential 
    sudo pip install --upgrade pip 
    sudo pip install --upgrade virtualenv 

Start jboss server on your local environment

Then run following command to run all tests
    python all_suite_test.py

If you see "User Session Does Not Exist" errors, then just rerun the tests 

To run a single test, specify test name, e.g.
python bugs_test.py BugsTest.test_create_bug_report
python bugs_test.py BugsTest.test_list_project_bug_report
python bugs_test.py BugsTest.test_assign_bugreport

