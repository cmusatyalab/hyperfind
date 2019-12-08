
package edu.cmu.cs.diamond.hyperfind;
import edu.cmu.cs.diamond.hyperfind.ResultIcon.ResultType;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Writer;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import com.google.gson.*;
import edu.cmu.cs.diamond.opendiamond.*;

public class HistoryLogger {
    static private Gson gson;
    private final StatisticsArea statsArea;

    // current session
    private static int roundNum = -1;
    private final String historyFolder;

    // reset each time when start is pressed
    private static int imgCounter;

    public HistoryLogger(String historyFolder, Gson gson, StatisticsArea statsArea) {
        this.historyFolder = historyFolder;
        // make the mega session directory
        File historyDir = new File(historyFolder);
        assert(!historyDir.exists());
        assert(historyDir.getParent() != null);
        // should've been set as absolute by main already
        assert(historyDir.getAbsolutePath().equals(historyFolder));
        historyDir.mkdir();
        System.out.println("HistoryLogger activated: " + historyFolder);

        this.gson = gson;
        this.statsArea = statsArea;
    }

    /* Stores history data when session is starting, including
     * - start_time
     * - type
     * - exports predicates
     */
    public void historyLogSearchSessionStart(PredicateListModel model) {
        // update session number
        roundNum++;

        // make session directory
        String sessionDir = getSessionDir();
        File sessionDirFile = new File(sessionDir);
        assert(!sessionDirFile.exists());
        sessionDirFile.mkdir();

        // log start info
        String startInfoPath = joinPaths(sessionDir, "start_info.json");
        Map<String, String> startInfo = new HashMap<String, String>();
        startInfo.put("start_time(ms)", getCurrentTimeString());
        startInfo.put("session_type", "search");
        tryWriteNewFile(startInfoPath, gson.toJson(startInfo));

        // export predicate
        String predicateFile = joinPaths(sessionDir, "pred.hyperfindsearch");
        exportPredicatesToFile(model, predicateFile);

        // initialize image counter
        imgCounter = -1;
    }

    /* Logs session info to file system.
     */
    public void historyLogSearchSessionStop() {
        // log end info
        String endInfoPath = joinPaths(getSessionDir(), "end_info.json");
        Map<String, String> endInfo = new HashMap<String, String>();
        endInfo.put("end_time(ms)", getCurrentTimeString());
        tryWriteNewFile(endInfoPath, gson.toJson(endInfo));

        // log end stats
        String endStatsPath = joinPaths(getSessionDir(), "end_stats.json");
        //statsArea.getStatisticsMap().forEach((k,v) -> {
            //endInfo.put(k, Long.toString(v));
        //});
        tryWriteNewFile(endStatsPath, gson.toJson(statsArea.getStatisticsMap()));
    }

    /* Stores the result for one image
     *
     */
    public void historyLogResult(Result r) {
        imgCounter++;
        assert(roundNum >= 0 && imgCounter >= 0);
        String sessionDir = getSessionDir();
        File sessionDirFile = new File(sessionDir);
        assert(sessionDirFile.exists());

        // get/make directory for thumbnails
        String thumbnailDir = joinPaths(sessionDir, "thumbnail");
        File thumbnailDirFile = new File(thumbnailDir);
        if (!thumbnailDirFile.exists()) {
            thumbnailDirFile.mkdir();
        }

        Set<String> keys = r.getKeys();

        // save the thumbnail
        String thumbnailKey = "thumbnail.jpeg";
        String imgPath = joinPaths(thumbnailDir, Integer.toString(imgCounter) + ".jpeg");
        assert(keys.contains(thumbnailKey));
        tryWriteNewFile(imgPath, r.getValue(thumbnailKey));

        // get/make directory for attributes
        String attributesDir = joinPaths(sessionDir, "attributes");
        File attributesDirFile = new File(attributesDir);
        if (!attributesDirFile.exists()) {
            attributesDirFile.mkdir();
        }

        // reconstruct attributes
        Map<String, String> attributes = new HashMap<String, String>();
        for (String key : keys) {
            // don't include the thumbnail!
            if (key != thumbnailKey) {
                attributes.put(key, r.getStrValue(key));
            }
        }
        // add time receival time to attributes
        attributes.put("arrival_time(ms)", getCurrentTimeString());

        // save json
        String attributesPath = joinPaths(attributesDir, Integer.toString(imgCounter) + ".json");
        tryWriteNewFile(attributesPath, gson.toJson(attributes));

        String statsDir = joinPaths(sessionDir, "stats");
        File statsDirFile = new File(statsDir);
        if (!statsDirFile.exists()) {
            statsDirFile.mkdir();
        }

        // build json for the stats
        Map<String, Long> statisticsMap = statsArea.getStatisticsMap();
        String statsJsonPath = joinPaths(statsDir, Integer.toString(imgCounter) + ".json");
        tryWriteNewFile(statsJsonPath, gson.toJson(statisticsMap));
    }

