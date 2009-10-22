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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.*;

import edu.cmu.cs.diamond.opendiamond.CookieMap;
import edu.cmu.cs.diamond.opendiamond.Filter;
import edu.cmu.cs.diamond.opendiamond.Search;
import edu.cmu.cs.diamond.opendiamond.SearchFactory;

/**
 * The Main class.
 */
public class Main {
    private final ThumbnailBox results;

    private CookieMap cookies;

    private final ExecutorService executor;

    private Main(ThumbnailBox results, ExecutorService executor,
            CookieMap initialCookieMap) {
        this.results = results;
        this.executor = executor;
        this.cookies = initialCookieMap;
    }

    public static Main createMain(File pluginRunner,
            List<SnapFindSearchFactory> factories) throws IOException,
            InterruptedException {
        JFrame frame = new JFrame("HyperFind");
        JButton startButton = new JButton("Start");
        JButton stopButton = new JButton("Stop");
        JButton defineScopeButton = new JButton("Define Scope");
        JList resultsList = new JList();
        StatisticsBar stats = new StatisticsBar();

        ThumbnailBox results = new ThumbnailBox(stopButton, startButton,
                resultsList, stats);

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

        final List<HyperFindSearch> codecList = new ArrayList<HyperFindSearch>();
        for (SnapFindSearchFactory f : factories) {
            SnapFindSearchType t = f.getType();
            switch (t) {
            case CODEC:
                codecList.add(f.createHyperFindSearch());
                break;
            case FILTER:
                searches.add(new JMenuItem(f.getDisplayName()));
                break;
            }
        }

        final JComboBox codecs = new JComboBox(codecList.toArray());

        final Main m = new Main(results, Executors.newCachedThreadPool(),
                CookieMap.createDefaultCookieMap());

        final JButton editCodecButton = new JButton("Edit");
        editCodecButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HyperFindSearch s = codecList.get(codecs.getSelectedIndex());
                try {
                    s.edit();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

        updateEditCodecButton(editCodecButton, codecList, codecs);
        codecs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateEditCodecButton(editCodecButton, codecList, codecs);
            }
        });

        // buttons

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    m.startSearch(codecList.get(codecs.getSelectedIndex())
                            .createFilters());
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

        defineScopeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    m.cookies = CookieMap.createDefaultCookieMap();
                    System.out.println(m.cookies);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

        // list of results
        resultsList.setCellRenderer(new SearchPanelCellRenderer());

        // layout
        // TODO make left side resizing not push huge ugly space in

        Box b = Box.createHorizontalBox();
        frame.add(b);

        // left side
        Box c1 = Box.createVerticalBox();

        // codec
        JPanel codecPanel = new JPanel();
        codecPanel.add(new JLabel("Codec"));
        codecPanel.add(codecs);
        codecPanel.add(editCodecButton);
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

        frame.setMinimumSize(new Dimension(640, 480));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        return m;
    }

    private void startSearch(List<Filter> codec) throws IOException,
            InterruptedException {
        System.out.println(codec);

        List<Filter> filters = new ArrayList<Filter>(codec);

        // TODO collect more search info

        List<String> appDepends = new ArrayList<String>();
        appDepends.add("RGB");

        SearchFactory factory = new SearchFactory(filters, appDepends, cookies);

        System.out.println(factory);

        Search search = factory.createSearch(null);

        // start
        results.start(search, factory, executor);
    }

    private static void updateEditCodecButton(final JButton editCodecButton,
            final List<HyperFindSearch> codecList, final JComboBox codecs) {
        HyperFindSearch s = codecList.get(codecs.getSelectedIndex());
        editCodecButton.setEnabled(s.isEditable());
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
        Collections.sort(factories);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    createMain(pluginRunner, factories);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        });
    }
}
