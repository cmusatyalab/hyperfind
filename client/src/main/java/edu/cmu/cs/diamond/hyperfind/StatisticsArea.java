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
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

final class StatisticsArea extends JPanel {

    private static final int PREFERRED_WIDTH = 300;
    private static final int MINIMUM_HEIGHT = 200;

    private final JTextArea display = new JTextArea();

    private String currentString;

    private long prevDisplayed = 0;

    private long prevTp = 0;

    private float prevPrecision = 0;

    private long currDisplayed = 0;

    private long currDisplayedPositives = 0;

    private float avgPrecision = 0;

    private long diffDisplayed = 0;

    private long diffTp = 0;

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

        JScrollPane jsp = new JScrollPane(
                display,
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

                        diffDisplayed = currDisplayed - prevDisplayed;
                        diffTp = currDisplayedPositives - prevTp;
                        if (diffDisplayed == 0) {
                            avgPrecision = prevPrecision;
                            diffTp = prevTp;
                            diffDisplayed = prevDisplayed;
                        } else {
                            avgPrecision = 100f * diffTp / diffDisplayed;
                        }

                        prevDisplayed = currDisplayed;
                        prevTp = currDisplayedPositives;
                        prevPrecision = avgPrecision;
                        // t.cancel();
                    }
                },
                period,
                period
        );
    }

    public void clear() {
        update(SearchStats.of(0, 0, 0, OptionalLong.empty(), 0, Optional.empty()), 0, 0);
    }

    public String getStatistics() {
        return currentString;
    }

    public void update(SearchStats stats, long displayed, long displayedPositives) {
        currDisplayed = displayed;
        currDisplayedPositives = displayedPositives;

        StringBuilder strDisplay = new StringBuilder();
        strDisplay.append(String.format("\n %0$-17s %d\n", "Total", stats.totalObjects()));

        long searched = stats.processedObjects();
        strDisplay.append(String.format("\n %0$-14s %d\n", "Searched", searched));

        long dropped = stats.droppedObjects();
        strDisplay.append(String.format("\n %0$-14s %d (%.2f%%)\n", "Dropped", dropped, 100f * dropped / searched));

        stats.passedObjects().ifPresent(p ->
                strDisplay.append(String.format("\n %0$-15s %d (%.2f%%)\n", "Passed", p, 100f * p / searched)));

        strDisplay.append(String.format("\n %0$-15s %d (%.2f%%)\n", "Displayed", displayed,
                100f * displayed / searched));

        if (displayedPositives > 0 || stats.falseNegatives() > 0) {
            strDisplay.append(String.format("\n x------- AUGMENTED --------x \n"));
            long labeledTotal = displayedPositives + stats.falseNegatives();

            float precision = ((float) displayedPositives) / displayed;
            float recall = ((float) displayedPositives) / labeledTotal;

            if (!timerStart) {
                diffTp = displayedPositives;
                diffDisplayed = displayed;
                avgPrecision = 100f * precision;
            }
            strDisplay.append(String.format("\n %0$-18s %d \n", "True Positives", displayedPositives));
            strDisplay.append(String.format("\n %0$-17s %d \n", "FN Dropped", stats.falseNegatives()));
            strDisplay.append(String.format("\n %0$-11s (%d/%d) = %.1f%% \n", "Precision ", displayedPositives,
                    displayed, 100f * precision));
            strDisplay.append(String.format("\n %0$-11s (%d/%d) = %.1f%% \n", "Avg. Precision ",
                    diffTp, diffDisplayed, avgPrecision));
            strDisplay.append(String.format("\n %0$-14s (%d/%d) = %.1f%% \n", "Recall", displayedPositives,
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
        });

        display.setText(strDisplay.toString());
        currentString = strDisplay.toString();
    }

    public void setDone() {
        clear();
    }
}
