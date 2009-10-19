/*
 *  HyperFind, an search application for the OpenDiamond platform
 *
 *  Copyright (c) 2009 Carnegie Mellon University
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
 *  programs or libraries that are released under the GNU LGPL or the
 *  Eclipse Public License 1.0. You may copy and distribute such a system
 *  following the terms of the GNU GPL for HyperFind and the licenses of
 *  the other code concerned, provided that you include the source code of
 *  that other code when and as the GNU GPL requires distribution of source
 *  code.
 *
 *  Note that people who make modified versions of HyperFind are not
 *  obligated to grant this special exception for their modified versions;
 *  it is their choice whether to do so. The GNU General Public License
 *  gives permission to release a modified version without this exception;
 *  this exception also makes it possible to release a modified version
 *  which carries forward this exception.
 */

package edu.cmu.cs.diamond.hyperfind;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;

/**
 * The Main class.
 */
public class Main {
    private final JFrame frame;

    private Main(JFrame frame) {
        this.frame = frame;
    }

    public static Main createMain() {
        JFrame frame = new JFrame("HyperFind");

        JList list = new JList();
        list.setCellRenderer(new SearchPanelCellRenderer());

        StatisticsBar stats = new StatisticsBar();

        JButton startButton = new JButton("Start");
        JButton stopButton = new JButton("Stop");
        frame.add(startButton, BorderLayout.NORTH);
        frame.add(stopButton, BorderLayout.SOUTH);

        Main m = new Main(frame);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        });

        frame.add(new ThumbnailBox(stopButton, startButton, list, stats));

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        return m;
    }

    private static void printUsage() {
        System.out.println("usage: " + Main.class.getName()
                + " snapfind-plugin-runner");
    }

    public static void main(String[] args) throws IOException,
            InterruptedException {
        if (args.length != 1) {
            printUsage();
            System.exit(1);
        }

        File pluginRunner = new File(args[0]);
        if (!pluginRunner.canExecute()) {
            throw new IOException(
                    "cannot execute given snapfind-plugin-runner: "
                            + pluginRunner);
        }

        List<SnapFindSearchFactory> snapfindSearches = SnapFindSearchFactory
                .createSnapFindSearchFactorys(pluginRunner);
        snapfindSearches.get(1).createHyperFindSearch().edit();

        createMain();
    }
}
