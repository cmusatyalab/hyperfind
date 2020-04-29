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
import edu.cmu.cs.diamond.hyperfind.delphi.DelphiModelStatistics;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

final class StatisticsArea extends JPanel {

    private static final int PREFERRED_WIDTH = 300;

    private static final int MINIMUM_HEIGHT = 200;

    private final Box box = Box.createVerticalBox();

    private final JTextArea display = new JTextArea();

    private static String currentString;

    private long prev_displayed = 0;

    private long prev_tp = 0;

    private float prev_precision = 0;

    private long curr_displayed = 0;

    private long curr_tp = 0;

    private float avg_precision = 0;

    private long diff_displayed = 0;

    private long diff_tp = 0;

    private boolean timerStart = false;

    public StatisticsArea() {
        super();
        setMinimumSize(new Dimension(PREFERRED_WIDTH, MINIMUM_HEIGHT));
        setPreferredSize(new Dimension(PREFERRED_WIDTH, MINIMUM_HEIGHT));
        setMaximumSize(new Dimension(PREFERRED_WIDTH, Integer.MAX_VALUE));
        setLayout(new BorderLayout());

        display.setEditable(false);
        display.setFont(display.getFont().deriveFont(Font.BOLD, 16f));
        clear();

        JScrollPane jsp = new JScrollPane(display,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp.setBorder(BorderFactory.createTitledBorder("Statistics"));
        jsp.getHorizontalScrollBar().setUnitIncrement(20);
        jsp.getVerticalScrollBar().setUnitIncrement(20);

        add(jsp);

        long period = 3 * 60 * 1000; // 3 min

        //Periodically update the average precision
		Timer t = new java.util.Timer();
        t.schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {

                        timerStart = true;

                        diff_displayed = curr_displayed - prev_displayed;
                        diff_tp = curr_tp - prev_tp;
                        if(diff_displayed == 0) {
                            avg_precision = prev_precision;
                            diff_tp = prev_tp;
                            diff_displayed = prev_displayed;
                        }
                        else {
                            avg_precision = 100f * diff_tp / diff_displayed;
                        }

                        prev_displayed = curr_displayed;
                        prev_tp = curr_tp;
                        prev_precision = avg_precision;
                        // t.cancel();
                    }
                },
                period,
                period
        );

    }

    public void clear() {
        setNumbers(0, 0, 0, 0, 0, 0, 0, Optional.empty());
    }

    public String getStatistics() {
        return currentString;
    }

    private void setNumbers(long total, long searched, long dropped, long displayed, long true_positives, long false_negatives, long false_display, Optional<DelphiModelStatistics> modelStatistics) {
        long passed = searched - dropped;
        curr_displayed = displayed;
        curr_tp = true_positives;
        StringBuilder str_display = new StringBuilder();
        str_display.append(String.format("\n %0$-17s %d\n", "Total", total));
        str_display.append(String.format("\n %0$-14s %d\n", "Searched", searched));
        str_display.append(String.format("\n %0$-14s %d (%.2f%%)\n", "Dropped", dropped, 100f * dropped / searched));
        str_display.append(String.format("\n %0$-15s %d (%.2f%%)\n", "Passed", passed, 100f * passed / searched));
        str_display.append(String.format("\n %0$-15s %d (%.2f%%)\n", "Displayed", displayed, 100f * displayed / searched));

        if (true_positives > 0 || false_negatives > 0 || false_display > 0) {
            str_display.append(String.format("\n x------- AUGMENTED --------x \n"));
            long labeled_total = true_positives + false_negatives + false_display;
            float precision = 100f * true_positives / displayed;
            if(!timerStart) {
                diff_tp = true_positives;
                diff_displayed = displayed;
                avg_precision = precision;
            }
            str_display.append(String.format("\n %0$-18s %d \n", "True Positives", true_positives));
            str_display.append(String.format("\n %0$-18s %d \n", "FN Displayed", false_display));
            str_display.append(String.format("\n %0$-17s %d \n", "FN Dropped", false_negatives));
            str_display.append(String.format("\n %0$-11s (%d/%d) = %.1f%% \n", "Precision ", true_positives, displayed, precision));
            str_display.append(String.format("\n %0$-11s (%d/%d) = %.1f%% \n", "Avg. Precision ", diff_tp, diff_displayed, avg_precision));
            str_display.append(String.format("\n %0$-14s (%d/%d) = %.1f%% \n", "Curr. Recall", true_positives, labeled_total, 100f * true_positives / labeled_total));
        }

        if (modelStatistics.isPresent()) {
            str_display.append(String.format("\n x------- MODEL STATISTICS --------x \n"));
            str_display.append(String.format("\n %0$-18s %d \n", "Model Version", modelStatistics.get().getLastModelVersion()));
            str_display.append(String.format("\n %0$-18s %d \n", "Test Set Size", modelStatistics.get().getTestExamples()));
            str_display.append(String.format("\n %0$-17s %.1f%% \n", "Test Set Precision", modelStatistics.get().getPrecision() * 100));
            str_display.append(String.format("\n %0$-17s %.1f%% \n", "Test Set Recall", modelStatistics.get().getRecall() * 100));
            str_display.append(String.format("\n %0$-17s %.1f \n", "Test Set F1 Score", modelStatistics.get().getF1Score() * 100));
        }

        display.setText(str_display.toString());
        currentString = str_display.toString();
    }


    public void update(Map<String, SearchStats> serverStats, long displayed, long sampledPositive, long sampledNegative,
            long discardedPositives, Optional<DelphiModelStatistics> modelStatistics) {
        long t = 0;
        long s = 0;
        long d = 0;
        long n = 0;

        for (SearchStats ss : serverStats.values()) {
            t += ss.totalObjects();
            s += ss.processedObjects();
            d += (ss.droppedObjects() + discardedPositives);
            n += ss.falseNegatives();
        }

        setNumbers(t, s, d, displayed, sampledPositive, n, sampledNegative, modelStatistics);
    }

    public void setDone() {
        clear();
    }

}
