from flask import Flask

app = Flask(__name__)

import sys, os
logdir_key = "HYPERBOARD_LOGDIR"
if logdir_key in os.environ:
    app.config["ROOT_FOLDER"] = os.environ[logdir_key]
    if app.config["ROOT_FOLDER"] == '':
        print("require logdir as an argument!")
        os._exit(-1)

    # make absolute path
    if not os.path.isabs(app.config["ROOT_FOLDER"]):
        # the ../ is to take into account the hyperboard folder, since we
        # assume the caller is using hyperfind's script
        app.config["ROOT_FOLDER"] = os.path.abspath("../" + app.config["ROOT_FOLDER"])
    print("Hyperboard using as root folder ", app.config["ROOT_FOLDER"])
else:
    print("require a valid root folder!")
    os._exit(-1)

from app import routes
from app import logs

