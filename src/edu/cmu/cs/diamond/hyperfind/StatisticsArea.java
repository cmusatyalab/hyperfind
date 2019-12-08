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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import edu.cmu.cs.diamond.opendiamond.ServerStatistics;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import java.util.concurrent.Semaphore;

final class StatisticsArea extends JPanel{

    private static final int PREFERRED_WIDTH = 300;

    private static final int MINIMUM_HEIGHT = 200;

    private final Box box = Box.createVerticalBox();

    private final JTextArea display = new JTextArea();

    private static String currentString;

    private Semaphore statisticsMapSem = new Semaphore(1);
    private Map<String, Long> statisticsMap = new HashMap<String, Long>();


    public StatisticsArea() {
        super();
        setMinimumSize(new Dimension(PREFERRED_WIDTH, MINIMUM_HEIGHT));
        setPreferredSize(new Dimension(PREFERRED_WIDTH, MINIMUM_HEIGHT));
        setMaximumSize(new Dimension(PREFERRED_WIDTH, Integer.MAX_VALUE));
        setLayout(new BorderLayout());

        display.setEditable(false);
        display.setFont(display.getFont().deriveFont(Font.BOLD,16f));
	    clear();

        JScrollPane jsp = new JScrollPane(display,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp.setBorder(BorderFactory.createTitledBorder("Statistics"));
        jsp.getHorizontalScrollBar().setUnitIncrement(20);
        jsp.getVerticalScrollBar().setUnitIncrement(20);

        add(jsp);
    }

    public void clear() {
        setNumbers(0, 0, 0, 0, 0, 0, 0);
    }

    public String getStatistics(){
        return currentString;
    }

    private void setNumbers(long total, long searched, long dropped, long displayed, long true_positives, long false_negatives, long false_display) {
        long passed = searched - dropped;
        StringBuilder str_display = new StringBuilder();
        str_display.append(String.format("\n %0$-17s %d\n", "Total", total));
        str_display.append(String.format("\n %0$-14s %d\n", "Searched", searched));
        str_display.append(String.format("\n %0$-14s %d (%.2f%%)\n", "Dropped", dropped, 100f*dropped/searched));
        str_display.append(String.format("\n %0$-15s %d (%.2f%%)\n", "Passed", passed, 100f*passed/searched));
        str_display.append(String.format("\n %0$-15s %d (%.2f%%)\n", "Displayed", displayed, 100f*displayed/searched));
        if (true_positives>0 || false_negatives>0 || false_display>0) {
            str_display.append(String.format("\n x------- AUGMENTED --------x \n"));
            long labeled_total = true_positives + false_negatives + false_display;
            str_display.append(String.format("\n %0$-18s %d \n", "True Positives", true_positives));
            str_display.append(String.format("\n %0$-18s %d \n", "FN Displayed", false_display));
            str_display.append(String.format("\n %0$-17s %d \n", "FN Dropped", false_negatives));
            str_display.append(String.format("\n %0$-11s (%d/%d) = %.1f%% \n", "Precision ", true_positives, displayed, 100f*true_positives/displayed));
            str_display.append(String.format("\n %0$-14s (%d/%d) = %.1f%% \n", "Curr. Recall", true_positives, labeled_total, 100f*true_positives/labeled_total));
        }
        display.setText(str_display.toString());
        currentString = str_display.toString();

        try {
            statisticsMapSem.acquire();
            try {
                statisticsMap.put("Total", total);
                statisticsMap.put("Searched", searched);
                statisticsMap.put("Dropped", dropped);
                statisticsMap.put("Passed", passed);
                statisticsMap.put("TruePositive", true_positives);
                statisticsMap.put("FalseNegative", false_negatives);
                statisticsMap.put("FalseDisplay", false_display);
            } finally {
                statisticsMapSem.release();
            }
        } catch (InterruptedException ex) {
            // ignore this datapoint
            ex.printStackTrace();
        }
    }

    public Map<String, Long> getStatisticsMap() {
        Map<String, Long> shallowCopy = new HashMap<String, Long>();
        try {
            statisticsMapSem.acquire();
            try {
                shallowCopy = new HashMap<String, Long>(statisticsMap);
            } finally {
                statisticsMapSem.release();
            }
        } catch (InterruptedException ex) {
            // ignore this datapoint, return empty stats
            ex.printStackTrace();
        }
        return shallowCopy;
    }


    public void update(Map<String, ServerStatistics> serverStats,
        long displayed, long sampled_positive, long sampled_negative) {
        long t = 0;
        long s = 0;
        long d = 0;
	    long p = 0;
        long n = 0;
        for (ServerStatistics ss : serverStats.values()) {
            Map<String, Long> map = ss.getServerStats();
            t += map.get(ss.TOTAL_OBJECTS);
            s += map.get(ss.PROCESSED_OBJECTS);
            d += map.get(ss.DROPPED_OBJECTS);
            p += map.get(ss.TP_OBJECTS);
            n += map.get(ss.FN_OBJECTS);
        }
        setNumbers(t, s, d, displayed, sampled_positive, n, sampled_negative);
        //System.out.println(String.format("Server \n Total %d\n Processed %d \n Dropped %d \n Passed %d\n Sampled Neg %d ",
        //    t, s, d, displayed, sampled_negative));
    }

    public void setDone() {
        clear();
    }

}
