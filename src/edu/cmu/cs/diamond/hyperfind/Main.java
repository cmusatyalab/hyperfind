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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

/**
 * The Main class.
 */
public class Main {
    private final JFrame frame;

    private Main(JFrame frame) {
        this.frame = frame;
    }

    public static Main createMain(File pluginRunner,
            List<SnapFindSearchFactory> factories) {
        JFrame frame = new JFrame("HyperFind");
        Main m = new Main(frame);

        // statistics
        StatisticsBar stats = new StatisticsBar();

        // search list
        SearchList searchList = new SearchList(factories);

        // codecs / menu
        // TODO
        JButton addSearchButton = new JButton("+");
        final JPopupMenu searches = new JPopupMenu();

        addSearchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Component c = (Component) e.getSource();
                searches.show(c, 0, c.getHeight());
            }
        });

        List<SnapFindSearchFactory> codecList = new ArrayList<SnapFindSearchFactory>();
        for (SnapFindSearchFactory f : factories) {
            SnapFindSearchType t = f.getType();
            switch (t) {
            case CODEC:
                codecList.add(f);
                break;
            case FILTER:
                searches.add(new JMenuItem(f.getDisplayName()));
                break;
            }
        }

        JComboBox codecs = new JComboBox(codecList.toArray());

        // buttons
        JButton startButton = new JButton("Start");

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        });

        JButton stopButton = new JButton("Stop");
        JButton defineScopeButton = new JButton("Define Scope");

        // list of results
        JList list = new JList();
        list.setCellRenderer(new SearchPanelCellRenderer());
        ThumbnailBox results = new ThumbnailBox(stopButton, startButton, list,
                stats);

        // layout
        Box b = Box.createHorizontalBox();
        frame.add(b);

        // left side
        Box c1 = Box.createVerticalBox();

        // codec
        JPanel codecPanel = new JPanel();
        codecPanel.add(new JLabel("Codec"));
        codecPanel.add(codecs);
        c1.add(codecPanel);

        // filters
        JScrollPane jsp = new JScrollPane(searchList);
        jsp.setBorder(BorderFactory.createTitledBorder("Filters"));
        c1.add(jsp);

        Box h1 = Box.createHorizontalBox();
        h1.add(Box.createHorizontalGlue());
        h1.add(addSearchButton);
        c1.add(h1);

        // start/stop/define
        Box v1 = Box.createVerticalBox();
        Box r2 = Box.createHorizontalBox();
        r2.add(defineScopeButton);
        v1.add(r2);
        v1.add(Box.createVerticalStrut(4));

        Box r1 = Box.createHorizontalBox();
        r1.add(startButton);
        r1.add(Box.createHorizontalStrut(20));
        stopButton.setEnabled(false);
        r1.add(stopButton);

        v1.add(r1);

        c1.add(v1);

        b.add(c1);

        // right side
        Box c2 = Box.createVerticalBox();
        c2.add(results);
        b.add(c2);

        frame.pack();

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

        final File pluginRunner = new File(args[0]);
        if (!pluginRunner.canExecute()) {
            throw new IOException(
                    "cannot execute given snapfind-plugin-runner: "
                            + pluginRunner);
        }

        final List<SnapFindSearchFactory> factories = SnapFindSearchFactory
                .createSnapFindSearchFactories(pluginRunner);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createMain(pluginRunner, factories);
            }
        });
    }
}
