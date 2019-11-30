
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
    private static String logFolder = System.getProperty("user.home") + "/.diamond/history_logs/";
    private String historyFolder;

    // reset each time when start is pressed
    private static int imgCounter;

    public HistoryLogger(Gson gson, StatisticsArea statsArea) {
        this.gson = gson;
        this.statsArea = statsArea;
    }

    private void createHistoryFolder() {
        File historyDir = new File(historyFolder);
        if (historyDir.exists()) {
            deleteDir(historyDir);
        }
        historyDir.mkdirs();
    }

    public void updateSessionName(String name) {
        assert(name.length() > 0);
        // TODO: shift these constraints to the config window itself
        // doesn't support names with . (to prevent malicious paths)
        if (name.contains(".")) {
            System.out.println("Invalid name " + name + " containing '.'");
        }
        assert(!name.contains("."));
        String newHistoryFolder = logFolder + name + "/";
        if (roundNum >= 0) {
            assert(newHistoryFolder.equals(historyFolder));
        } else {
            historyFolder = newHistoryFolder;
        }
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

        String startInfoPath = historyFolder + Integer.toString(roundNum) + "/start_info.json";
        Map<String, String> startInfo = new HashMap<String, String>();
        startInfo.put("start_time(ms)", getCurrentTimeString());
        startInfo.put("session_type", "search");
        tryWriteNewFile(startInfoPath, gson.toJson(startInfo));

        String predicateFile = sessionDir + "pred.hyperfindsearch";
        exportPredicatesToFile(model, predicateFile);

        // initialize image counter
        imgCounter = -1;
    }

    /* Logs session info to file system.
     */
    public void historyLogSearchSessionStop() {
        // write the info to file system and clear mapping for next session
        String endInfoPath = historyFolder + Integer.toString(roundNum) + "/end_info.json";
        Map<String, String> endInfo = new HashMap<String, String>();
        endInfo.put("end_time(ms)", getCurrentTimeString());
        tryWriteNewFile(endInfoPath, gson.toJson(endInfo));

        statsArea.getStatisticsMap().forEach((k,v) -> {
            endInfo.put(k, Long.toString(v));
        });

        tryWriteNewFile(endInfoPath, gson.toJson(endInfo));
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
        return historyFolder + Integer.toString(roundNum) + "/";
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

