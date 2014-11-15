Install python libraries:
    linux: sudo apt-get install python-pip python-dev build-essential 
    macos: curl -o get-pip.py https://bootstrap.pypa.io/get-pip.py && sudo python get-pip.py

    sudo pip install --upgrade pip 
    sudo pip install --upgrade virtualenv 
    sudo pip install websocket-client

Start server on your local environment

Then run following command to run all tests
    python all_suite_test.py

To run a single test, specify test name, e.g.
python bugs_test.py BugsTest.test_create_bug_report
python bugs_test.py BugsTest.test_list_project_bug_report
python bugs_test.py BugsTest.test_assign_bugreport

