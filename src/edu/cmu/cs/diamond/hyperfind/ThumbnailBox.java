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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.swing.*;

import edu.cmu.cs.diamond.hyperfind.ResultIcon.ResultIconSetting;
import edu.cmu.cs.diamond.opendiamond.*;

public class ThumbnailBox extends JPanel {
    private final int resultsPerScreen;

    private static final ResultIcon PAUSE_RESULT = new ResultIcon(null, null,
            null, null, null);

    private Search search;

    final private StatisticsBar stats;

    private ScheduledExecutorService timerExecutor;

    private ScheduledFuture<?> statsTimerFuture;

    private final JButton stopButton;

    private final JButton startButton;

    private final JButton moreResultsButton;

    private final JList list;

    private SwingWorker<?, ?> workerFuture;

    public ThumbnailBox(JButton stopButton, JButton startButton, JList list,
            StatisticsBar stats, final int resultsPerScreen) {
        super();

        this.stopButton = stopButton;
        this.startButton = startButton;
        this.stats = stats;
        this.list = list;
        this.resultsPerScreen = resultsPerScreen;

        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JScrollPane jsp = new JScrollPane(list);
        jsp
                .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jsp
                .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(jsp);

        // more results button
        moreResultsButton = new JButton("Get next " + resultsPerScreen
                + " results");
        moreResultsButton.setVisible(false);

        panel.add(moreResultsButton, BorderLayout.SOUTH);

        add(panel);

        add(stats, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(700, 600));

    }

    private void startStatsTimer() {
        timerExecutor = Executors.newSingleThreadScheduledExecutor();
        statsTimerFuture = timerExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    updateStats();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    // called on AWT thread
    public void stop() {
        if (workerFuture != null) {
            workerFuture.cancel(true);
        }
    }

    // called on AWT thread
    public void start(Search s, final Collection<String> patchAttributes,
            final List<ActiveSearch> activeSearches) {
        search = s;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        stats.setIndeterminateMessage("Initializing Search");
        startStatsTimer();

        final DefaultListModel model = new DefaultListModel();
        list.setModel(model);

        // the tricky pausing, try to make it better with local variables
        final AtomicInteger resultsLeftBeforePause = new AtomicInteger(
                resultsPerScreen);
        final Semaphore pauseSemaphore = new Semaphore(0);

        for (ActionListener a : moreResultsButton.getActionListeners()) {
            moreResultsButton.removeActionListener(a);
        }
        moreResultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moreResultsButton.setVisible(false);
                resultsLeftBeforePause.set(resultsPerScreen);
                pauseSemaphore.release();
                revalidate();
                repaint();
            }
        });

        workerFuture = new SwingWorker<Object, ResultIcon>() {
            @Override
            protected Object doInBackground() throws InterruptedException {
                // non-AWT thread
                try {
                    try {
                        while (true) {
                            if (resultsLeftBeforePause.getAndDecrement() == 0) {
                                publish(PAUSE_RESULT);

                                pauseSemaphore.acquire();
                                continue;
                            }

                            Result r = search.getNextResult();
                            if (r == null) {
                                break;
                            }
                            // System.out.println(r);

                            byte[] thumbData = r.getValue("thumbnail.jpeg");
                            BufferedImage thumb = null;
                            if (thumbData != null) {
                                ByteArrayInputStream in = new ByteArrayInputStream(
                                        thumbData);
                                try {
                                    thumb = ImageIO.read(in);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (thumb == null) {
                                // cook up blank image
                                thumb = new BufferedImage(200, 150,
                                        BufferedImage.TYPE_INT_RGB);
                            }

                            // draw patches
                            Graphics2D g = thumb.createGraphics();
                            g.setColor(Color.GREEN);
                            int origW = Util
                                    .extractInt(r.getValue("_cols.int"));
                            int origH = Util
                                    .extractInt(r.getValue("_rows.int"));
                            g
                                    .scale((double) thumb.getWidth()
                                            / (double) origW, (double) thumb
                                            .getHeight()
                                            / (double) origH);
                            for (String p : patchAttributes) {
                                // System.out.println(p);
                                byte[] patch = r.getValue(p);
                                if (patch != null) {
                                    drawPatches(g, patch);
                                }
                            }
                            g.dispose();

                            // check setting from server
                            ResultIconSetting d = ResultIconSetting.ICON_ONLY;
                            byte[] tmp = r
                                    .getValue("hyperfind.thumbnail-display");
                            if (tmp != null) {
                                String setting = Util.extractString(tmp);
                                if (setting.equals("icon")) {
                                    d = ResultIconSetting.ICON_ONLY;
                                } else if (setting.equals("label")) {
                                    d = ResultIconSetting.LABEL_ONLY;
                                } else if (setting.equals("icon-and-label")) {
                                    d = ResultIconSetting.ICON_AND_LABEL;
                                }
                            }

                            final ResultIcon resultIcon = new ResultIcon(
                                    new ImageIcon(thumb), r
                                            .getObjectIdentifier(),
                                    activeSearches, r.getName(), d);

                            publish(resultIcon);
                        }
                    } finally {
                        // System.out.println("STOP");

                        // update stats one more time
                        updateStats();

                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (search != null) {
                                    try {
                                        search.close();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                        Thread.currentThread().interrupt();
                                    }
                                }

                                if (timerExecutor != null) {
                                    timerExecutor.shutdownNow();
                                }

                                if (statsTimerFuture != null) {
                                    statsTimerFuture.cancel(true);
                                }

                                startButton.setEnabled(true);
                                stopButton.setEnabled(false);
                                moreResultsButton.setVisible(false);
                            }
                        });
                    }
                } catch (final IOException e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            stats.showException(e.getCause());
                        }
                    });
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void process(List<ResultIcon> chunks) {
                // AWT thread
                for (ResultIcon resultIcon : chunks) {
                    if (resultIcon == PAUSE_RESULT) {
                        moreResultsButton.setVisible(true);
                        revalidate();
                        repaint();
                    } else {
                        model.addElement(resultIcon);
                    }
                }
            }
        };
        workerFuture.execute();
    }

    private void drawPatches(Graphics2D g, byte[] patches) {
        ByteBuffer bb = ByteBuffer.wrap(patches);
        List<BoundingBox> boxes = BoundingBox.fromPatchesList(bb);

        for (BoundingBox b : boxes) {
            int x0 = b.getX0();
            int y0 = b.getY0();
            int x1 = b.getX1();
            int y1 = b.getY1();
            Rectangle r = new Rectangle(x0, y0, x1 - x0, y1 - y0);
            g.draw(r);
        }
    }

    private void updateStats() throws IOException, InterruptedException {
        try {
            final Map<String, ServerStatistics> serverStats = search
                    .getStatistics();

            boolean hasStats = false;
            for (ServerStatistics s : serverStats.values()) {
                if (s.getTotalObjects() != 0) {
                    hasStats = true;
                    break;
                }
            }
            if (hasStats) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        stats.update(serverStats);
                    }
                });
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        stats
                                .setIndeterminateMessage("Waiting for First Results");
                    }
                });
            }
        } catch (SearchClosedException ignore) {
        }
    }
}
