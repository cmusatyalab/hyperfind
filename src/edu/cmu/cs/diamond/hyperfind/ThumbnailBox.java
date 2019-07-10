/*
 *  HyperFind, a search application for the OpenDiamond platform
 *
 *  Copyright (c) 2007-2012 Carnegie Mellon University
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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.cmu.cs.diamond.hyperfind.ResultIcon.ResultIconSetting;
import edu.cmu.cs.diamond.opendiamond.*;


/**
 * The ThumbnailBox contains a scrolling panel of the thumbnails of search results
 * + a conditional "Get next xxx results" button + statistics bar
 * NOTE: Status for start, stop buttons and stats bar is changed within the class.
 */
public class ThumbnailBox extends JPanel {

    private static final long NANOSEC_PER_MILLI = (long) 1e6;

    private final int resultsPerScreen;

    private static final ResultIcon PAUSE_RESULT = new ResultIcon(null, null,
            null, null);

    private static final HeatmapOverlayConvertOp HEATMAP_OVERLAY_OP =
            new HeatmapOverlayConvertOp(new Color(0x8000ff00, true));

    private Search search;

    final private StatisticsBar stats;

    final private StatisticsArea statsArea;

    private ScheduledExecutorService timerExecutor;

    private ScheduledFuture<?> statsTimerFuture;

    private final JButton stopButton;

    private final JButton startButton;

    private final JButton moreResultsButton;

    private final JList list;

    private final JLabel timeLabel;

    private final JPopupMenu popupMenu;

    private long startTime;

    private Timer timer;

    private SwingWorker<?, ?> workerFuture;

    private List<HyperFindSearchMonitor> searchMonitors;

