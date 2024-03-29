/*
 *  HyperFind, a search application for the OpenDiamond platform
 *
 *  Copyright (c) 2009-2012 Carnegie Mellon University
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import edu.cmu.cs.diamond.hyperfind.collaboration.SearchSelector;
import edu.cmu.cs.diamond.hyperfind.connection.api.Connection;
import edu.cmu.cs.diamond.hyperfind.connection.api.FeedbackObject;
import edu.cmu.cs.diamond.hyperfind.connection.api.Filter;
import edu.cmu.cs.diamond.hyperfind.connection.api.HyperFindPredicateState;
import edu.cmu.cs.diamond.hyperfind.connection.api.ObjectId;
import edu.cmu.cs.diamond.hyperfind.connection.api.RunningSearch;
import edu.cmu.cs.diamond.hyperfind.connection.api.Search;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchFactory;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchInfo;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchListenable;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchListener;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchResult;
import edu.cmu.cs.diamond.hyperfind.connection.api.bundle.BundleType;
import edu.cmu.cs.diamond.hyperfind.jackson.ObjectMappers;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * The Main class.
 */
public final class Main {

    private static final String SAVED_SEARCH_EXTENSION = "hyperfindsearch";
    private static final int IMAGE_DOWNLOAD_BATCH_SIZE = 50;

    private final ThumbnailBox results;

    private final Connection connection;

    private SearchFactory searchFactory;

    private Search search;

    private final PredicateListModel model;

    private final List<HyperFindPredicateFactory> examplePredicateFactories;

    private final JFrame frame;

    private final JFrame popupFrame;

    private final JComboBox codecs;

