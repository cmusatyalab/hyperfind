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

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import edu.cmu.cs.diamond.hyperfind.ResultIcon.ResultIconSetting;
import edu.cmu.cs.diamond.hyperfind.ResultIcon.ResultType;
import edu.cmu.cs.diamond.hyperfind.connector.api.FeedbackObject;
import edu.cmu.cs.diamond.hyperfind.connector.api.ObjectId;
import edu.cmu.cs.diamond.hyperfind.connector.api.Search;
import edu.cmu.cs.diamond.hyperfind.connector.api.SearchResult;
import edu.cmu.cs.diamond.hyperfind.connector.api.SearchStats;
import edu.cmu.cs.diamond.hyperfind.delphi.DelphiModelStatistics;
import java.awt.Adjustable;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import one.util.streamex.EntryStream;

/**
 * The ThumbnailBox contains a scrolling panel of the thumbnails of search results + a conditional "Get next xxx
 * results" button + statistics bar NOTE: Status for start, stop buttons and stats bar is changed within the class.
 */
public class ThumbnailBox extends JPanel {

    public static final int NUM_PANELS = 2;

    private static final long NANOSEC_PER_MILLI = (long) 1e6;
    private static final int PREFERRED_WIDTH = 750;
    private static final ResultIcon PAUSE_RESULT = new ResultIcon(null, null, null, null);
    private static final HeatmapOverlayConvertOp HEATMAP_OVERLAY_OP =
            new HeatmapOverlayConvertOp(new Color(0x8000ff00, true));

    public final List<JList<ResultIcon>> resultLists;

    private final StatisticsBar stats;
    private final StatisticsArea statsArea;
    private final JButton stopButton;
    private final JButton startButton;
    private final JButton retrainButton;
    private final JButton moreResultsButton;
    private final JLabel timeLabel;
    private final JPopupMenu popupMenu;
    private final ConcurrentMap<ObjectId, ResultType> labelsToSend = new ConcurrentHashMap<>();
    private final Map<Integer, JScrollPane> resultPanes = new HashMap<>();
    private final Map<String, FeedbackObject> feedbackItems = new HashMap<>();
    private final AtomicReference<DelphiModelStatistics> modelStats = new AtomicReference<>();
    private final AtomicInteger runningJobs = new AtomicInteger(0);
    private final int resultsPerScreen;

    private volatile boolean pauseState = false;

    private Search search;
    private ScheduledExecutorService timerExecutor;
    private ExecutorService labelExecutor;
    private ScheduledFuture<?> statsTimerFuture;

    private long sampledTPCount = 0;
    private long sampledFNCount = 0;
    private long discardedPositivesCount = 0;
    private boolean proxyFlag;
    private boolean downloadResults;
    private boolean colorByModelVersion;

    private long startTime;
    private Timer timer;
    private SwingWorker<?, ?> workerFuture;
    private SwingWorker<?, ?> retrainWorker;
    private List<HyperFindSearchMonitor> searchMonitors;

    /**
     * @param stopButton
     * @param startButton
     * @param retrainButton
     * @param stats            Stats bar. Event handler will be set here.
     * @param statsArea        Stats TextArea. Event handler will be set here.
     * @param resultsPerScreen The amount of "Get next"
     */
    public ThumbnailBox(
            JButton stopButton, JButton startButton, JButton retrainButton, StatisticsBar stats,
            StatisticsArea statsArea, int resultsPerScreen) {
        this.stopButton = stopButton;
        this.startButton = startButton;
        this.retrainButton = retrainButton;
        this.stats = stats;
        this.statsArea = statsArea;
        this.resultLists = new ArrayList<>();
        this.resultsPerScreen = resultsPerScreen;
        this.popupMenu = new JPopupMenu();

        setUpResultLists();

        setLayout(new BorderLayout());

        //adding Label for timer
        timeLabel = new JLabel();
        add(timeLabel, BorderLayout.NORTH);

        // Scrolling panel for results
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        /*
         * Creating two panels for result display
         * ----------------------------------------
         * leftPanel : Results for user labeling
         * rightPanel: Results for user validation
                        requiring user annotaion / AL displayed
        */

        Box b = Box.createHorizontalBox();
        JScrollPane leftPanel = new JScrollPane(resultLists.get(0));
        JScrollPane rightPanel = new JScrollPane(resultLists.get(1));

        resultPanes.put(0, leftPanel);
        resultPanes.put(1, rightPanel);

        //rightPanel Configurations
        rightPanel.setPreferredSize(new Dimension(PREFERRED_WIDTH, Integer.MAX_VALUE));
        rightPanel.setMaximumSize(new Dimension(PREFERRED_WIDTH, Integer.MAX_VALUE));
        rightPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        rightPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        //leftPanel Configurations
        leftPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        leftPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        b.add(leftPanel);
        b.add(rightPanel);
        panel.add(b);

        // "Get Next xxx result" button
        moreResultsButton = new JButton(String.format("Get next  %d results", resultsPerScreen));
        moreResultsButton.setVisible(false);

        panel.add(moreResultsButton, BorderLayout.SOUTH);

        add(panel);

        add(stats, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(700, 600));

        setTimerListener();

        setPopUpMenu();
    }

