/*
 *  HyperFind, an search application for the OpenDiamond platform
 *
 *  Copyright (c) 2007-2009 Carnegie Mellon University
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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.swing.*;

import edu.cmu.cs.diamond.opendiamond.*;

public class ThumbnailBox extends JPanel {
    private Search search;

    private SearchFactory factory;

    final private StatisticsBar stats;

    final private Timer statsTimer = new Timer(500, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            // because it is Swing Timer, this is called from the
            // AWT dispatch thread
            Map<String, ServerStatistics> serverStats = null;
            try {
                serverStats = search.getStatistics();
            } catch (SearchClosedException e1) {
                return;
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            boolean hasStats = false;
            for (ServerStatistics s : serverStats.values()) {
                if (s.getTotalObjects() != 0) {
                    hasStats = true;
                    break;
                }
            }
            if (hasStats) {
                stats.update(serverStats);
            } else {
                stats.setIndeterminateMessage("Waiting for First Results");
            }
        }
    });

    private final JButton stopButton;

    private final JButton startButton;

    private final JList list;

    public ThumbnailBox(JButton stopButton, JButton startButton, JList list,
            StatisticsBar stats) {
        super();

        this.stopButton = stopButton;
        this.startButton = startButton;
        this.stats = stats;
        this.list = list;

        setLayout(new BorderLayout());

        add(new JScrollPane(list));

        add(stats, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(600, 600));
    }

    public void stop() throws InterruptedException {
        search.close();
        statsTimer.stop();
    }

    public Future<?> start(Search s, SearchFactory f, ExecutorService executor) {
        search = s;
        factory = f;

        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        stats.setIndeterminateMessage("Initializing Search");
        statsTimer.start();

        final DefaultListModel model = new DefaultListModel();

        list.setModel(model);

        return executor.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    while (true) {
                        Result r = search.getNextResult();
                        if (r == null) {
                            break;
                        }
                        model.addElement(r);
                    }
                } finally {
                    search.close();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            startButton.setEnabled(true);
                            stopButton.setEnabled(false);
                        }
                    });
                }

                return null;
            }
        });
    }
}