    private Main(
            JFrame frame,
            Connection connection,
            ThumbnailBox results,
            PredicateListModel model,
            List<HyperFindPredicateFactory> examplePredicateFactories,
            JComboBox codecs) {
        this.frame = frame;
        this.connection = connection;
        this.results = results;
        this.model = model;
        this.examplePredicateFactories = examplePredicateFactories;
        this.codecs = codecs;

        popupFrame = new JFrame();
        popupFrame.setMinimumSize(new Dimension(512, 384));
        JComponent root = popupFrame.getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "dispose");
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ctrl W"), "dispose");
        root.getActionMap().put("dispose", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                popupFrame.dispose();
            }
        });
    }

    public static Main createMain(Connection connection, Optional<RunningSearch> runningSearch) throws IOException {
        // ugly hack to set application name for GNOME Shell
        // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6528430
        try {
            Field awtAppClassName = Toolkit.getDefaultToolkit().getClass().
                    getDeclaredField("awtAppClassName");
            awtAppClassName.setAccessible(true);
            awtAppClassName.set(null, "HyperFind");
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<HyperFindPredicateFactory> factories =
                HyperFindPredicateFactory.createHyperFindPredicateFactories(connection.getBundles());

        JFrame frame = new JFrame("HyperFind");

        JButton startButton = new JButton("Start");
        JButton stopButton = new JButton("Stop");
        JButton scopeButton = new JButton("Define Scope");
        JButton exportPredicatesButton = new JButton("Export");
        JButton importPredicatesButton = new JButton("Import");
        StatisticsBar stats = new StatisticsBar();
        StatisticsArea statsArea = new StatisticsArea();

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16,
                500, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        threadPoolExecutor.allowCoreThreadTimeOut(true);

        SearchManager searchManager = new SearchManager(startButton, stopButton);
        ThumbnailBox results = new ThumbnailBox(searchManager, stats, statsArea, 500);

        // predicate list
        PredicateListModel model = new PredicateListModel();
        PredicateList predicateList = new PredicateList(model);

        // codecs / menu
        JButton addPredicateButton = new JButton("+");
        JPopupMenu predicates = new JPopupMenu();

        addPredicateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Component c = (Component) e.getSource();
                predicates.show(c, 0, c.getHeight());
            }
        });

        List<HyperFindPredicateFactory> examplePredicateFactories = new ArrayList<>();
        List<HyperFindPredicate> codecList = new ArrayList<>();
        initPredicateFactories(factories, model, predicates, examplePredicateFactories, codecList);

        JComboBox<HyperFindPredicate> codecs = new JComboBox<>(codecList.toArray(HyperFindPredicate[]::new));
        Main m = new Main(frame, connection, results, model, examplePredicateFactories, codecs);

        predicateList.setTransferHandler(new PredicateImportTransferHandler(m, model, connection));

        // add paste
        predicates.add(new JSeparator());
        Action pasteAction = new AbstractAction("From Clipboard") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Clipboard clip = Toolkit.getDefaultToolkit().
                        getSystemClipboard();
                TransferHandler.TransferSupport ts =
                        new TransferHandler.TransferSupport(
                                predicateList,
                                clip.getContents(predicateList));
                predicateList.getTransferHandler().importData(ts);
            }
        };
        KeyStroke ks = KeyStroke.getKeyStroke("ctrl V");
        pasteAction.putValue(Action.ACCELERATOR_KEY, ks);
        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(ks, "paste");
        frame.getRootPane().getActionMap().put("paste", pasteAction);
        predicates.add(new JMenuItem(pasteAction));

        // add import
        JMenuItem fromFileMenuItem = new JMenuItem("From File...");
        predicates.add(fromFileMenuItem);
        fromFileMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // get file
                JFileChooser chooser = new JFileChooser();
                // predicate filter
                FileNameExtensionFilter predicateFilter =
                        new FileNameExtensionFilter("Predicate Files", BundleType.PREDICATE.getExtension());
                // image filter
                String[] suffixes = ImageIO.getReaderFileSuffixes();
                List<String> filteredSuffixes = new ArrayList<String>();
                for (String su : suffixes) {
                    if (!su.isEmpty()) {
                        filteredSuffixes.add(su);
                    }
                }
                FileNameExtensionFilter imageFilter =
                        new FileNameExtensionFilter(
                                "Images",
                                filteredSuffixes.toArray(new String[0]));
                // combined filter
                filteredSuffixes.add(BundleType.PREDICATE.getExtension());
                FileNameExtensionFilter combinedFilter =
                        new FileNameExtensionFilter(
                                "Predicate Files, Images",
                                filteredSuffixes.toArray(new String[0]));
                // enable filters
                chooser.setFileFilter(combinedFilter);
                chooser.addChoosableFileFilter(predicateFilter);
                chooser.addChoosableFileFilter(imageFilter);
                // show
                int returnVal = chooser.showOpenDialog(m.frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    // XXX near-duplicate of code in PredicateImportTransferHandler
                    // first try to load it as a predicate bundle
                    try {
                        HyperFindPredicate p = HyperFindPredicateFactory.createHyperFindPredicate(
                                connection, chooser.getSelectedFile().toURI());
                        model.addPredicate(p);
                        p.edit();
                    } catch (IOException e1) {
                        // now try to read it as an example image
                        try {
                            File f = chooser.getSelectedFile();
                            BufferedImage img = ImageIO.read(f);
                            if (img == null) {
                                throw new IOException("Could not read file.");
                            }
                            m.popup(f.getName(), img);
                        } catch (IOException e2) {
                            JOptionPane.showMessageDialog(frame, e2
                                            .getLocalizedMessage(), "Error Reading File",
                                    JOptionPane.ERROR_MESSAGE);
                            e2.printStackTrace();
                        }
                    }
                }
            }
        });

        // add example from screenshot (requires ImageMagick)
        JMenuItem importScreenshotMenuItem =
                new JMenuItem("From Screenshot...");
        predicates.add(importScreenshotMenuItem);
        importScreenshotMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // safely create a unique filename
                    File baseDir = new File(
                            System.getProperty("user.home"),
                            "hyperfind-screenshots");
                    baseDir.mkdir();
                    File snapFile;
                    do {
                        snapFile = new File(baseDir, new Formatter()
                                .format("%1$tF-%1$tT.%1$tL.png", new Date())
                                .toString());
                    } while (!snapFile.createNewFile());

                    // save screenshot into it for future use
                    try {
                        Process p = new ProcessBuilder(
                                "import",
                                snapFile.getAbsolutePath()).start();
                        if (p.waitFor() != 0) {
                            throw new IOException();
                        }
                    } catch (IOException e1) {
                        snapFile.delete();
                        JOptionPane.showMessageDialog(frame,
                                "Could not execute ImageMagick.", "HyperFind",
                                JOptionPane.ERROR_MESSAGE);
                        throw e1;
                    }

                    // load it
                    BufferedImage img = ImageIO.read(snapFile);

                    // display it
                    m.popup(snapFile.getAbsolutePath(), img);
                } catch (IOException e1) {
                    // ignore
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
            }
        });

        final JButton editCodecButton = new JButton("Edit");
        editCodecButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HyperFindPredicate p = (HyperFindPredicate)
                        codecs.getSelectedItem();
                p.edit();
            }
        });

        m.updateEditCodecButton(editCodecButton);
        codecs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m.updateEditCodecButton(editCodecButton);
            }
        });

        // buttons
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    HyperFindPredicate p = (HyperFindPredicate) codecs.getSelectedItem();
                    List<Filter> filters = new ArrayList<>(p.createFilters());

                    // give the ResultExportTransferHandler a different
                    // factory with just the codec, since it only needs the
                    // decoded image and not the filter output attributes
                    m.results.resultList.setTransferHandler(
                            new ResultExportTransferHandler(
                                    connection.getSearchFactory(filters),
                                    threadPoolExecutor));

                    filters.addAll(model.createFilters());
                    SearchFactory factory = connection.getSearchFactory(filters);
                    m.searchFactory = factory;

                    List<HyperFindSearchMonitor> monitors =
                            HyperFindSearchMonitorFactory.getInterestedSearchMonitors(filters);

                    // push attributes
                    Set<String> attributes = new HashSet<>();
                    attributes.add("_rgb_image.rgbimage"); // RGB
                    attributes.add("thumbnail.jpeg"); // thumbnail
                    attributes.add("_cols.int"); // original width
                    attributes.add("_rows.int"); // original height
                    attributes.add("feature_vector.json");
                    attributes.add("Device-Name");
                    attributes.add("Display-Name");
                    attributes.add("hyperfind.thumbnail-display");
                    attributes.add("hyperfind.external-link");  // an external URL for downloaind the orignal object
                    attributes.add("hyperfind.save-ext"); // a custom file extension to save the object
                    attributes.add("");

                    for (HyperFindSearchMonitor m : monitors) {
                        attributes.addAll(m.getPushAttributes());
                    }

                    // patches and heatmaps
                    Set<String> filterNames = new HashSet<String>();
                    for (Filter f : filters) {
                        filterNames.add(f.name());
                    }
                    attributes.addAll(ResultRegions.getPushAttributes(filterNames));

                    List<HyperFindPredicateState> predicateState = model.getSelectedPredicates().stream()
                            .map(HyperFindPredicate::export)
                            .collect(Collectors.toList());
                    m.search = factory.createSearch(attributes, predicateState);

                    // clear old state
                    m.results.terminate();

                    // start
                    m.results.start(
                            m.search,
                            new ActivePredicateSet(m, model.getSelectedPredicates(), factory),
                            monitors);
                } catch (RuntimeException e1) {
                    searchManager.searchStopped();
                    Throwable e2 = e1.getCause();
                    stats.showException(e2 != null ? e2 : e1);
                    e1.printStackTrace();
                }
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Map<String, List<String>>  csvResults = m.results.getStats();

                File baseDir = new File(
                        System.getProperty("user.home"),
                        "hyperfind-logs");
                baseDir.mkdir();
                String timeNow = new Formatter().format("%1$tF-%1$tT.%1$tL.csv", new Date()).toString();
                csvResults.forEach((key, statsList) -> {
                    String filename = key + "-" + timeNow;
                    try{
                        PrintWriter writer = new PrintWriter(new File(baseDir, filename));
                        for (String stats : statsList) {
                            writer.write(stats);
                        }
                        writer.close();
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }
                });

                Optional<Path> exportDirOpt = m.search.getExportDir();
                m.stopSearch();

                if (exportDirOpt.isPresent()) {
                    Path exportDir = exportDirOpt.get();

                    Map<String, FeedbackObject> map = m.results.getFeedbackItems();

                    Iterables.partition(map.values(), IMAGE_DOWNLOAD_BATCH_SIZE).forEach(batch -> {
                        Map<ObjectId, SearchResult> results = m.searchFactory.getResults(
                                batch.stream().map(FeedbackObject::id).collect(Collectors.toSet()),
                                ImmutableSet.of(SearchResult.DATA_ATTR));

                        for (FeedbackObject object : batch) {
                            Path subDir = exportDir.resolve(object.label() == 1 ? "positive" : "negative");
                            subDir.toFile().mkdirs();

                            String[] nameSplits = object.id().objectId().split("/");
                            String filename = nameSplits[nameSplits.length - 1];

                            try {
                                Files.write(
                                        subDir.resolve("hyperfind_export_" + filename),
                                        results.get(object.id()).getData());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                }
                m.results.clearFeedBackItems();
            }
        });

        scopeButton.addActionListener(_e -> m.connection.defineScope());

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(
                new FileNameExtensionFilter("HyperFind Search Predicates", SAVED_SEARCH_EXTENSION));

        exportPredicatesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<HyperFindPredicate> selectedPredicates = model.getSelectedPredicates();
                // Serialization
                try {

                    int retVal = chooser.showSaveDialog(m.frame);
                    if (JFileChooser.APPROVE_OPTION == retVal) {
                        String filename = chooser.getSelectedFile().getAbsolutePath();
                        if (!filename.endsWith("." + SAVED_SEARCH_EXTENSION)) {
                            filename += "." + SAVED_SEARCH_EXTENSION;
                        }

                        List<HyperFindPredicateState> predicateStates = selectedPredicates.stream()
                                .map(HyperFindPredicate::export)
                                .collect(Collectors.toList());

                        ObjectMappers.MAPPER.writeValue(new File(filename), predicateStates);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        importPredicatesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int retVal = chooser.showOpenDialog(m.frame);

                    if (JFileChooser.APPROVE_OPTION == retVal) {
                        List<HyperFindPredicateState> restoredStates = ObjectMappers.MAPPER.readValue(
                                chooser.getSelectedFile(), new TypeReference<>() {});

                        for (HyperFindPredicateState state : restoredStates) {
                            model.addPredicate(HyperFindPredicate.restore(state, connection));
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        MouseAdapter displayClick = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JList<?> list = (JList<?>) e.getSource();
                    int index = list.locationToIndex(e.getPoint());
                    if (index == -1) {
                        return;
                    }

                    ResultIcon r = (ResultIcon) list.getModel()
                            .getElementAt(index);
                    if (r != null) {
                        m.reexecute(r.getResult());
                    }
                }
            }
        };

        m.results.resultList.addMouseListener(displayClick);

        // layout

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
        c1.add(predicateList);

        // progress display
        c1.add(statsArea);

        Box h1 = Box.createHorizontalBox();
        h1.add(Box.createHorizontalGlue());
        h1.add(addPredicateButton);
        c1.add(h1);

        // start/stop/define
        Box v1 = Box.createVerticalBox();
        v1.add(Box.createVerticalStrut(10));
        Box r1 = Box.createHorizontalBox();
        r1.add(startButton);
        r1.add(Box.createHorizontalStrut(20));
        stopButton.setEnabled(false);
        r1.add(stopButton);
        r1.add(Box.createHorizontalStrut(20));
        r1.add(scopeButton);
        v1.add(r1);
        v1.add(Box.createVerticalStrut(4));

        // Export/Import
        Box r3 = Box.createHorizontalBox();
        r3.add(exportPredicatesButton);
        r3.add(Box.createHorizontalStrut(20));
        r3.add(importPredicatesButton);
        v1.add(r3);
        v1.add(Box.createVerticalStrut(10));

        c1.add(v1);

        b.add(c1);

        // right side
        Box c2 = Box.createVerticalBox();
        c2.add(results);
        b.add(c2);

        frame.pack();

        frame.setMinimumSize(new Dimension(640, 480));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                // Hack to check if search is running
                if (startButton.isEnabled() || !connection.supportsOfflineSearch()) {
                    return;
                }

                int confirmed = JOptionPane.showConfirmDialog(
                        null,
                        "Do you wish to continue running your search after disconnecting the client?",
                        "Continue search",
                        JOptionPane.YES_NO_OPTION);

                if (confirmed != JOptionPane.YES_OPTION) {
                    m.search.close();
                }
            }
        });

        runningSearch.ifPresent(search -> {
            search.predicateState()
                    .forEach(p -> model.addPredicate(HyperFindPredicate.restore(p, connection)));

            List<HyperFindSearchMonitor> monitors =
                    HyperFindSearchMonitorFactory.getInterestedSearchMonitors(search.filters());

            SearchFactory factory = connection.getSearchFactory(search.filters());
            m.searchFactory = factory;
            m.search = search.search();

            // start
            m.results.start(
                    m.search,
                    new ActivePredicateSet(m, model.getSelectedPredicates(), factory),
                    monitors);
        });

        frame.setVisible(true);

        return m;
    }

    void popup(String name, BufferedImage img) {
        popup(name, PopupPanel.createInstance(this, connection, img, null,
                examplePredicateFactories, model));
    }

    private static void initPredicateFactories(
            List<HyperFindPredicateFactory> factories,
            final PredicateListModel model, final JPopupMenu predicates,
            final List<HyperFindPredicateFactory> examplePredicateFactories,
            final List<HyperFindPredicate> codecList) {
        JMenuItem jm = new JMenuItem("Add search predicate:");
        jm.setEnabled(false);
        predicates.add(jm);
        List<JMenuItem> exampleItems = new ArrayList<JMenuItem>();
        for (final HyperFindPredicateFactory f : factories) {
            if (f.getType() == BundleType.CODEC) {
                codecList.add(f.createHyperFindPredicate());
            } else {
                jm = new JMenuItem(f.getDisplayName());
                jm.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        HyperFindPredicate p = f.createHyperFindPredicate();
                        model.addPredicate(p);
                        p.edit();
                    }
                });
                if (f.needsExamples()) {
                    examplePredicateFactories.add(f);
                    exampleItems.add(jm);
                } else {
                    predicates.add(jm);
                }
            }
        }
        predicates.addSeparator();
        jm = new JMenuItem("Add example predicate:");
        jm.setEnabled(false);
        predicates.add(jm);
        for (JMenuItem ex : exampleItems) {
            predicates.add(ex);
        }
    }

    private void popup(HyperFindResult r) {
        popup(r.getResult().getName(), PopupPanel.createInstance(this, connection, r, examplePredicateFactories,
                model));
    }

    private void popup(HyperFindResult r, Optional<SearchResult> oldResult) {
        popup(r.getResult().getName(), PopupPanel.createInstance(this, connection, r, examplePredicateFactories, model,
                oldResult));
    }

    private void popup(String title, PopupPanel p) {
        popupFrame.setVisible(false);
        popupFrame.setTitle(title);
        popupFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        popupFrame.setIconImage(p.getImage());

        popupFrame.getContentPane().removeAll();
        popupFrame.add(p);

        popupFrame.pack();
        popupFrame.repaint();
        popupFrame.setVisible(true);
    }

    void reexecute(HyperFindResult result) {
        SearchResult prevResult = result.getResult();
        ObjectId id = prevResult.getId();

        ActivePredicateSet ps = result.getActivePredicateSet();
        SearchFactory factory = ps.getSearchFactory();
        Cursor oldCursor = frame.getCursor();
        // try to render popup using the existing result.
        // if unsuccessful, try making a connection to Diamond via reexecution
        try {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            popup(result, Optional.ofNullable(prevResult));
        } catch (Exception e) {
            popup(
                new HyperFindResult(ps, factory.getResult(id, Collections.emptySet())),
                Optional.ofNullable(prevResult));
        } finally {
            frame.setCursor(oldCursor);
        }
    }

    // returns null if object was dropped
    private ResultRegions getRegions(
            Connection connection,
            HyperFindPredicate predicate,
            ObjectId objectID,
            byte[] data) {
        // Create factory
        HyperFindPredicate p = (HyperFindPredicate) codecs.getSelectedItem();
        List<Filter> filters = new ArrayList<Filter>(p.createFilters());
        filters.addAll(predicate.createFilters());
        SearchFactory factory = connection.getSearchFactory(filters);

        // Set push attributes for patches and heatmaps
        List<String> filterNames = predicate.getFilterNames();
        Set<String> attributes = ResultRegions.getPushAttributes(filterNames);

        // Generate result
        SearchResult r;
        if (objectID != null) {
            r = factory.getResult(objectID, attributes);
        } else {
            r = factory.getResult(data, attributes);
        }

        // Check if object was dropped
        for (String fName : filterNames) {
            if (r.getBytes("_filter." + fName + "_score").isEmpty()) {
                return null;
            }
        }

        // We're safe
        return new ResultRegions(filterNames, r);
    }

    ResultRegions getRegions(Connection connection, HyperFindPredicate predicate, ObjectId objectID) {
        return getRegions(connection, predicate, objectID, null);
    }

    ResultRegions getRegions(Connection connection, HyperFindPredicate predicate, byte[] data) {
        return getRegions(connection, predicate, null, data);
    }

    private void stopSearch() {
        results.stop();
    }

    private void updateEditCodecButton(final JButton editCodecButton) {
        HyperFindPredicate p = (HyperFindPredicate) codecs.getSelectedItem();
        editCodecButton.setEnabled(p.isEditable());
    }

    private static void printUsage() {
        System.out.printf("usage: %s connection-type <connection args>", Main.class.getName());
        System.out.println();
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }

        String[] connnectorArgs = Arrays.stream(args).skip(1).toArray(String[]::new);
        Class<?>[] connnectorArgClasses = Arrays.stream(connnectorArgs).map(c -> String.class).toArray(Class[]::new);

        Connection connection =
                (Connection) Class.forName(args[0])
                        .getConstructor(connnectorArgClasses)
                        .newInstance((Object[]) connnectorArgs);

        List<SearchInfo> runningSearches = connection.getRunningSearches();

        SwingUtilities.invokeLater(() -> {
            try {
                if (runningSearches.isEmpty()) {
                    createMain(connection, Optional.empty());
                } else {
                    new SearchSelector(runningSearches, searchInfo -> {
                        try {
                            createMain(connection, searchInfo.map(s -> s.searchSupplier().get()));
                        } catch (IOException ex) {
                            throw new RuntimeException("Failed to create main frame", ex);
                        }
                    }).setVisible(true);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to create main frame", e);
            }
        });
    }
}

class SearchManager implements SearchListenable, SearchListener {

    private final JButton startButton;
    private final JButton stopButton;
    private final List<SearchListener> searchListeners = new ArrayList<>();
    private Optional<Search> runningSearch = Optional.empty();
    private Optional<Runnable> runningCallback = Optional.empty();

    SearchManager(JButton startButton, JButton stopButton) {
        this.startButton = startButton;
        this.stopButton = stopButton;
    }

    @Override
    public void addListener(SearchListener listener) {
        runningSearch.ifPresent(s -> listener.searchStarted(s, runningCallback.get()));
        searchListeners.add(listener);
    }

    @Override
    public void removeListener(SearchListener listener) {
        searchListeners.remove(listener);
    }

    @Override
    public void searchStarted(Search search, Runnable retrainCallback) {
        runningSearch = Optional.of(search);
        runningCallback = Optional.of(retrainCallback);
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        searchListeners.forEach(s -> s.searchStarted(search, retrainCallback));
    }

    @Override
    public void searchStopped() {
        runningSearch = Optional.empty();
        runningCallback = Optional.empty();
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        searchListeners.forEach(SearchListener::searchStopped);
    }
}