    public void setConfig(Boolean flag, boolean downloadResults, boolean colorByModelVersion) {
        this.proxyFlag = flag;
        this.downloadResults = downloadResults;
        this.colorByModelVersion = colorByModelVersion;
    }

    //Scroll the resultPane to bottom if no item selected
    private void scrollPanesToBottom() {
        for (Map.Entry<Integer, JScrollPane> entry : resultPanes.entrySet()) {
            JScrollPane scrollPane = entry.getValue();
            int id = entry.getKey();
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            AdjustmentListener downScroller = new AdjustmentListener() {
                @Override
                public void adjustmentValueChanged(AdjustmentEvent e) {
                    Adjustable adjustable = e.getAdjustable();
                    adjustable.setValue(adjustable.getMaximum());
                    verticalBar.removeAdjustmentListener(this);
                }
            };
            if (resultLists.get(id).isSelectionEmpty()) {
                verticalBar.addAdjustmentListener(downScroller);
            }
        }
    }

    // Setting Up result List this Listeners
    private void setUpResultLists() {
        // Add listeners to selection on the result list
        ListSelectionListener selectListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                List<HyperFindResult> results = new
                        ArrayList<HyperFindResult>();
                JList<ResultIcon> list = (JList<ResultIcon>) e.getSource();
                for (ResultIcon o : list.getSelectedValuesList()) {
                    results.add(o.getResult());
                }
                results = Collections.unmodifiableList(results);
                for (HyperFindSearchMonitor sm : searchMonitors) {
                    sm.selectionChanged(results);
                }
            }
        };

        //Add listener Key-Esc to clear resultlist
        KeyListener clearSelected = new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                JList list = (JList) e.getSource();
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    list.clearSelection();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }
        };

        //Add listener for right-click to display popUpMenu
        MouseAdapter popClick = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JList list = (JList) e.getSource();
                if (SwingUtilities.isRightMouseButton(e)    // if right mouse button clicked
                        && !list.isSelectionEmpty()) {      // and list selection is not empty
                    popupMenu.show(list, e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
            }
        };

        for (int l = 0; l < NUM_PANELS; l++) {
            JList<ResultIcon> list = new JList<>();
            list.setModel(new DefaultListModel<>());
            list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            list.setDragEnabled(true);
            list.setCellRenderer(new SearchPanelCellRenderer());
            list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            list.setVisibleRowCount(0);
            list.addListSelectionListener(selectListener);
            list.addMouseListener(popClick);
            list.addKeyListener(clearSelected);
            resultLists.add(list);
        }
    }

    private void setPopUpMenu() {
        ActionListener popUpListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ResultType cmd = ResultType.valueOf(e.getActionCommand());
                List<ResultIcon> valuesSelected = new ArrayList<>();
                for (int i = 0; i < NUM_PANELS; i++) {
                    JList<ResultIcon> list = resultLists.get(i);
                    valuesSelected.addAll(list.getSelectedValuesList());
                }
                for (ResultIcon icon : valuesSelected) {
                    icon.drawOverlay(cmd);
                    SearchResult r = icon.getResult().getResult();
                    Optional<byte[]> fv = r.getBytes("feature_vector.json");
                    if (cmd == ResultType.Ignore) {
                        // If item present in the Map then delete entry
                        feedbackItems.remove(icon.getName());
                    } else {
                        if ((fv.isPresent() && fv.get().length != 0) || downloadResults) {
                            feedbackItems.put(
                                    icon.getName(),
                                    FeedbackObject.of(r.getId(), cmd.getValue(), fv.orElse(new byte[0])));
                        }
                    }

                    labelsToSend.put(r.getId(), cmd);
                }

                if (runningJobs.getAndUpdate(i -> Math.min(2, i + 1)) < 2) {
                    labelExecutor.execute(() -> {
                        try {
                            Map<ObjectId, ResultType> toSend = ImmutableMap.copyOf(labelsToSend);
                            Map<ObjectId, Integer> examples = EntryStream.of(toSend)
                                    .mapValues(r -> r.equals(ResultType.Ignore) ? -1 : r.getValue())
                                    .toMap();

                            search.labelExamples(examples);
                            toSend.forEach(labelsToSend::remove);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        } finally {
                            runningJobs.decrementAndGet();
                        }
                    });
                }

                repaint();
            }
        };

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

    private void setTimerListener() {
        startTime = System.nanoTime();
        ActionListener timerListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                long nowTime = System.nanoTime();
                long timeElapsed = (nowTime - startTime) / NANOSEC_PER_MILLI;
                String timeDisplay = String.format(
                        "%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(timeElapsed),
                        TimeUnit.MILLISECONDS.toMinutes(timeElapsed) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeElapsed)),
                        TimeUnit.MILLISECONDS.toSeconds(timeElapsed) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeElapsed)));

                timeLabel.setText(timeDisplay);
                scrollPanesToBottom();
            }
        };

        this.timer = new Timer(1000, timerListener);
        this.timer.setInitialDelay(0);
    }

    private void startStatsTimer() {
        timerExecutor = Executors.newSingleThreadScheduledExecutor();
        statsTimerFuture = timerExecutor.scheduleWithFixedDelay(this::updateStats, 0, 500, TimeUnit.MILLISECONDS);
    }

    // called on AWT thread
    public void stop() {
        if (workerFuture != null) {
            workerFuture.cancel(true);
        }
        if (retrainWorker != null) {
            retrainWorker.cancel(true);
        }
        pauseState = true;
        sampledTPCount = 0;
        sampledFNCount = 0;
        discardedPositivesCount = 0;
        modelStats.set(null);
        startButton.setEnabled(true);
        retrainButton.setEnabled(false);
        stopButton.setEnabled(false);
        moreResultsButton.setVisible(false);
    }

    public void terminate() {
        if (searchMonitors != null) {
            for (HyperFindSearchMonitor sm : searchMonitors) {
                sm.terminated();
            }
        }
    }

    public Map<String, FeedbackObject> getFeedbackItems() {
        return feedbackItems;
    }

    public void clearFeedBackItems() {
        feedbackItems.clear();
    }

    public void retrainSearch() {
        retrainWorker = new SwingWorker<Object, Void>() {
            @Override
            protected Object doInBackground() throws InterruptedException {
                // non-AWT thread
                try {
                    pauseState = true;
                    sampledFNCount = 0;
                    sampledTPCount = 0;
                    moreResultsButton.doClick();
                    pauseState = true;
                    for (int l = 0; l < resultLists.size(); l++) {
                        DefaultListModel<ResultIcon> model =
                                (DefaultListModel<ResultIcon>) resultLists.get(l).getModel();
                        model.removeAllElements();
                    }
                    moreResultsButton.setVisible(false);
                    stats.setIndeterminateMessage("Retraining in Progress...");
                    statsArea.setDone();
                    revalidate();
                    repaint();
                    search.retrainFilter(feedbackItems);
                    System.out.println("Retrain finish !");
                    clearFeedBackItems();
                    pauseState = false;
                } catch (RuntimeException e) {
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
        };
        retrainWorker.execute();
    }

    // called on AWT thread
    public void start(Search s, ActivePredicateSet activePredicateSet, List<HyperFindSearchMonitor> monitors) {
        stats.setDone();
        statsArea.setDone();
        search = s;
        searchMonitors = monitors;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        if (proxyFlag) {
            retrainButton.setEnabled(true);
        }

        pauseState = false;
        sampledTPCount = 0;
        sampledFNCount = 0;
        discardedPositivesCount = 0;
        modelStats.set(null);

        startTime = System.nanoTime();
        timer.start();
        startStatsTimer();
        labelExecutor = Executors.newSingleThreadExecutor();

        for (int l = 0; l < resultLists.size(); l++) {
            resultLists.get(l).setModel(new DefaultListModel<>());
        }

        // the tricky pausing, try to make it better with local variables
        final AtomicInteger resultsLeftBeforePause = new AtomicInteger(resultsPerScreen);
        final Semaphore pauseSemaphore = new Semaphore(0);

        for (ActionListener a : moreResultsButton.getActionListeners()) {
            moreResultsButton.removeActionListener(a);
        }

        moreResultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moreResultsButton.setVisible(false);
                resultsLeftBeforePause.set(resultsPerScreen);
                pauseState = false;
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
                            if (pauseState) {
                                continue;
                            }

                            Optional<SearchResult> resultOpt = search.getNextResult();

                            if (resultOpt.isEmpty()) {
                                System.out.println("RESULT NULL");
                                break;
                            }

                            SearchResult result = resultOpt.get();

                            Optional<byte[]> systemExamples = result.getBytes("_delphi.system_examples");
                            if (systemExamples.isPresent()) {
                                Map<String, BufferedImage> images = new HashMap<>();
                                Map<String, Map<String, ?>> metadata = new HashMap<>();

                                try (ZipInputStream zi = new ZipInputStream(new ByteArrayInputStream(systemExamples.get()))) {
                                    ZipEntry zipEntry;
                                    while ((zipEntry = zi.getNextEntry()) != null) {
                                        String[] splits = zipEntry.getName().split("/");
                                        String filename = splits[splits.length - 1];
                                        if (filename.endsWith(".jpg")) {
                                            images.put(filename.substring(0, filename.length() - 4), ImageIO.read(zi));
                                        } else {
                                            metadata.put(
                                                    filename,
                                                    new Gson().fromJson(new InputStreamReader(
                                                            zi,
                                                            StandardCharsets.UTF_8), Map.class));
                                        }
                                    }
                                }

                                publish(metadata.entrySet().stream()
                                        .map(e -> {
                                            Map<String, ?> imageMetadata = e.getValue();
                                            String objectId = (String) imageMetadata.get("object_id");
                                            double confidence =
                                                    ((Number) imageMetadata.get("confidence")).doubleValue();
                                            ObjectId systemId = ObjectId.of(
                                                    objectId,
                                                    result.getId().deviceName(),
                                                    result.getId().hostname());
                                            SearchResult systemResult = new SearchResult(systemId, ImmutableMap.of());

                                            BufferedImage image = images.get(e.getKey());
                                            if (colorByModelVersion) {
                                                int modelVersion =
                                                        ((Number) imageMetadata.get("model_version")).intValue();
                                                drawBorder(
                                                        image.createGraphics(),
                                                        Color.getHSBColor((float) ((0.1 * modelVersion) % 1), 1, 1),
                                                        image.getWidth(),
                                                        image.getHeight(),
                                                        10);
                                            }

                                            return new ResultIcon(
                                                    new HyperFindResult(activePredicateSet, systemResult),
                                                    objectId,
                                                    new ImageIcon(image),
                                                    ResultIconSetting.ICON_ONLY,
                                                    1,
                                                    -1
                                            );
                                        })
                                        .toArray(ResultIcon[]::new));
                            }

                            // This is a 'positive' that we're only receiving to get the system examples
                            // Don't show the image and adjust statistics to account for this false 'positive'
                            if (result.getBytes("_delphi.should_discard").isPresent()) {
                                discardedPositivesCount++;
                                continue;
                            }

                            OptionalInt testExamples = result.getInt("_delphi.test_examples.int");
                            OptionalInt modelVersion = result.getInt("_delphi.model_version.int");
                            if (testExamples.isPresent()) {
                                DelphiModelStatistics latestStats = modelStats.get();
                                if (latestStats == null
                                        || latestStats.getLastModelVersion() < modelVersion.getAsInt()) {
                                    modelStats.set(new DelphiModelStatistics(
                                            modelVersion.getAsInt(),
                                            testExamples.getAsInt(),
                                            result.getDouble("_delphi.precision.double").getAsDouble(),
                                            result.getDouble("_delphi.recall.double").getAsDouble(),
                                            result.getDouble("_delphi.f1_score.double").getAsDouble()));
                                }
                            }

                            int score = result.getInt("_score.string").orElse(0);

                            if (resultsLeftBeforePause.getAndDecrement() == 0) {
                                publish(PAUSE_RESULT);

                                pauseSemaphore.acquire();
                                continue;
                            }

                            HyperFindResult hr = new HyperFindResult(activePredicateSet, result);

                            for (HyperFindSearchMonitor m : searchMonitors) {
                                m.notify(hr);
                            }

                            Optional<byte[]> thumbData = result.getBytes("thumbnail.jpeg");
                            BufferedImage thumb = null;
                            if (thumbData.isPresent()) {
                                ByteArrayInputStream in = new ByteArrayInputStream(thumbData.get());

                                try {
                                    thumb = ImageIO.read(in);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (thumb == null) {
                                // cook up blank image
                                thumb = new BufferedImage(200, 150, BufferedImage.TYPE_INT_RGB);
                            }

                            // draw heatmaps and patches
                            ResultRegions regions = hr.getRegions();
                            Graphics2D g = thumb.createGraphics();
                            g.setRenderingHint(
                                    RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                            int origW = result.getInt("_cols.int").getAsInt();
                            int origH = result.getInt("_rows.int").getAsInt();

                            g.scale(
                                    (double) thumb.getWidth() / (double) origW,
                                    (double) thumb.getHeight() / (double) origH);

                            for (BufferedImage heatmap : regions.getHeatmaps()) {
                                drawHeatmap(g, heatmap);
                            }

                            g.setColor(Color.GREEN);

                            for (BoundingBox box : regions.getPatches()) {
                                drawPatch(g, box);
                            }

                            if (result.getBytes("_gt_label").isPresent()) {
                                drawBorder(g, Color.RED, origW, origH, 80);
                                if (score == 1) {
                                    sampledFNCount += 1;
                                } else {
                                    sampledTPCount += 1;
                                }
                            } else if (colorByModelVersion && modelVersion.isPresent()) {
                                drawBorder(g,
                                        Color.getHSBColor((float) ((0.1 * modelVersion.getAsInt()) % 1), 1, 1),
                                        origW,
                                        origH,
                                        80);
                            }

                            g.dispose();

                            // check setting from server
                            ResultIconSetting d = ResultIconSetting.ICON_ONLY;
                            Optional<String> settingOpt = result.getString("hyperfind.thumbnail-display");
                            if (settingOpt.isPresent()) {
                                String setting = settingOpt.get();
                                if (setting.equals("icon")) {
                                    d = ResultIconSetting.ICON_ONLY;
                                } else if (setting.equals("label")) {
                                    d = ResultIconSetting.LABEL_ONLY;
                                } else if (setting.equals("icon-and-label")) {
                                    d = ResultIconSetting.ICON_AND_LABEL;
                                }
                            }

                            ResultIcon resultIcon =
                                    new ResultIcon(hr, result.getName(), new ImageIcon(thumb), d, score);
                            publish(resultIcon);
                        }
                    } finally {
                        // System.out.println("STOP");

                        timer.stop();
                        // update stats one more time, if possible
                        updateStats();

                        for (HyperFindSearchMonitor sm : searchMonitors) {
                            sm.stopped();
                        }

                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (search != null) {
                                    search.close();
                                }

                                if (timerExecutor != null) {
                                    timerExecutor.shutdownNow();
                                }

                                if (labelExecutor != null) {
                                    labelExecutor.shutdownNow();
                                    labelsToSend.clear();
                                }

                                if (statsTimerFuture != null) {
                                    statsTimerFuture.cancel(true);
                                }

                                startButton.setEnabled(true);
                                retrainButton.setEnabled(false);
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
                    if (pauseState) {
                        break;
                    }
                    if (resultIcon == PAUSE_RESULT) {
                        updateStats();
                        pauseState = true;
                        moreResultsButton.setVisible(true);
                        revalidate();
                        repaint();  // Repaint this Thumbnail box
                    } else {
                        /* Add newly fetched search result to result list */
                        int score = resultIcon.getScore(); //score in range 0-2
                        score = (score == 2) ? 0 : score;
                        DefaultListModel<ResultIcon> model =
                                (DefaultListModel<ResultIcon>) resultLists.get(score).getModel();
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

    private void drawBorder(Graphics2D g, Color c, int width, int height, int thickness) {
        Stroke currentStroke = g.getStroke();
        g.setColor(c);
        g.setStroke(new BasicStroke(thickness));
        Rectangle r = new Rectangle(0, 0, width, height);
        g.draw(r);
        g.setStroke(currentStroke);
    }

    private void updateStats() {
        try {
            if (pauseState) {
                return;
            }

            Map<String, SearchStats> serverStats = search.getStats();

            boolean hasStats = false;
            for (SearchStats s : serverStats.values()) {
                if (s.totalObjects() != 0) {
                    hasStats = true;
                    break;
                }
            }

            if (hasStats) {
                SwingUtilities.invokeLater(() -> {
                    long passed = resultLists.get(0).getModel().getSize();
                    stats.update(serverStats);
                    statsArea.update(
                            serverStats,
                            passed,
                            sampledTPCount,
                            sampledFNCount,
                            discardedPositivesCount,
                            Optional.ofNullable(modelStats.get()));
                });
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        stats.setIndeterminateMessage("Waiting for First Results");
                    }
                });
            }
        } catch (RuntimeException ignore) {
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
