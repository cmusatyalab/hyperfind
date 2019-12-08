from flask import Flask

app = Flask(__name__)

def print_usage():
    print("usage: ./hyperboard logdir pwd")

import sys, os
pwd_key = "HYPERBOARD_PWD"
logdir_key = "HYPERBOARD_LOGDIR"
assert(logdir_key in os.environ)
assert(pwd_key in os.environ)
pwd = os.environ[pwd_key]
logdir = os.environ[logdir_key]
if pwd == '' or logdir == '':
    print("Input pwd = %s, logdir = %s" % (pwd, logdir))
    print_usage()
    os._exit(-1)

# make logdir absolute path by using pwd
assert(os.path.isabs(pwd))
if not os.path.isabs(logdir):
    logdir = os.path.join(pwd, logdir)
app.config["ROOT_FOLDER"] = logdir

print("Hyperboard using as root folder ", app.config["ROOT_FOLDER"])

from app import routes
from app import process_logs
from app import predicate
from app import logs_helpers
