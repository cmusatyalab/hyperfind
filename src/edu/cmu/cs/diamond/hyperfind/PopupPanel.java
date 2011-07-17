/*
 *  HyperFind, an search application for the OpenDiamond platform
 *
 *  Copyright (c) 2008-2010 Carnegie Mellon University
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
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import edu.cmu.cs.diamond.opendiamond.ObjectIdentifier;
import edu.cmu.cs.diamond.opendiamond.Result;
import edu.cmu.cs.diamond.opendiamond.Util;

public class PopupPanel extends JPanel {
    private static class ExistingSearchComboModel extends AbstractListModel
            implements ComboBoxModel, ListDataListener {

        private final SearchListModel model;

        private final List<SelectableSearch> list;

        private Object selectedItem;

        public ExistingSearchComboModel(SearchListModel model) {
            this.model = model;

            // XXX leaking
            model.addListDataListener(this);

            // copy the elements out and keep a shadow copy
            list = new ArrayList<SelectableSearch>();
            for (int i = 0; i < model.getSize(); i++) {
                SelectableSearch item = (SelectableSearch) model
                        .getElementAt(i);
                if (item.getSearch().needsPatches()) {
                    list.add(item);
                } else {
                    list.add(null);
                }
            }
        }

        @Override
        public Object getSelectedItem() {
            return selectedItem;
        }

        @Override
        public void setSelectedItem(Object anItem) {
            if ((selectedItem != null && !selectedItem.equals(anItem))
                    || selectedItem == null && anItem != null) {
                selectedItem = anItem;
                fireContentsChanged(this, -1, -1);
            }
        }

        @Override
        public Object getElementAt(int index) {
            int myIndex = -1;

            Object o = null;
            for (int i = 0; i < list.size(); i++) {
                o = list.get(i);
                if (o != null) {
                    myIndex++;
                }

                if (myIndex == index) {
                    break;
                }
            }

            return o;
        }

        @Override
        public int getSize() {
            int size = 0;
            for (Object o : list) {
                if (o != null) {
                    size++;
                }
            }
            return size;
        }

        // SearchListModel will never give a range, just single elements
        @Override
        public void contentsChanged(ListDataEvent e) {
            assert e.getIndex0() == e.getIndex1();

            int index = e.getIndex0();

            // find in ours
            if (list.get(index) != null) {
                fireContentsChanged(this, index, index);
            }
        }

        @Override
        public void intervalAdded(ListDataEvent e) {
            assert e.getIndex0() == e.getIndex1();

            int index = e.getIndex0();

            SelectableSearch item = (SelectableSearch) model
                    .getElementAt(index);
            if (!item.getSearch().needsPatches()) {
                item = null;
            }
            list.add(index, item);

            if (item != null) {
                fireIntervalAdded(this, index, index);
            }
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            assert e.getIndex0() == e.getIndex1();

            int index = e.getIndex0();

            SelectableSearch item = list.remove(index);
            if (item != null) {
                fireIntervalRemoved(this, index, index);
            }
        }
    }

    private static class SearchInstanceCellRenderer
            extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list,
                Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);
            // We may also get "No selection"
            if (value instanceof SelectableSearch) {
                HyperFindSearch s = ((SelectableSearch) value).getSearch();
                label.setText(s.getInstanceName());
            }
            return label;
        }
    }

    private static final int PATCH_LIST_MINIMUM_HEIGHT = 300;

    private static final int PATCH_LIST_PREFERRED_WIDTH = 300;

    private final BufferedImage img;

    private PopupPanel(BufferedImage img) {
        this.img = img;
    }

    private static String attributeToString(String name, byte[] value) {
        if (name.endsWith(".int")) {
            return Integer.toString(Util.extractInt(value));
        } else if (name.endsWith(".time")) {
            return Long.toString(Util.extractLong(value));
        } else if (name.endsWith(".double")) {
            return Double.toString(Util.extractDouble(value));
        } else if (name.endsWith(".jpeg")) {
            return "JPEG";
        } else if (name.endsWith(".rgbimage")) {
            return "RGBImage";
        } else if (name.endsWith(".patches")) {
            return BoundingBox.fromPatchesList(value).toString();
        } else {
            String str = Util.extractString(value);
            int len = Math.min(str.length(), 1024);
            return str.substring(0, len);
        }
    }

    public static PopupPanel createInstance(Main m, Result r,
            List<ActiveSearch> activeSearches,
            List<HyperFindSearchFactory> exampleSearchFactories,
            SearchListModel model) {

        BufferedImage img = Util.extractImageFromResult(r);

        Map<String, byte[]> attributes = new HashMap<String, byte[]>();
        for (String k : r.getKeys()) {
            // skip "data" attribute
            if (!k.equals("")) {
                attributes.put(k, r.getValue(k));
            }
        }
        return createInstance(m, r.getObjectIdentifier(), img, r.getData(),
                activeSearches, exampleSearchFactories, attributes, model);
    }

    public static PopupPanel createInstance(Main m, BufferedImage img,
            byte resultData[],
            List<HyperFindSearchFactory> exampleSearchFactories,
            SearchListModel model) {
        Map<String, byte[]> attributes = Collections.emptyMap();
        List<ActiveSearch> activeSearches = Collections.emptyList();

        return createInstance(m, null, img, resultData, activeSearches,
                exampleSearchFactories, attributes, model);
    }

    private static PopupPanel createInstance(Main m,
            ObjectIdentifier objectID, BufferedImage img, byte resultData[],
            List<ActiveSearch> activeSearches,
            List<HyperFindSearchFactory> exampleSearchFactories,
            final Map<String, byte[]> attributes,
            SearchListModel searchListModel) {
        PopupPanel p = new PopupPanel(img);

        // sort keys
        final List<String> keys = new ArrayList<String>(attributes.keySet());
        Collections.sort(keys);

        TableModel model = new AbstractTableModel() {
            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public int getRowCount() {
                return keys.size();
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                String key = keys.get(rowIndex);
                byte value[] = attributes.get(key);

                switch (columnIndex) {
                case 0:
                    return key;
                case 1:
                    return Integer.toString(value.length);
                case 2:
                    return attributeToString(key, value);
                default:
                    return null;
                }
            }

            @Override
            public String getColumnName(int column) {
                switch (column) {
                case 0:
                    return "Name";
                case 1:
                    return "Size";
                case 2:
                    return "Value";
                default:
                    return null;
                }
            }
        };

        JTable properties = new JTable(model);
        // properties.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane propertiesPane = new JScrollPane(properties);

        // assemble entire window
        Box hBox = Box.createHorizontalBox();
        Box leftSide = Box.createVerticalBox();

        // image pane or text pane
        JScrollPane scrollPane;
        if (img != null) {
            ImagePatchesLabel image = new ImagePatchesLabel(img);
            scrollPane = new JScrollPane(image);
            scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
            scrollPane.getVerticalScrollBar().setUnitIncrement(20);
            leftSide.add(createPatchesList(activeSearches, attributes, image));
            leftSide.add(createTestSearchBox(m, searchListModel, image,
                    objectID, img, p));
            leftSide.add(createExampleSearchPanel(searchListModel, image, img,
                    exampleSearchFactories));
        } else {
            String text;
            try {
                text = new String(resultData, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                text = "";
            }
            JTextArea textArea = new JTextArea(text, 25, 80);
            textArea.setEditable(false);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            scrollPane = new JScrollPane(textArea);
        }

        hBox.add(leftSide);

        JSplitPane rightSide = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
                scrollPane, propertiesPane);
        int scrollPaneHeight = Math.min(700,
                (int) scrollPane.getPreferredSize().getHeight() + 1);
        rightSide.setDividerLocation(scrollPaneHeight);

        Box vBox = Box.createVerticalBox();
        vBox.add(rightSide);
        hBox.add(vBox);

        // done
        p.setLayout(new BorderLayout());
        p.add(hBox);

        return p;
    }

    private static JPanel createExampleSearchPanel(final SearchListModel model,
            final ImagePatchesLabel image, final BufferedImage img,
            List<HyperFindSearchFactory> exampleSearchFactories) {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder("Example Search"));

        Box vBox = Box.createVerticalBox();
        p.add(vBox);

        final JButton addButton = new JButton("+");
        addButton.setEnabled(false);

        final JPopupMenu searches = new JPopupMenu();

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Component c = (Component) e.getSource();
                searches.show(c, 0, c.getHeight());
            }
        });

        for (final HyperFindSearchFactory f : exampleSearchFactories) {
            JMenuItem jm = new JMenuItem(f.getDisplayName());
            jm.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        // make patches
                        List<BufferedImage> patches = createPatches(image, img);

                        model.addSearch(f.createHyperFindSearch(patches));
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

        final JButton clearButton = new JButton("Clear Patches");
        clearButton.setEnabled(false);

        final JLabel countLabel = new JLabel();
        updateCountLabel(countLabel, model);

        // XXX leaky
        model.addListDataListener(new ListDataListener() {
            @Override
            public void contentsChanged(ListDataEvent e) {
            }

            @Override
            public void intervalAdded(ListDataEvent e) {
                updateCountLabel(countLabel, model);
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                updateCountLabel(countLabel, model);
            }
        });

        Box hBox = Box.createHorizontalBox();
        hBox.add(addButton);
        hBox.add(Box.createHorizontalStrut(10));
        hBox.add(clearButton);
        vBox.add(hBox);

        final JButton addToExistingButton = new JButton("Add to Existing");
        final ExistingSearchComboModel existingSearchComboModel = new ExistingSearchComboModel(
                model);
        final JComboBox addToExistingCombo = new JComboBox(
                existingSearchComboModel);
        addToExistingCombo.setRenderer(new SearchInstanceCellRenderer());

        addToExistingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // get item
                SelectableSearch item = (SelectableSearch) existingSearchComboModel
                        .getSelectedItem();
                try {
                    // System.out.println(item);
                    item.getSearch().addPatches(createPatches(image, img));
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

        addToExistingButton.setEnabled(false);
        addToExistingCombo.setEnabled(false);

        hBox = Box.createHorizontalBox();
        hBox.add(addToExistingButton);
        hBox.add(Box.createHorizontalStrut(10));
        hBox.add(addToExistingCombo);
        vBox.add(Box.createVerticalStrut(10));
        vBox.add(hBox);

        hBox = Box.createHorizontalBox();
        hBox.add(countLabel);
        vBox.add(hBox);

        // enabled/disabled behaviors
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                image.clearDrawnPatches();
                addButton.setEnabled(false);
                clearButton.setEnabled(false);
                addToExistingButton.setEnabled(false);
                addToExistingCombo.setEnabled(false);
            }
        });

        addToExistingCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateComponentsEnablement(image, addButton, clearButton,
                        addToExistingButton, addToExistingCombo);
            }
        });

        image.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                updateComponentsEnablement(image, addButton, clearButton,
                        addToExistingButton, addToExistingCombo);
            }
        });
        return p;
    }

    private static void updateComponentsEnablement(
            final ImagePatchesLabel image, final JButton addButton,
            final JButton clearButton, final JButton addToExistingButton,
            final JComboBox addToExistingCombo) {
        boolean b = !image.getDrawnPatches().isEmpty();
        addButton.setEnabled(b);
        clearButton.setEnabled(b);
        addToExistingCombo.setEnabled(b);

        addToExistingButton.setEnabled(b
                && (addToExistingCombo.getSelectedIndex() != -1));
    }

    private static void updateCountLabel(JLabel countLabel,
            SearchListModel model) {
        int count = model.getSize();
        countLabel.setText("Search count: " + count);
    }

    private static class TestSearchComboModel extends AbstractListModel
            implements ComboBoxModel, ListDataListener {

        private final SearchListModel model;

        private Object selectedItem;

        public TestSearchComboModel(SearchListModel model) {
            this.model = model;

            // XXX leaking
            model.addListDataListener(this);
        }

        @Override
        public Object getSelectedItem() {
            return selectedItem;
        }

        @Override
        public void setSelectedItem(Object anItem) {
            if ((selectedItem != null && !selectedItem.equals(anItem))
                    || selectedItem == null && anItem != null) {
                selectedItem = anItem;
                fireContentsChanged(this, -1, -1);
            }
        }

        @Override
        public Object getElementAt(int index) {
            if (index == 0) {
                return "No selection";
            }
            return model.getElementAt(index - 1);
        }

        @Override
        public int getSize() {
            return model.getSize() + 1;
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            fireContentsChanged(this, e.getIndex0() + 1, e.getIndex1() + 1);
        }

        @Override
        public void intervalAdded(ListDataEvent e) {
            fireIntervalAdded(this, e.getIndex0() + 1, e.getIndex1() + 1);
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            fireIntervalRemoved(this, e.getIndex0() + 1, e.getIndex1() + 1);
        }
    }

    private static JPanel createTestSearchBox(final Main m,
            final SearchListModel model, final ImagePatchesLabel image,
            final ObjectIdentifier objectID, final BufferedImage img,
            final PopupPanel pp) {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder("Filter Test"));

        final JComboBox c = new JComboBox(new TestSearchComboModel(model));
        c.setRenderer(new SearchInstanceCellRenderer());
        c.setSelectedIndex(0);
        p.add(c);

        c.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (c.getSelectedIndex() <= 0) {
                    // clear
                    List<BoundingBox> patches = Collections.emptyList();
                    image.setTestResultPatches(patches);
                } else {
                    SelectableSearch ss = (SelectableSearch) c
                            .getSelectedItem();
                    HyperFindSearch s = ss.getSearch();

                    Cursor oldCursor = pp.getCursor();

                    try {
                        pp.setCursor(Cursor
                                .getPredefinedCursor(Cursor.WAIT_CURSOR));

                        List<BoundingBox> bb;
                        if (objectID != null) {
                            bb = m.getPatches(s, objectID);
                        } else {
                            bb = m.getPatches(s, encodePNM(img));
                        }
                        image.setTestResultPatches(bb);
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } finally {
                        pp.setCursor(oldCursor);
                    }
                }
            }
        });

        return p;
    }

    private static JPanel createPatchesList(List<ActiveSearch> activeSearches,
            Map<String, byte[]> attributes, final ImagePatchesLabel image) {
        Box box = Box.createVerticalBox();

        JPanel p = new JPanel();

        p.setMinimumSize(new Dimension(PATCH_LIST_PREFERRED_WIDTH,
                PATCH_LIST_MINIMUM_HEIGHT));
        p.setPreferredSize(new Dimension(PATCH_LIST_PREFERRED_WIDTH,
                PATCH_LIST_MINIMUM_HEIGHT));
        p.setMaximumSize(new Dimension(PATCH_LIST_PREFERRED_WIDTH,
                Integer.MAX_VALUE));
        p.setLayout(new BorderLayout());

        JScrollPane jsp = new JScrollPane(box,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp.setBorder(BorderFactory.createTitledBorder("Patch Results"));
        jsp.getHorizontalScrollBar().setUnitIncrement(20);
        jsp.getVerticalScrollBar().setUnitIncrement(20);

        p.add(jsp);

        for (ActiveSearch h : activeSearches) {
            // extract patches
            String searchName = h.getSearchName();
            String name = h.getInstanceName();
            String mName = h.getMangledName();

            String key = "_filter." + mName + ".patches";

            // System.out.println(key);
            if (attributes.containsKey(key)) {
                // System.out.println(" YES");
                // patches found, add them
                final List<BoundingBox> bb = BoundingBox
                        .fromPatchesList(attributes.get(key));

                JCheckBox cb = new JCheckBox();
                Formatter f = new Formatter();
                f.format("%s (similarity %.0f%%)", searchName, 100 - 100.0 * bb
                        .get(0).getDistance());
                SearchList.updateCheckBox(cb, f.toString(), name);

                cb.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            image.addResultPatch(bb);
                        } else {
                            image.removeResultPatch(bb);
                        }
                    }
                });
                box.add(cb);
            }
        }

        return p;
    }

    public Image getImage() {
        return img;
    }

    private static List<BufferedImage> createPatches(ImagePatchesLabel image,
            BufferedImage img) {
        List<BufferedImage> patches = new ArrayList<BufferedImage>();
        for (Rectangle r : image.getDrawnPatches()) {
            BufferedImage b = new BufferedImage(r.width, r.height,
                    BufferedImage.TYPE_INT_RGB);

            Graphics2D g2 = b.createGraphics();
            g2.drawImage(img, 0, 0, r.width, r.height, r.x, r.y, r.x + r.width,
                    r.y + r.height, null);
            g2.dispose();

            patches.add(b);
        }
        return patches;
    }

    private static byte[] encodePNM(BufferedImage image) throws IOException {
        BufferedImage buf = new BufferedImage(image.getWidth(), image
                .getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = buf.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();

        ByteArrayOutputStream ppmOut = new ByteArrayOutputStream();
        // System.out.println(buf);
        if (!ImageIO.write(buf, "PNM", ppmOut)) {
            throw new IOException("Can't write out PNM");
        }
        return ppmOut.toByteArray();
    }
}