    public void historyLogFeedback(Result r, ResultType cmd) {
        assert(roundNum >= 0);
        String sessionDir = getSessionDir();
        String feedbackPath = joinPaths(sessionDir, "feedback.csv");
        // if csv file doesn't yet exist (first feedback)
        if (!(new File(feedbackPath).exists())) {
            String header = "absolute time(ms),id,feedback_label\n";
            tryWriteNewFile(feedbackPath, header);
        }

        String id = Util.extractString(r.getValue("_ObjectID"));
        String feedback = feedback2str(cmd);
        String row = getCurrentTimeString() + "," + id + "," + feedback + "\n";
        tryAppendExistingFile(feedbackPath, row);
    }

    private String feedback2str(ResultType cmd) {
        if (cmd == ResultType.Ignore) {
            return "Ignore";
        } else if (cmd == ResultType.Positive) {
            return "Positive";
        } else {
            assert(cmd == ResultType.Negative);
            return "Negative";
        }
    }

    private void tryWriteNewFile(String path, String s) {
        // file must be fresh!
        assert(!((new File(path)).exists()));

        try {
            tryWriteNewFile(path, s.getBytes("utf-8"));
        } catch (UnsupportedEncodingException ex) {
            // this shouldn't happen
            ex.printStackTrace();
            assert(false);
        }
    }

    private void tryWriteNewFile(String path, byte[] bytes) {
        // file must be fresh!
        assert(!((new File(path)).exists()));
        try {
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(bytes);
            fos.close();
        } catch (FileNotFoundException ex) {
            // this shouldn't happen
            ex.printStackTrace();
            assert(false);
        } catch (IOException ex) {
            // if this happens, alert the user but don't need to crash
            ex.printStackTrace();
            return;
        }
    }


    private void tryAppendExistingFile(String path, String s) {
        assert((new File(path)).exists());
        try {
            // second parameter is for append
            FileOutputStream fos = new FileOutputStream(path, true);
            fos.write(s.getBytes("utf-8"));
            fos.close();
        } catch (FileNotFoundException ex) {
            // this shouldn't happen
            ex.printStackTrace();
            assert(false);
        } catch (UnsupportedEncodingException ex) {
            // this shouldn't happen
            ex.printStackTrace();
            assert(false);
        } catch (IOException ex) {
            // if this happens, alert the user but don't need to crash
            ex.printStackTrace();
            return;
        }
    }

    private String getSessionDir() {
        return joinPaths(historyFolder, Integer.toString(roundNum));
    }

    private String getCurrentTimeString() {
        return Long.toString(System.currentTimeMillis());
    }

    static public void exportPredicatesToFile(PredicateListModel model, String filename) {
        try {
            List<HyperFindPredicate> selectedPredicates = model.getSelectedPredicates();

            Writer writer = new FileWriter(filename);
            ArrayList<HyperFindPredicate.HyperFindPredicateState> states = new ArrayList<HyperFindPredicate.HyperFindPredicateState>();
            for (HyperFindPredicate pred : selectedPredicates) {
                states.add(pred.export());
            }

            writer.write(gson.toJson(states));
            writer.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    static public String joinPaths(String parentPath, String subdirPath) {
        // make sure that path2
        assert(!(new File(subdirPath)).getAbsolutePath().equals(subdirPath));
        return new File(parentPath, subdirPath).getPath();
    }
}

