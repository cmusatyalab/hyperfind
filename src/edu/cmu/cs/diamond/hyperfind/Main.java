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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.cmu.cs.diamond.opendiamond.*;

/**
 * The Main class.
 */
public final class Main {
    private final ThumbnailBox results;

    private CookieMap cookies;

    private Search search;

    private final PredicateListModel model;

    private final List<HyperFindPredicateFactory> examplePredicateFactories;

    private final JFrame frame;

    private final JFrame popupFrame;

    private final JComboBox codecs;

    private Main(JFrame frame, ThumbnailBox results, PredicateListModel model,
            CookieMap initialCookieMap,
            List<HyperFindPredicateFactory> examplePredicateFactories,
            JComboBox codecs) {
        this.frame = frame;
        this.results = results;
        this.model = model;
        this.cookies = initialCookieMap;
        this.examplePredicateFactories = examplePredicateFactories;
        this.codecs = codecs;

        popupFrame = new JFrame();
        popupFrame.setMinimumSize(new Dimension(512, 384));
    }

    public static Main createMain(List<File> bundleDirectories,
            List<File> filterDirectories) throws IOException {
        final BundleFactory bundleFactory =
                new BundleFactory(bundleDirectories, filterDirectories);

        final List<HyperFindPredicateFactory> factories =
                HyperFindPredicateFactory
                .createHyperFindPredicateFactories(bundleFactory);

        final JFrame frame = new JFrame("HyperFind");
        JButton startButton = new JButton("Start");
        JButton stopButton = new JButton("Stop");
        JButton defineScopeButton = new JButton("Define Scope");
        final JList resultsList = new JList();
        final StatisticsBar stats = new StatisticsBar();

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16,
                500, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        final ExecutorService executor = threadPoolExecutor;

        resultsList.setModel(new DefaultListModel());
        resultsList
                .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        resultsList.setDragEnabled(true);

        ThumbnailBox results = new ThumbnailBox(stopButton, startButton,
                resultsList, stats, 500);

        // predicate list
        final PredicateListModel model = new PredicateListModel();
        final PredicateList predicateList = new PredicateList(model);

        // codecs / menu
        JButton addPredicateButton = new JButton("+");
        final JPopupMenu predicates = new JPopupMenu();

        addPredicateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Component c = (Component) e.getSource();
                predicates.show(c, 0, c.getHeight());
            }
        });

        CookieMap defaultCookieMap = CookieMap.emptyCookieMap();
        try {
            defaultCookieMap = CookieMap.createDefaultCookieMap();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<HyperFindPredicateFactory> examplePredicateFactories =
                new ArrayList<HyperFindPredicateFactory>();
        final List<HyperFindPredicate> codecList =
                new ArrayList<HyperFindPredicate>();
        initPredicateFactories(factories, model, predicates,
                examplePredicateFactories, codecList);

        final JComboBox codecs = new JComboBox(codecList.toArray());

        final Main m = new Main(frame, results, model, defaultCookieMap,
                examplePredicateFactories, codecs);

        predicateList.setTransferHandler(new PredicateImportTransferHandler(m,
                model, bundleFactory));

        // add paste
        predicates.add(new JSeparator());
        Action pasteAction = new AbstractAction("From Clipboard") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Clipboard clip = Toolkit.getDefaultToolkit().
                        getSystemClipboard();
                TransferHandler.TransferSupport ts =
                        new TransferHandler.TransferSupport(predicateList,
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
                        new FileNameExtensionFilter("Predicate Files",
                        BundleType.PREDICATE.getExtension());
                // image filter
                String[] suffixes = ImageIO.getReaderFileSuffixes();
                List<String> filteredSuffixes = new ArrayList<String>();
                for (String su : suffixes) {
                    if (!su.isEmpty()) {
                        filteredSuffixes.add(su);
                    }
                }
                FileNameExtensionFilter imageFilter =
                        new FileNameExtensionFilter("Images",
                        filteredSuffixes.toArray(new String[0]));
                // combined filter
                filteredSuffixes.add(BundleType.PREDICATE.getExtension());
                FileNameExtensionFilter combinedFilter =
                        new FileNameExtensionFilter("Predicate Files, Images",
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
                        HyperFindPredicate p = HyperFindPredicateFactory
                                .createHyperFindPredicate(bundleFactory,
                                        chooser.getSelectedFile().toURI());
                        model.addPredicate(p);
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
                    File baseDir = new File(System.getProperty("user.home"),
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
                        Process p = new ProcessBuilder("import",
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
                p.edit(frame);
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
                    HyperFindPredicate p = (HyperFindPredicate) codecs
                            .getSelectedItem();
                    List<Filter> filters = new ArrayList<Filter>(
                            p.createFilters());

                    // give the ResultExportTransferHandler a different
                    // factory with just the codec, since it only needs the
                    // decoded image and not the filter output attributes
                    resultsList.setTransferHandler(
                            new ResultExportTransferHandler(
                                    m.createFactory(filters), executor));

                    filters.addAll(model.createFilters());
                    SearchFactory factory = m.createFactory(filters);

                    List<HyperFindSearchMonitor> monitors =
                            HyperFindSearchMonitorFactory
                            .getInterestedSearchMonitors(m.cookies, filters);

                    // push attributes
                    Set<String> attributes = new HashSet<String>();
                    attributes.add("thumbnail.jpeg"); // thumbnail
                    attributes.add("_cols.int"); // original width
                    attributes.add("_rows.int"); // original height
                    attributes.add("Display-Name");
                    attributes.add("hyperfind.thumbnail-display");

                    for (HyperFindSearchMonitor m : monitors) {
                        attributes.addAll(m.getPushAttributes());
                    }

                    // patches and heatmaps
                    Set<String> filterNames = new HashSet<String>();
                    for (Filter f : filters) {
                        filterNames.add(f.getName());
                    }
                    attributes.addAll(ResultRegions.
                            getPushAttributes(filterNames));

                    m.search = factory.createSearch(attributes);

                    // clear old state
                    m.results.terminate();

                    // start
                    m.results.start(m.search, new ActivePredicateSet(m,
                                    model.getSelectedPredicates(), factory),
                                    monitors);
                } catch (IOException e1) {
                    Throwable e2 = e1.getCause();
                    stats.showException(e2 != null ? e2 : e1);
                    e1.printStackTrace();
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m.stopSearch();
            }
        });

        defineScopeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    m.cookies = CookieMap.createDefaultCookieMap();
                    // System.out.println(m.cookies);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

        resultsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = resultsList.locationToIndex(e.getPoint());
                    if (index == -1) {
                        return;
                    }

                    ResultIcon r = (ResultIcon) resultsList.getModel()
                            .getElementAt(index);
                    if (r != null) {
                        m.reexecute(r.getResult());
                    }
                }
            }
        });

        // list of results
        resultsList.setCellRenderer(new SearchPanelCellRenderer());
        resultsList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        resultsList.setVisibleRowCount(0);

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

        Box h1 = Box.createHorizontalBox();
        h1.add(Box.createHorizontalGlue());
        h1.add(addPredicateButton);
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
        popup(name, PopupPanel.createInstance(this, img, null,
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
                            model.addPredicate(f.createHyperFindPredicate());
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
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
        popup(r.getResult().getName(), PopupPanel.createInstance(this,
                r, examplePredicateFactories, model));
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
        ObjectIdentifier id = result.getResult().getObjectIdentifier();
        ActivePredicateSet ps = result.getActivePredicateSet();
        SearchFactory factory = ps.getSearchFactory();
        Cursor oldCursor = frame.getCursor();
        try {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Set<String> attributes = Collections.emptySet();
            popup(new HyperFindResult(ps, factory.generateResult(id,
                    attributes)));
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            frame.setCursor(oldCursor);
        }
    }

    // returns null if object was dropped
    private ResultRegions getRegions(HyperFindPredicate predicate,
            ObjectIdentifier objectID, byte[] data) throws IOException {
        // Create factory
        HyperFindPredicate p = (HyperFindPredicate) codecs.getSelectedItem();
        List<Filter> filters = new ArrayList<Filter>(p.createFilters());
        filters.addAll(predicate.createFilters());
        SearchFactory factory = createFactory(filters);

        // Set push attributes for patches and heatmaps
        List<String> filterNames = predicate.getFilterNames();
        Set<String> attributes = ResultRegions.getPushAttributes(filterNames);

        // Generate result
        Result r;
        if (objectID != null) {
            r = factory.generateResult(objectID, attributes);
        } else {
            r = factory.generateResult(data, attributes);
        }

        // Check if object was dropped
        for (String fName : filterNames) {
            if (r.getValue("_filter." + fName + "_score") == null) {
                return null;
            }
        }

        // We're safe
        return new ResultRegions(filterNames, r);
    }

    ResultRegions getRegions(HyperFindPredicate predicate,
            ObjectIdentifier objectID) throws IOException {
        return getRegions(predicate, objectID, null);
    }

    ResultRegions getRegions(HyperFindPredicate predicate, byte[] data)
            throws IOException {
        return getRegions(predicate, null, data);
    }

    private SearchFactory createFactory(List<Filter> filters) {
        return new SearchFactory(filters, cookies);
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

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            printUsage();
            System.exit(1);
        }

        final List<File> bundleDirectories = splitDirs(args[0]);
        final List<File> filterDirectories = splitDirs(args[1]);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    createMain(bundleDirectories, filterDirectories);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }
}
