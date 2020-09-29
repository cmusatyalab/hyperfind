/*
 *  HyperFind, a search application for the OpenDiamond platform
 *
 *  Copyright (c) 2008-2010 Carnegie Mellon University
 *  All rights reserved.
 *
 *  HyperFind is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, version 2.
 *
 *  HyperFind is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with HyperFind. If not, see <http://www.gnu.org/licenses/>.
 *
 *  Linking HyperFind statically or dynamically with other modules is
 *  making a combined work based on HyperFind. Thus, the terms and
 *  conditions of the GNU General Public License cover the whole
 *  combination.
 *
 *  In addition, as a special exception, the copyright holders of
 *  HyperFind give you permission to combine HyperFind with free software
 *  programs or libraries that are released under the GNU LGPL, the
 *  Eclipse Public License 1.0, or the Apache License 2.0. You may copy and
 *  distribute such a system following the terms of the GNU GPL for
 *  HyperFind and the licenses of the other code concerned, provided that
 *  you include the source code of that other code when and as the GNU GPL
 *  requires distribution of source code.
 *
 *  Note that people who make modified versions of HyperFind are not
 *  obligated to grant this special exception for their modified versions;
 *  it is their choice whether to do so. The GNU General Public License
 *  gives permission to release a modified version without this exception;
 *  this exception also makes it possible to release a modified version
 *  which carries forward this exception.
 */

package edu.cmu.cs.diamond.hyperfind;

import edu.cmu.cs.diamond.hyperfind.connection.api.SearchStats;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Timer;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

final class StatisticsArea extends JPanel {

    private static final int PREFERRED_WIDTH = 300;
    private static final int MINIMUM_HEIGHT = 200;

    private final JTextArea display = new JTextArea();

    private SearchStats prevStats = null;

    private DisplayStats prevDisplayStats = null;

    private String currentString;

    private List<String> csvStats;

    private List<String> timeStats;

    private long prevDisplayed = 0;

    private long prevTp = 0;

    private float prevPrecision = 0;

    private long prevVersion = 0;

    private long currDisplayed = 0;

    private long currDisplayedPositives = 0;

    private float avgPrecision = 0;

    private long diffDisplayed = 0;

    private long diffTp = 0;

    private boolean timerStart = false;

    public class DisplayStats
    {
        private Boolean statsDone = false;
        public long displayed;
        public long displayedPositives;
        public long userPositives;
        public long userNegatives;
        public String timeElapsed;

        public DisplayStats(long displayed, long tp, long pos, long neg, String time) {
            this.displayed = displayed;
            this.displayedPositives = tp;
            this.userPositives = pos;
            this.userNegatives = neg;
            this.timeElapsed = time;
        }

        public void setDone() {
            this.statsDone = true;
        }

        public Boolean isDone() {
            return this.statsDone;
        }
    };

