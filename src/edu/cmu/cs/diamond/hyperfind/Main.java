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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

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

    private final JFrame frame;

    private final JFrame popupFrame;

    private Main(JFrame frame, ThumbnailBox results, CookieMap initialCookieMap) {
        this.frame = frame;
        this.results = results;
        this.cookies = initialCookieMap;

        popupFrame = new JFrame();
        popupFrame.setMinimumSize(new Dimension(512, 384));
    }

    public static Main createMain(File pluginRunner,
            List<HyperFindSearchFactory> factories) throws IOException,
            InterruptedException {
        final JFrame frame = new JFrame("HyperFind");
        JButton startButton = new JButton("Start");
        JButton stopButton = new JButton("Stop");
        JButton defineScopeButton = new JButton("Define Scope");
        final JList resultsList = new JList();
        StatisticsBar stats = new StatisticsBar();

        resultsList.setModel(new DefaultListModel());
        resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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

        final Main m = new Main(frame, results, CookieMap
                .createDefaultCookieMap());

        final List<Filter> thumbnailFilter = new ArrayList<Filter>();
        final List<HyperFindSearchFactory> exampleSearchFactories = new ArrayList<HyperFindSearchFactory>();
        final List<HyperFindSearch> codecList = new ArrayList<HyperFindSearch>();
        initSearchFactories(factories, model, searches, thumbnailFilter,
                exampleSearchFactories, codecList);

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
                        List<ActiveSearch> empty = Collections.emptyList();
                        m.popup(f.getName(), img, empty,
                                exampleSearchFactories, model);
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
        });

        final JComboBox codecs = new JComboBox(codecList.toArray());

        final JButton editCodecButton = new JButton("Edit");
        editCodecButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HyperFindSearch s = codecList.get(codecs.getSelectedIndex());
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
                    m.startSearch(thumbnailFilter, codecList.get(
                            codecs.getSelectedIndex()).createFilters(), model
                            .createFilters(), convertToActiveSearchList(model
                            .getSelectedSearches()));
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
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
                try {
                    m.stopSearch();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                    Thread.currentThread().interrupt();
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

        resultsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int index = resultsList.locationToIndex(e.getPoint());
                    if (index == -1) {
                        return;
                    }

                    ResultIcon r = (ResultIcon) resultsList.getModel()
                            .getElementAt(index);
                    if (r != null) {
                        ObjectIdentifier id = r.getObjectIdentifier();
                        try {
                            Result newR = m.reexecute(id, thumbnailFilter,
                                    codecList.get(codecs.getSelectedIndex())
                                            .createFilters(), model
                                            .createFilters());
                            m.popup(newR, r.getActiveSearches(),
                                    exampleSearchFactories, model);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
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
                try {
                    m.stopSearch();
                    m.popupFrame.dispose();
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                    e1.printStackTrace();
                }
            }
        });
        frame.setVisible(true);

        return m;
    }

    private static List<ActiveSearch> convertToActiveSearchList(
            List<HyperFindSearch> selectedSearches) {
        List<ActiveSearch> result = new ArrayList<ActiveSearch>(
                selectedSearches.size());

        for (HyperFindSearch h : selectedSearches) {
            result.add(new ActiveSearch(h.getSearchName(), h.getInstanceName(),
                    h.getMangledName()));
        }

        return Collections.unmodifiableList(result);
    }

    private void popup(String name, BufferedImage img,
            List<ActiveSearch> activeSearches,
            List<HyperFindSearchFactory> exampleSearchFactories,
            SearchListModel model) {
        popup(name, PopupPanel.createInstance(img, activeSearches,
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
                } else {
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

    private void popup(Result r, List<ActiveSearch> activeSearches,
            List<HyperFindSearchFactory> exampleSearchFactories,
            SearchListModel model) throws IOException {
        popup(r.getName(), PopupPanel.createInstance(r, activeSearches,
                exampleSearchFactories, model));
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

    private Result reexecute(ObjectIdentifier id, List<Filter> thumbnail,
            List<Filter> codec, List<Filter> searches) throws IOException {
        Cursor oldCursor = frame.getCursor();

        try {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            List<Filter> filters = new ArrayList<Filter>(codec);
            filters.addAll(thumbnail);
            filters.addAll(searches);

            SearchFactory factory = createFactory(filters);
            Set<String> attributes = Collections.emptySet();
            return factory.generateResult(id, attributes);
        } finally {
            frame.setCursor(oldCursor);
        }
    }

    private SearchFactory createFactory(List<Filter> filters) {
        List<String> appDepends = new ArrayList<String>();
        appDepends.add("RGB");

        return new SearchFactory(filters, appDepends, cookies);
    }

    private void stopSearch() throws InterruptedException {
        results.stop();
    }

    private void startSearch(List<Filter> thumbnail, List<Filter> codec,
            List<Filter> searches, List<ActiveSearch> activeSearches)
            throws IOException, InterruptedException {
        List<Filter> filters = new ArrayList<Filter>(codec);
        filters.addAll(thumbnail);
        filters.addAll(searches);

        SearchFactory factory = createFactory(filters);
        System.out.println(factory);

        // push attributes
        Set<String> attributes = new HashSet<String>();
        attributes.add("thumbnail.jpeg"); // thumbnail
        attributes.add("_cols.int"); // original width
        attributes.add("_rows.int"); // original height

        Set<String> patchAttributes = new HashSet<String>();
        for (Filter f : filters) {
            String n = f.getName();
            String p = "_filter." + n + ".patches"; // patches
            attributes.add(p);
            patchAttributes.add(p);
        }

        search = factory.createSearch(attributes);

        // start
        results.start(search, patchAttributes, activeSearches);
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

        final List<HyperFindSearchFactory> factories = SnapFindSearchFactory
                .createHyperFindSearchFactories(pluginRunner);
        Collections.sort(factories, new Comparator<HyperFindSearchFactory>() {
            @Override
            public int compare(HyperFindSearchFactory o1,
                    HyperFindSearchFactory o2) {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        });

        // make SSL trusting
        trustAllSSL();

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

    private static void trustAllSSL() {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];

        javax.net.ssl.TrustManager tm = new VeryTrusting();
        trustAllCerts[0] = tm;

        javax.net.ssl.SSLContext sc;
        try {
            sc = javax.net.ssl.SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, null);
            javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc
                    .getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KeyManagementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static class VeryTrusting implements javax.net.ssl.TrustManager,
            javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }

        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }
}
