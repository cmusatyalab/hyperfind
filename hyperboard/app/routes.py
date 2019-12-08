import copy
import os, json
from collections import OrderedDict
from flask import redirect, request, render_template, send_from_directory, jsonify
from app import app
from app.logs import *

app.config["LOG_FOLDER"] = None
app.config["CURRENT_SESSION_NAME"] = None

################################
######## SESSION STATES ########
################################

def invalid_root():
    if not os.path.exists(app.config["ROOT_FOLDER"]):
        print("%s doesn't exist. " % app.config["ROOT_FOLDER"])
        return True
    if not os.path.isdir(app.config["ROOT_FOLDER"]):
        print("%s exists but is not a directory. " % app.config["ROOT_FOLDER"])
        return True
    return False

def reset_states():
    app.config["LOG_FOLDER"] = None
    app.config["CURRENT_SESSION_NAME"] = None


def inconsistent_state():
    if app.config["LOG_FOLDER"] is None:
        print("log folder is none!")
        return True
    if app.config["CURRENT_SESSION_NAME"] is None:
        print("current session name is none!")
        return True
    if not os.path.isdir(app.config["LOG_FOLDER"]):
        print("log folder %s is not a directory!" % app.config["LOG_FOLDER"])
        return True
    return False

@app.route("/update_log_folder/<path:session_name>")
def update_log_folder(session_name):
    app.config["CURRENT_SESSION_NAME"] = session_name
    app.config["LOG_FOLDER"] = os.path.join(app.config["ROOT_FOLDER"], session_name)
    print("Updated with ", session_name, " log folder", app.config["LOG_FOLDER"])
    return homepage()


#################################
########### PLOT ################
#################################


def get_stat_series(rows):
    """Helper that formats statistics into a list-form for D3 plot to consume"""
    assert rows is not None
    stat_series = []
    # at the end of each row, add a zero point
    for row in rows:
        if "per_img" in row:
            stat_series.extend(row["per_img"])
        if "end_stats" in row:
            # if this exists, add it to the end
            end_stats = row["end_stats"]
            stat_series.append(end_stats)

            # also, add a zero point
            zero_entry = copy.deepcopy(end_stats)
            for k in zero_entry["derived_stats"]:
                zero_entry["derived_stats"][k] = 0
            stat_series.append(zero_entry)
    return stat_series


@app.route("/refresh_plot")
def refresh_plot():
    """Periodically, makes AJAX request to get new updated plot data"""
    stat_series = {}
    if app.config["LOG_FOLDER"] is None or not os.path.isdir(app.config["LOG_FOLDER"]):
        return jsonify({})
    rows = process_data(app.config["LOG_FOLDER"])
    if rows is None:
        print(
            "invalid results from process data of log folder: ",
            app.config["LOG_FOLDER"],
        )
        return jsonify({})
    stat_series = get_stat_series(rows)
    print("Refreshing plots with " + str(len(stat_series)))
    return jsonify(stat_series)


######################################
############# MAIN PAGE ##############
######################################

# download the predicate file
@app.route("/download_predicate/<int:sess_num>")
def download_predicate(sess_num):
    """Serves download predicate request"""
    path = "%d/pred.hyperfindsearch" % sess_num
    return send_from_directory(app.config["LOG_FOLDER"], path)


@app.route("/")
def homepage():
    """Loads the main page, with tables and plots, and buttons to replay"""

    # invalid root folder
    if invalid_root():
        return render_template("index.html")

    candidates = get_valid_search_paths(app.config["ROOT_FOLDER"])
    # nothing to show, since no valid candidate folder
    if len(candidates) == 0:
        print("No valid search paths with root folder %s" % app.config["ROOT_FOLDER"])
        return render_template("index.html")

    # make current state consistent (can become inconsistent after this, but let's assume not)
    if inconsistent_state() or app.config["CURRENT_SESSION_NAME"] not in candidates:
        update_log_folder(candidates[0])

    rows = process_data(app.config["LOG_FOLDER"])
    # state is consistent, but folder is malformed :(
    if rows is None:
        print("invalid rows from log folder: ", app.config["LOG_FOLDER"])
        return render_template(
                "index.html",
                session_name=app.config["CURRENT_SESSION_NAME"],
                candidates=candidates,
            )

    def get_stat_keys(rows):
        """Tries to find one row with valid stat keys"""
        for row in rows:
            if "per_img" in row:
                return row["per_img"][0]["derived_stats"].keys()
        return None

    stat_keys = get_stat_keys(rows)
    if stat_keys is None:
        print("No per image data yet!")
        return render_template(
            "index.html",
            session_name=app.config["CURRENT_SESSION_NAME"],
            candidates=candidates,
            rows=rows,
        )

    return render_template(
        "index.html",
        session_name=app.config["CURRENT_SESSION_NAME"],
        candidates=candidates,
        rows=rows,
        stat_keys=stat_keys,
    )

####################################
########## REPLAY WINDOW ###########
####################################


@app.route("/replay/<int:sess_num>")
def replay_homepage(sess_num):
    """Homepage for replay, which sets up the <img> tags to make
    GET request to replay_img, and also passes the labels.
    Note, we assume that in between this function call and the GET requests,
    that the state remains consistent. This may not be the case, as the user
    can maliciously delete files in this time frame. We assume this isn't the case.
    """
    if invalid_root() or inconsistent_state():
        print("inconsistent state, returning to homepage")
        return homepage()

    rows = process_data(app.config["LOG_FOLDER"])
    if "per_img" not in rows[sess_num]:
        return render_template("replay.html")

    return render_template(
        "replay.html",
        per_img=rows[sess_num]["per_img"],
        pos_ids=rows[sess_num]["positive_ids"],
        neg_ids=rows[sess_num]["negative_ids"],
    )


@app.route("/replay_img/<path:filename>")
def replay_img(filename):
    """Serves GET request in replay, sending actual thumbnail contents"""
    return send_from_directory(app.config["LOG_FOLDER"], filename)

