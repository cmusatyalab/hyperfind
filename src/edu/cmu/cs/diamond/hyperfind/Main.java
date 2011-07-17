/*
 *  HyperFind, an search application for the OpenDiamond platform
 *
 *  Copyright (c) 2009-2010 Carnegie Mellon University
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

    private final SearchListModel model;

    private final List<HyperFindSearchFactory> exampleSearchFactories;

    private final JFrame frame;

    private final JFrame popupFrame;

    private final JComboBox codecs;

    private Main(JFrame frame, ThumbnailBox results, SearchListModel model,
            CookieMap initialCookieMap,
            List<HyperFindSearchFactory> exampleSearchFactories,
            JComboBox codecs) {
        this.frame = frame;
        this.results = results;
        this.model = model;
        this.cookies = initialCookieMap;
        this.exampleSearchFactories = exampleSearchFactories;
        this.codecs = codecs;

        popupFrame = new JFrame();
        popupFrame.setMinimumSize(new Dimension(512, 384));
    }

    public static Main createMain(File pluginRunner, File pluginDirectory)
            throws IOException, InterruptedException {
        final List<HyperFindSearchFactory> factories = HyperFindSearchFactory
                .createHyperFindSearchFactories(pluginRunner, pluginDirectory);

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

        // search list
        final SearchListModel model = new SearchListModel();
        final SearchList searchList = new SearchList(model);

        // codecs / menu
        JButton addSearchButton = new JButton("+");
        final JPopupMenu searches = new JPopupMenu();

        addSearchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Component c = (Component) e.getSource();
                searches.show(c, 0, c.getHeight());
            }
        });

        CookieMap defaultCookieMap = CookieMap.emptyCookieMap();
        try {
            defaultCookieMap = CookieMap.createDefaultCookieMap();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final List<Filter> thumbnailFilter = new ArrayList<Filter>();
        List<HyperFindSearchFactory> exampleSearchFactories =
                new ArrayList<HyperFindSearchFactory>();
        List<HyperFindSearch> codecList = new ArrayList<HyperFindSearch>();
        initSearchFactories(factories, model, searches, thumbnailFilter,
                exampleSearchFactories, codecList);

        final JComboBox codecs = new JComboBox(codecList.toArray());

        final Main m = new Main(frame, results, model, defaultCookieMap,
                exampleSearchFactories, codecs);

        searchList.setTransferHandler(new SearchImportTransferHandler(m,
                model, factories));

        // add import
        searches.add(new JSeparator());
        JMenuItem importExampleMenuItem = new JMenuItem("From Example...");
        searches.add(importExampleMenuItem);
        importExampleMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // get file
                JFileChooser chooser = new JFileChooser();
                String[] suffixes = ImageIO.getReaderFileSuffixes();
                List<String> filteredSuffixes = new ArrayList<String>();
                for (String su : suffixes) {
                    if (!su.isEmpty()) {
                        filteredSuffixes.add(su);
                    }
                }
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "Images", filteredSuffixes.toArray(new String[0]));
                chooser.setFileFilter(filter);
                int returnVal = chooser.showOpenDialog(m.frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        File f = chooser.getSelectedFile();
                        BufferedImage img = ImageIO.read(f);
                        m.popup(f.getName(), img);
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
        });

        // add example from screenshot (requires ImageMagick)
        JMenuItem importScreenshotMenuItem =
                new JMenuItem("From Screenshot...");
        searches.add(importScreenshotMenuItem);
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

        // add from file
        JMenuItem fromFileMenuItem = new JMenuItem("From ZIP file...");
        searches.add(fromFileMenuItem);
        fromFileMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // get file
                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "ZIP files", "zip");
                chooser.setFileFilter(filter);
                int returnVal = chooser.showOpenDialog(m.frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        HyperFindSearch s = HyperFindSearchFactory
                                .createHyperFindSearch(factories, chooser
                                        .getSelectedFile().toURI());
                        if (s != null) {
                            model.addSearch(s);
                        } else {
                            JOptionPane.showMessageDialog(frame,
                                    "No search found.");
                        }
                    } catch (IOException e2) {
                        JOptionPane.showMessageDialog(frame, e2
                                .getLocalizedMessage(), "Error Reading File",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        final JButton editCodecButton = new JButton("Edit");
        editCodecButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HyperFindSearch s = (HyperFindSearch) codecs.getSelectedItem();
                try {
                    s.edit(frame);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
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
                    HyperFindSearch s = (HyperFindSearch) codecs
                            .getSelectedItem();
                    List<Filter> filters = new ArrayList<Filter>(
                            s.createFilters());
                    filters.addAll(thumbnailFilter);
                    filters.addAll(model.createFilters());

                    SearchFactory factory = m.createFactory(filters);
                    // System.out.println(factory);
                    resultsList
                            .setTransferHandler(new ResultExportTransferHandler(
                                    factory, executor));

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

                    Set<String> patchAttributes = new HashSet<String>();
                    for (Filter f : filters) {
                        String n = f.getName();
                        String p = "_filter." + n + ".patches"; // patches
                        attributes.add(p);
                        patchAttributes.add(p);
                    }

                    m.search = factory.createSearch(attributes);

                    // clear old state
                    m.results.terminate();

                    // start
                    m.results.start(m.search, patchAttributes,
                            new ActiveSearchSet(m, model.getSelectedSearches(),
                                    factory), monitors);
                } catch (IOException e1) {
                    stats.showException(e1.getCause());
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
        c1.add(searchList);

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
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                m.stopSearch();
                // clear state from previous search
                m.results.terminate();
                m.popupFrame.dispose();
            }
        });
        frame.setVisible(true);

        return m;
    }

    void popup(String name, BufferedImage img) {
        popup(name, PopupPanel.createInstance(this, img, null,
                exampleSearchFactories, model));
    }

    private static void initSearchFactories(
            List<HyperFindSearchFactory> factories,
            final SearchListModel model, final JPopupMenu searches,
            final List<Filter> thumbnailFilter,
            final List<HyperFindSearchFactory> exampleSearchFactories,
            final List<HyperFindSearch> codecList) throws IOException,
            InterruptedException {
        for (final HyperFindSearchFactory f : factories) {
            HyperFindSearchType t = f.getType();
            switch (t) {
            case CODEC:
                codecList.add(f.createHyperFindSearch());
                break;
            case FILTER:
                if (f.needsPatches()) {
                    exampleSearchFactories.add(f);
                } else if (!f.needsBundle()) {
                    JMenuItem jm = new JMenuItem(f.getDisplayName());
                    jm.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                model.addSearch(f.createHyperFindSearch());
                            } catch (IOException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            } catch (InterruptedException e1) {
                                // TODO Auto-generated catch block
                                Thread.currentThread().interrupt();
                                e1.printStackTrace();
                            }
                        }

                    });
                    searches.add(jm);
                }
                break;
            case THUMBNAIL:
                thumbnailFilter.addAll(f.createHyperFindSearch()
                        .createFilters());
            }
        }
    }

    private void popup(HyperFindResult r) {
        List<ActiveSearch> activeSearches = r.getActiveSearchSet()
                .getActiveSearches();
        popup(r.getResult().getName(), PopupPanel.createInstance(this,
                r.getResult(), activeSearches, exampleSearchFactories, model));
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
        ActiveSearchSet ss = result.getActiveSearchSet();
        SearchFactory factory = ss.getSearchFactory();
        Cursor oldCursor = frame.getCursor();
        try {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Set<String> attributes = Collections.emptySet();
            popup(new HyperFindResult(ss, factory.generateResult(id,
                    attributes)));
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            frame.setCursor(oldCursor);
        }
    }

    private List<BoundingBox> getPatches(HyperFindSearch search,
            ObjectIdentifier objectID, byte[] data)
            throws IOException {
        // Create factory
        HyperFindSearch s = (HyperFindSearch) codecs.getSelectedItem();
        List<Filter> filters = new ArrayList<Filter>(s.createFilters());
        filters.addAll(search.createFilters());
        SearchFactory factory = createFactory(filters);

        // Set push attributes
        String attr = "_filter." + search.getDigestedName() + ".patches";
        Set<String> attributes = new HashSet<String>();
        attributes.add(attr);

        // Generate result
        Result r;
        if (objectID != null) {
            r = factory.generateResult(objectID, attributes);
        } else {
            r = factory.generateResult(data, attributes);
        }

        // Generate bounding boxes
        byte[] patches = r.getValue(attr);
        if (patches != null) {
            return BoundingBox.fromPatchesList(patches);
        } else {
            return Collections.emptyList();
        }
    }

    List<BoundingBox> getPatches(HyperFindSearch search,
            ObjectIdentifier objectID) throws IOException {
        return getPatches(search, objectID, null);
    }

    List<BoundingBox> getPatches(HyperFindSearch search, byte[] data)
            throws IOException {
        return getPatches(search, null, data);
    }

    private SearchFactory createFactory(List<Filter> filters) {
        return new SearchFactory(filters, cookies);
    }

    private void stopSearch() {
        results.stop();
    }

    private void updateEditCodecButton(final JButton editCodecButton) {
        HyperFindSearch s = (HyperFindSearch) codecs.getSelectedItem();
        editCodecButton.setEnabled(s.isEditable());
    }

    private static void printUsage() {
        System.out.println("usage: " + Main.class.getName()
                + " snapfind-plugin-runner hyperfind-plugin-directory");
    }

    public static void main(String[] args) throws IOException,
            InterruptedException {
        if (args.length != 2) {
            printUsage();
            System.exit(1);
        }

        final File pluginRunner = new File(args[0]);
        if (!pluginRunner.canExecute()) {
            throw new IOException(
                    "cannot execute given snapfind-plugin-runner: "
                            + pluginRunner);
        }

        final File pluginDirectory = new File(args[1]);
        if (!pluginDirectory.isDirectory()) {
            throw new IOException(
                    "plugin directory does not exist: " + pluginDirectory);
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    createMain(pluginRunner, pluginDirectory);
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
