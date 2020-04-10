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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import edu.cmu.cs.diamond.hyperfind.connector.api.Connection;
import edu.cmu.cs.diamond.hyperfind.connector.api.FeedbackObject;
import edu.cmu.cs.diamond.hyperfind.connector.api.Filter;
import edu.cmu.cs.diamond.hyperfind.connector.api.ObjectId;
import edu.cmu.cs.diamond.hyperfind.connector.api.Search;
import edu.cmu.cs.diamond.hyperfind.connector.api.SearchFactory;
import edu.cmu.cs.diamond.hyperfind.connector.api.SearchResult;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.BundleType;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

    private static final int IMAGE_DOWNLOAD_BATCH_SIZE = 50;

    public static Boolean proxyFlag = false;

    private final ThumbnailBox results;

    private final Connection connection;

    private SearchFactory searchFactory;

    private Search search;

    private static HyperFindProperty properties;

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
        this.properties = new HyperFindProperty();

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

    public static Main createMain(Connection connection) throws IOException {
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
        JButton retrainButton = new JButton("Retrain");
        JButton configButton = new JButton("Set Config");
        JButton defineScopeButton = new JButton("Define Scope");
        JButton exportPredicatesButton = new JButton("Export");
        JButton importPredicatesButton = new JButton("Import");
        JCheckBox proxyBox = new JCheckBox("Proxy Enable");
        StatisticsBar stats = new StatisticsBar();
        StatisticsArea statsArea = new StatisticsArea();

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16,
                500, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        ExecutorService executor = threadPoolExecutor;

        ThumbnailBox results = new ThumbnailBox(stopButton, startButton, retrainButton,
                stats, statsArea, 500);

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
        initPredicateFactories(factories, model, predicates,
                examplePredicateFactories, codecList);

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
                    // start search
                    proxyBox.setEnabled(false);
                    m.results.setConfig(proxyFlag, m.properties.checkDownload(), m.properties.colorByModelVersion());
                    HyperFindPredicate p = (HyperFindPredicate) codecs
                            .getSelectedItem();
                    List<Filter> filters = new ArrayList<>(p.createFilters());

                    // give the ResultExportTransferHandler a different
                    // factory with just the codec, since it only needs the
                    // decoded image and not the filter output attributes
                    for (int i = 0; i < m.results.NUM_PANELS; i++) {
                        m.results.resultLists.get(i).setTransferHandler(
                                new ResultExportTransferHandler(connection.getSearchFactory(filters), executor));
                    }

                    filters.addAll(model.createFilters());
                    SearchFactory factory = connection.getSearchFactory(filters);
                    m.searchFactory = factory;

                    List<HyperFindSearchMonitor> monitors =
                            HyperFindSearchMonitorFactory.getInterestedSearchMonitors(filters);

                    // push attributes
                    Set<String> attributes = new HashSet<>();
                    attributes.add("thumbnail.jpeg"); // thumbnail
                    attributes.add("_cols.int"); // original width
                    attributes.add("_rows.int"); // original height
                    attributes.add("_score.int");
                    attributes.add("feature_vector.json");
                    attributes.add("Device-Name");
                    attributes.add("Display-Name");
                    attributes.add("hyperfind.thumbnail-display");
                    attributes.add("hyperfind.external-link");
                    attributes.add("_delphi.system_examples");
                    attributes.add("_delphi.should_discard.int");
                    attributes.add("_delphi.model_version.int");
                    attributes.add("_delphi.test_examples.int");
                    attributes.add("_delphi.precision.double");
                    attributes.add("_delphi.recall.double");
                    attributes.add("_delphi.f1_score.double");

                    for (HyperFindSearchMonitor m : monitors) {
                        attributes.addAll(m.getPushAttributes());
                    }

                    // patches and heatmaps
                    Set<String> filterNames = new HashSet<String>();
                    for (Filter f : filters) {
                        filterNames.add(f.name());
                    }
                    attributes.addAll(ResultRegions.getPushAttributes(filterNames));

                    m.search = factory.createSearch(attributes);

                    // clear old state
                    m.results.terminate();

                    // start
                    m.results.start(
                            m.search,
                            new ActivePredicateSet(m, model.getSelectedPredicates(), factory),
                            monitors);
                } catch (RuntimeException e1) {
                    proxyBox.setEnabled(true);
                    Throwable e2 = e1.getCause();
                    stats.showException(e2 != null ? e2 : e1);
                    e1.printStackTrace();
                }
            }
        });

        ExecutorService downloadExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m.stopSearch();
                proxyBox.setEnabled(true);
                boolean downloadResults = m.properties.checkDownload();

                if (downloadResults) {
                    Path downloadDir =
                            m.properties.getDownloadDirectory().resolve("hyperfind-" + System.currentTimeMillis());

                    Map<String, FeedbackObject> map = m.results.getFeedbackItems();

                    Iterables.partition(map.values(), IMAGE_DOWNLOAD_BATCH_SIZE).forEach(batch -> {
                        Map<ObjectId, SearchResult> results = m.searchFactory.getResults(
                                batch.stream().map(FeedbackObject::id).collect(Collectors.toSet()),
                                ImmutableSet.of(SearchResult.DATA_ATTR));

                        for (FeedbackObject object : batch) {
                            Path subDir = downloadDir.resolve(object.label() == 1 ? "positive" : "negative");
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

        retrainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("retrain pressed");
                m.results.retrainSearch();
            }
        });

        configButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m.properties = new HyperFindProperty();
            }
        });

        defineScopeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connection.updateCookies(proxyFlag ? Optional.of(m.properties.getProxyIP()) : Optional.empty());
            }
        });

        //Proxy Enable CheckBox
        proxyBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                proxyFlag = proxyBox.isSelected();
                m.results.setConfig(proxyFlag, m.properties.checkDownload(), m.properties.colorByModelVersion());
                connection.updateCookies(proxyFlag ? Optional.of(m.properties.getProxyIP()) : Optional.empty());
            }
        });

        // Export and import predicates
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter())
                .registerTypeHierarchyAdapter(BufferedImage.class, new BufferedImageToByteArrayTypeAdaptor())
                .create();
        final String savedSearchExtension = "hyperfindsearch";
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(
                new FileNameExtensionFilter("HyperFind Search Predicates", savedSearchExtension));

        exportPredicatesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<HyperFindPredicate> selectedPredicates = model.getSelectedPredicates();
                // Serialization
                try {

                    int retVal = chooser.showSaveDialog(m.frame);
                    if (JFileChooser.APPROVE_OPTION == retVal) {
                        String filename = chooser.getSelectedFile().getAbsolutePath();
                        if (!filename.endsWith("." + savedSearchExtension)) {
                            filename += "." + savedSearchExtension;
                        }
                        Writer writer = new FileWriter(filename);
                        System.out.println("Saving predicates to " + filename);

                        ArrayList<HyperFindPredicate.HyperFindPredicateState> states =
                                new ArrayList<HyperFindPredicate.HyperFindPredicateState>();
                        for (HyperFindPredicate pred : selectedPredicates) {
                            states.add(pred.export());
                        }

                        writer.write(gson.toJson(states));
                        writer.close();
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

                        System.out.println("Importing predicates from " + chooser.getSelectedFile().getCanonicalPath());
                        Reader reader = new FileReader(chooser.getSelectedFile());
                        ArrayList<HyperFindPredicate.HyperFindPredicateState> restored_states;
                        Type type = new TypeToken<ArrayList<HyperFindPredicate.HyperFindPredicateState>>() {
                        }.getType();
                        restored_states = gson.fromJson(reader, type);

                        for (HyperFindPredicate.HyperFindPredicateState state : restored_states) {
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
                    JList list = (JList) e.getSource();
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

        for (int i = 0; i < m.results.NUM_PANELS; i++) {
            m.results.resultLists.get(i).addMouseListener(displayClick);
        }

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
        Box r2 = Box.createHorizontalBox();
        r2.add(defineScopeButton);
        r2.add(Box.createHorizontalStrut(20));
        r2.add(configButton);
        r2.add(Box.createHorizontalStrut(20));
        v1.add(r2);
        v1.add(Box.createVerticalStrut(4));

        Box r1 = Box.createHorizontalBox();
        r1.add(startButton);
        r1.add(Box.createHorizontalStrut(20));
        stopButton.setEnabled(false);
        r1.add(stopButton);
        r1.add(Box.createHorizontalStrut(20));
        retrainButton.setEnabled(false);
        r1.add(retrainButton);
        v1.add(r1);
        v1.add(Box.createVerticalStrut(4));

        // Export/Import
        Box r3 = Box.createHorizontalBox();
        r3.add(exportPredicatesButton);
        r3.add(Box.createHorizontalStrut(20));
        r3.add(importPredicatesButton);
        v1.add(r3);
        v1.add(Box.createVerticalStrut(4));

        Box r4 = Box.createHorizontalBox();
        r4.add(proxyBox);
        v1.add(r4);

        c1.add(v1);

        b.add(c1);

        // right side
        Box c2 = Box.createVerticalBox();
        c2.add(results);
        b.add(c2);

        frame.pack();

        frame.setMinimumSize(new Dimension(640, 480));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                m.stopSearch();
                // clear state from previous search
                m.results.terminate();
                m.popupFrame.dispose();
                for (HyperFindPredicate codec : codecList) {
                    codec.dispose();
                }
            }
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
            final List<HyperFindPredicate> codecList) throws IOException {
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
                        try {
                            HyperFindPredicate p = f.createHyperFindPredicate();
                            model.addPredicate(p);
                            p.edit();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
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
        try {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Set<String> attributes = Collections.emptySet();
            popup(new HyperFindResult(ps, factory.getResult(id, attributes)), Optional.ofNullable(prevResult));
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
        System.out.println("usage: " + Main.class.getName()
                + " bundle-directories filter-directories");
    }

    private static List<File> splitDirs(String paths) {
        List<File> dirs = new ArrayList<File>();
        for (String path : paths.split(":")) {
            File dir = new File(path);
            if (dir.isDirectory()) {
                dirs.add(dir);
            }
        }
        return dirs;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            printUsage();
            System.exit(1);
        }

         List<File> bundleDirectories = splitDirs(args[0]);
         List<File> filterDirectories = splitDirs(args[1]);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    createMain(null);
                    // createMain(bundleDirectories, filterDirectories);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

// https://gist.github.com/orip/3635246
class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
    public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        return Base64.getDecoder().decode(json.getAsString());
    }

    public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(Base64.getEncoder().encodeToString(src));
    }
}

class BufferedImageToByteArrayTypeAdaptor implements JsonSerializer<BufferedImage>, JsonDeserializer<BufferedImage> {

    @Override
    public BufferedImage deserialize(
            JsonElement jsonElement,
            Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        byte[] bytes = jsonDeserializationContext.deserialize(jsonElement, byte[].class);
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bufferedImage;
    }

    @Override
    public JsonElement serialize(
            BufferedImage bufferedImage,
            Type type,
            JsonSerializationContext jsonSerializationContext) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "jpg", baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonSerializationContext.serialize(baos.toByteArray());
    }
}
