
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
    private final Gson gson;
    private final StatisticsArea statsArea;

    // current session
    private static int roundNum = -1;
    //public static final String HISTORY_PATH = "~/.diamond/history_logs/";
    public static final String HISTORY_PATH = "/home/kaiwenw/.diamond/history_logs/";
    private static Map<String, String> sessionInfo = new HashMap<String, String>();

    // reset each time when start is pressed
    private static int imgCounter;

    public HistoryLogger(Gson gson, StatisticsArea statsArea) {
        this.gson = gson;
        this.statsArea = statsArea;
    }

    private void createHistoryFolder() {
        File historyDir = new File(HISTORY_PATH);
        if (historyDir.exists()) {
            deleteDir(historyDir);
        }
        historyDir.mkdir();
    }

    /* Stores history data when session is starting, including
     * - start_time
     * - type
     * Also, exports predicates.
     */
    public void historyLogSearchSessionStart(PredicateListModel model) {
        // create history folder when first session starts
        if (roundNum == -1) {
            createHistoryFolder();
        }
        roundNum++;
        String sessionDir = getSessionDir();
        File sessionDirFile = new File(sessionDir);
        assert(!sessionDirFile.exists());
        sessionDirFile.mkdir();

        assert(sessionInfo.isEmpty());
        sessionInfo.put("start_time(ms)", getCurrentTimeString());
        sessionInfo.put("type", "search");

        String predicateFile = sessionDir + "pred.hyperfindsearch";
        exportPredicatesToFile(model, predicateFile);

        imgCounter = -1;
    }

    /* Logs session info to file system.
     */
    public void historyLogSearchSessionStop() {
        // write the info to file system and clear mapping for next session
        String infoPath = HISTORY_PATH + Integer.toString(roundNum) + "/info.json";
        sessionInfo.put("end_time(ms)", getCurrentTimeString());
        tryWriteNewFile(infoPath, gson.toJson(sessionInfo));
        sessionInfo.clear();


        String endStatsPath = HISTORY_PATH + Integer.toString(roundNum) + "/end_stats.json";
        Map<String, String> endStats = new HashMap<String, String>();
        statsArea.getStatisticsMap().forEach((k,v) -> {
            endStats.put(k, Long.toString(v));
        });

        tryWriteNewFile(endStatsPath, gson.toJson(endStats));
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
        String thumbnailDir = sessionDir + "thumbnail/";
        File thumbnailDirFile = new File(thumbnailDir);
        if (!thumbnailDirFile.exists()) {
            thumbnailDirFile.mkdir();
        }

        Set<String> keys = r.getKeys();

        // save the thumbnail
        String thumbnailKey = "thumbnail.jpeg";
        String imgPath = thumbnailDir + Integer.toString(imgCounter) + ".jpeg";
        assert(keys.contains(thumbnailKey));
        tryWriteNewFile(imgPath, r.getValue(thumbnailKey));

        // get/make directory for metadata
        String metadataDir = sessionDir + "metadata/";
        File metadataDirFile = new File(metadataDir);
        if (!metadataDirFile.exists()) {
            metadataDirFile.mkdir();
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
        String metadataPath = metadataDir + Integer.toString(imgCounter) + ".json";
        tryWriteNewFile(metadataPath, gson.toJson(attributes));

        String statsDir = sessionDir + "stats/";
        File statsDirFile = new File(statsDir);
        if (!statsDirFile.exists()) {
            statsDirFile.mkdir();
        }

        // build json for the stats
        Map<String, Long> statisticsMap = statsArea.getStatisticsMap();
        String statsJsonPath = statsDir + Integer.toString(imgCounter) + ".json";
        tryWriteNewFile(statsJsonPath, gson.toJson(statisticsMap));
    }

    public void historyLogFeedback(Result r, ResultType cmd) {
        assert(roundNum >= 0);
        String sessionDir = getSessionDir();
        String feedbackPath = sessionDir + "feedback.csv";
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
        return HISTORY_PATH + Integer.toString(roundNum) + "/";
    }

    private String getCurrentTimeString() {
        return Long.toString(System.currentTimeMillis());
    }

    /* Equivalent to rm -rf /path/to/file */
    private void deleteDir(File dir) {
    	assert(dir.exists() && dir.isDirectory());
        for (String child : dir.list()) {
            File childF = new File(dir, child);
            if (childF.isDirectory()) {
                deleteDir(childF);
            } else {
                childF.delete();
            }
        }
        dir.delete();
    }

    public void exportPredicatesToFile(PredicateListModel model, String filename) {
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
}