    /**
     * @param stopButton
     * @param startButton
     * @param list The Jlist of image thumbnails.
     * @param stats Stats bar. Event handler will be set here.
     * @param statsArea Stats TextArea. Event handler will be set here.
     * @param resultsPerScreen The amount of "Get next"
     */
    public ThumbnailBox(JButton stopButton, JButton startButton, JList list,
            StatisticsBar stats, StatisticsArea statsArea, final int resultsPerScreen) {
        super();

        this.stopButton = stopButton;
        this.startButton = startButton;
        this.stats = stats;
        this.statsArea = statsArea;
        this.list = list;
        this.resultsPerScreen = resultsPerScreen;
        this.popupMenu = new JPopupMenu();

        final ThumbnailBox tb = this;

        // Add listeners to selection on the result list
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                List<HyperFindResult> results = new
                        ArrayList<HyperFindResult>();
                for (Object o : tb.list.getSelectedValues()) {
                    ResultIcon icon = (ResultIcon) o;
                    results.add(icon.getResult());
                }
                results = Collections.unmodifiableList(results);
                for (HyperFindSearchMonitor sm : tb.searchMonitors) {
                    sm.selectionChanged(results);
                }
            }
        });

        setLayout(new BorderLayout());

        //adding Label for timer
        timeLabel = new JLabel();
        add(timeLabel, BorderLayout.NORTH);

        // Scrolling panel for results
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JScrollPane jsp = new JScrollPane(list);
        jsp
                .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jsp
                .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(jsp);

        // "Get Next xxx result" button
        moreResultsButton = new JButton("Get next " + resultsPerScreen
                + " results");
        moreResultsButton.setVisible(false);

        panel.add(moreResultsButton, BorderLayout.SOUTH);

        add(panel);

        add(stats, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(700, 600));

        setTimerListener();

        setPopUpMenu();
    }

    private void setPopUpMenu(){
        ActionListener popUpListener = new ActionListener () {
            public void actionPerformed(ActionEvent e)
            {
                for (Object o : list.getSelectedValues()) {
                    ResultIcon icon = (ResultIcon) o;
                    icon.drawOverlay(e.getActionCommand());
                }
                repaint();
            }
        };

        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
               if (SwingUtilities.isRightMouseButton(e)    // if right mouse button clicked
                     && !list.isSelectionEmpty()) {      // and list selection is not empty
                  popupMenu.show(list, e.getX(), e.getY());
               }
            }
      	});

        JMenuItem positive = new JMenuItem("Positive");
        JMenuItem negative = new JMenuItem("Negative");
        JMenuItem ignore = new JMenuItem("Ignore");

        positive.addActionListener(popUpListener);       
        negative.addActionListener(popUpListener);       
        ignore.addActionListener(popUpListener);       

        popupMenu.add(positive);
        popupMenu.add(negative);
        popupMenu.add(ignore);

    }

    private void setTimerListener(){
		startTime = System.nanoTime();
        ActionListener timerListener = new ActionListener () {
            public void actionPerformed(ActionEvent e)
            {
                long nowTime = System.nanoTime();
                long timeElapsed = (nowTime - startTime)/NANOSEC_PER_MILLI;
				String timeDisplay = String.format("%02d:%02d:%02d", 
				    TimeUnit.MILLISECONDS.toHours(timeElapsed),
				    TimeUnit.MILLISECONDS.toMinutes(timeElapsed) - 
				    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeElapsed)),
				    TimeUnit.MILLISECONDS.toSeconds(timeElapsed) - 
				    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeElapsed)));

                timeLabel.setText(timeDisplay);
            }
        };

        this.timer = new Timer(1000, timerListener);
        this.timer.setInitialDelay(0);
    }

    private void startStatsTimer() {
        timerExecutor = Executors.newSingleThreadScheduledExecutor();
        statsTimerFuture = timerExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    updateStats();
                } catch (IOException e) {
                    // This should also be encountered and handled by the
                    // worker thread, so there's no need to be noisy here
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

    public void terminate() {
        if (searchMonitors != null) {
            for (HyperFindSearchMonitor sm : searchMonitors) {
                sm.terminated();
            }
        }
    }

    // called on AWT thread
    public void start(Search s, final ActivePredicateSet activePredicateSet,
            final List<HyperFindSearchMonitor> monitors) {
        search = s;
        searchMonitors = monitors;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);

		startTime = System.nanoTime();
		timer.start(); 
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
                            HyperFindResult hr = new HyperFindResult(
                                    activePredicateSet, r);

                            for (HyperFindSearchMonitor m : searchMonitors) {
                                m.notify(hr);
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

                            // draw heatmaps and patches
                            ResultRegions regions = hr.getRegions();
                            Graphics2D g = thumb.createGraphics();
                            g.setRenderingHint(
                                    RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                            int origW = Util
                                    .extractInt(r.getValue("_cols.int"));
                            int origH = Util
                                    .extractInt(r.getValue("_rows.int"));
                            g.scale((double) thumb.getWidth() /
                                    (double) origW,
                                    (double) thumb.getHeight() /
                                    (double) origH);
                            for (BufferedImage heatmap :
                                    regions.getHeatmaps()) {
                                drawHeatmap(g, heatmap);
                            }
                            g.setColor(Color.GREEN);
                            for (BoundingBox box : regions.getPatches()) {
                                drawPatch(g, box);
                            }
                            if (r.getKeys().contains("_gt_label")) {
                                drawBorder(g, Color.RED, origW, origH);
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
                                    hr, r.getName(), new ImageIcon(thumb), d);

                            publish(resultIcon);
                        }
                    } finally {
                        // System.out.println("STOP");

                        timer.stop();
                        // update stats one more time, if possible
                        try {
                            updateStats();
                        } catch (IOException e1) {
                            // swallow
                        } catch (InterruptedException e2) {
                            Thread.currentThread().interrupt();
                        }

                        for (HyperFindSearchMonitor sm : searchMonitors) {
                            sm.stopped();
                        }

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
                                stats.setDone();
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
                        repaint();  // Repaint this Thumbnail box
                    } else {
                        /* Add newly fetched search result to result list */
                        model.addElement(resultIcon);
                    }
                }
            }
        };
        workerFuture.execute();
    }

    private void drawHeatmap(Graphics2D g, BufferedImage heatmap) {
        g.drawImage(heatmap, HEATMAP_OVERLAY_OP, 0, 0);
    }

    private void drawPatch(Graphics2D g, BoundingBox box) {
        int x0 = box.getX0();
        int y0 = box.getY0();
        int x1 = box.getX1();
        int y1 = box.getY1();
        Rectangle r = new Rectangle(x0, y0, x1 - x0, y1 - y0);
        g.draw(r);
    }

    private void drawBorder(Graphics2D g, Color c, int width, int  height) {
        Stroke currentStroke = g.getStroke();
        g.setColor(c);
        int thickness = 80;
        g.setStroke(new BasicStroke(thickness));
        Rectangle r = new Rectangle(0, 0, width, height);
        g.draw(r);
        g.setStroke(currentStroke);
    }

    private void updateStats() throws IOException, InterruptedException {
        try {
            final Map<String, ServerStatistics> serverStats = search
                    .getStatistics();

            boolean hasStats = false;
            for (ServerStatistics s : serverStats.values()) {
                if (s.getServerStats().get(s.TOTAL_OBJECTS) != 0) {
                    hasStats = true;
                    break;
                }
            }
            if (hasStats) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        stats.update(serverStats);
                        statsArea.update(serverStats);
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
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    stats.setDone();
                    statsArea.setDone();
                }
            });
        }
    }
}