    public StatisticsArea() {
        super();
        setMinimumSize(new Dimension(PREFERRED_WIDTH, MINIMUM_HEIGHT));
        setPreferredSize(new Dimension(PREFERRED_WIDTH, MINIMUM_HEIGHT));
        setMaximumSize(new Dimension(PREFERRED_WIDTH, Integer.MAX_VALUE));
        setLayout(new BorderLayout());

        display.setEditable(false);
        display.setFont(display.getFont().deriveFont(Font.BOLD, 16f));
        clear();

        JScrollPane jsp = new JScrollPane(
                display,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp.setBorder(BorderFactory.createTitledBorder("Statistics"));
        jsp.getHorizontalScrollBar().setUnitIncrement(20);
        jsp.getVerticalScrollBar().setUnitIncrement(20);

        add(jsp);

        long period = 3 * 60 * 1000; // 3 min

        // **** Block to Periodically update the average precision
        // Timer t = new java.util.Timer();
        // t.schedule(
        //         new java.util.TimerTask() {
        //             @Override
        //             public void run() {

        //                 timerStart = true;

        //                 diffDisplayed = currDisplayed - prevDisplayed;
        //                 diffTp = currDisplayedPositives - prevTp;
        //                 if (diffDisplayed == 0) {
        //                     avgPrecision = prevPrecision;
        //                     diffTp = prevTp;
        //                     diffDisplayed = prevDisplayed;
        //                 } else {
        //                     avgPrecision = 100f * diffTp / diffDisplayed;
        //                 }

        //                 prevDisplayed = currDisplayed;
        //                 prevTp = currDisplayedPositives;
        //                 prevPrecision = avgPrecision;
        //             }
        //         },
        //         period,
        //         period
        // );
        // **** End of Block

        // Periodically log statistics
        Timer t_log = new java.util.Timer();
        t_log.schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        if (prevStats == null) {
                            update(SearchStats.of(0, 0, 0, OptionalLong.empty(), 0, Optional.empty()),
                                new DisplayStats(0, 0, 0, 0, "0"));
                        }

                        timeStats.add(getCsvString(prevStats, prevDisplayStats));
                    }
                },
                period,
                period
        );
    }

    private List<String> initCSVStats() {
        List<String> csvList = new ArrayList<String>();
        csvList.add("version,auc,precision,recall,searched,displayed,positives,negatives,time\n");
        return csvList;
    }

    public void clear() {
        update(SearchStats.of(0, 0, 0, OptionalLong.empty(), 0, Optional.empty()), new DisplayStats(0, 0, 0, 0, "0"));
        csvStats = initCSVStats();
        timeStats = initCSVStats();
    }

    public String getStatistics() {
        return currentString;
    }
    public Map getCSVStatistics() {
        //TODO Change: Temp hack to record last stats
        csvStats.add(currentString);
        Map<String, List<String>> csv_dict = new HashMap<>();
        csv_dict.put("model", csvStats);
        csv_dict.put("time", timeStats);
        return csv_dict;
    }

    public void update(SearchStats stats, DisplayStats displayStats) {
        prevStats = stats;
        prevDisplayStats = displayStats;
        currDisplayed = displayStats.displayed;
        currDisplayedPositives = displayStats.displayedPositives;

        StringBuilder strDisplay = new StringBuilder();
        strDisplay.append(String.format("\n %0$-17s %d\n", "Total", stats.totalObjects()));

        long searched = stats.processedObjects();
        strDisplay.append(String.format("\n %0$-14s %d\n", "Searched", searched));

        long dropped = stats.droppedObjects();
        strDisplay.append(String.format("\n %0$-14s %d (%.2f%%)\n", "Dropped", dropped, 100f * dropped / searched));

        stats.passedObjects().ifPresent(p ->
                strDisplay.append(String.format("\n %0$-15s %d (%.2f%%)\n", "Passed", p, 100f * p / searched)));

        strDisplay.append(String.format("\n %0$-15s %d (%.2f%%)\n", "Displayed", currDisplayed,
                100f * currDisplayed / searched));

        if (currDisplayedPositives > 0 || stats.falseNegatives() > 0) {
            strDisplay.append(String.format("\n x------- AUGMENTED --------x \n"));
            long labeledTotal = currDisplayedPositives + stats.falseNegatives();

            float precision = ((float) currDisplayedPositives) / currDisplayed;
            float recall = ((float) currDisplayedPositives) / labeledTotal;

            if (!timerStart) {
                diffTp = currDisplayedPositives;
                diffDisplayed = currDisplayed;
                avgPrecision = 100f * precision;
            }
            strDisplay.append(String.format("\n %0$-18s %d \n", "True Positives", currDisplayedPositives));
            strDisplay.append(String.format("\n %0$-17s %d \n", "FN Dropped", stats.falseNegatives()));
            strDisplay.append(String.format("\n %0$-11s (%d/%d) = %.1f%% \n", "Precision ", currDisplayedPositives,
                    currDisplayed, 100f * precision));
            strDisplay.append(String.format("\n %0$-11s (%d/%d) = %.1f%% \n", "Avg. Precision ",
                    diffTp, diffDisplayed, avgPrecision));
            strDisplay.append(String.format("\n %0$-14s (%d/%d) = %.1f%% \n", "Recall", currDisplayedPositives,
                    labeledTotal, 100f * recall));
            strDisplay.append(String.format("\n %0$-17s %.3f \n", "F1 Score",
                    2 * (precision * recall) / (precision + recall)));
        }

        stats.model().ifPresent(model -> {
            strDisplay.append(String.format("\n x------- MODEL STATISTICS --------x \n"));
            strDisplay.append(String.format("\n %0$-18s %d \n", "Model Version", model.version()));
            strDisplay.append(String.format("\n %0$-18s %d \n", "Test Set Size", model.textExamples()));
            strDisplay.append(String.format("\n %0$-17s %.3f \n", "Test Set AUC", model.auc()));
            strDisplay.append(String.format("\n %0$-17s %.1f%% \n", "Test Set Precision", model.precision() * 100));
            strDisplay.append(String.format("\n %0$-17s %.1f%% \n", "Test Set Recall", model.recall() * 100));
            strDisplay.append(String.format("\n %0$-17s %.3f \n", "Test Set F1 Score", model.f1Score()));
            if(model.version() != prevVersion || displayStats.isDone()) {
                System.out.println("Version "+(model.version() != prevVersion)+" done ? "+displayStats.isDone());
                System.out.println("Positives " + displayStats.userPositives);
                csvStats.add(getCsvString(stats, displayStats));
                prevVersion = model.version();
            }
        });

        display.setText(strDisplay.toString());
        currentString = strDisplay.toString();
    }

    private String getCsvString(SearchStats stats, DisplayStats displayStats) {
        // building String "version,auc,precision,recall,searched,displayed,positives,negatives"
        StringBuilder csvString = new StringBuilder();
        stats.model().ifPresent(model -> {
            csvString.append(String.format("%d,", model.version())); // model version
            csvString.append(String.format("%.3f,", model.auc())); // model AUC
            csvString.append(String.format("%.3f,", model.precision())); // model precision
            csvString.append(String.format("%.3f,", model.recall())); // model recall
        });
        csvString.append(String.format("%d,", stats.processedObjects()));
        csvString.append(String.format("%d,", displayStats.displayed));
        csvString.append(String.format("%d,", displayStats.userPositives));
        csvString.append(String.format("%d,", displayStats.userNegatives));
        csvString.append(String.format("%s\n", displayStats.timeElapsed));
        return csvString.toString();
    }

    public void setDone() {
        clear();
    }
}
