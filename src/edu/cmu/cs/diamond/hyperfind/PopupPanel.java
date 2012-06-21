/*
 *  HyperFind, a search application for the OpenDiamond platform
 *
 *  Copyright (c) 2008-2012 Carnegie Mellon University
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import edu.cmu.cs.diamond.opendiamond.ObjectIdentifier;
import edu.cmu.cs.diamond.opendiamond.Result;
import edu.cmu.cs.diamond.opendiamond.Util;

public class PopupPanel extends JPanel {
    private static class ExistingPredicateComboModel extends AbstractListModel
            implements ComboBoxModel, ListDataListener {

        private final PredicateListModel model;

        private final List<SelectablePredicate> list;

        private Object selectedItem;

        public ExistingPredicateComboModel(PredicateListModel model) {
            this.model = model;

            model.addListDataListener(this);

            // copy the elements out and keep a shadow copy
            list = new ArrayList<SelectablePredicate>();
            for (int i = 0; i < model.getSize(); i++) {
                SelectablePredicate item = (SelectablePredicate) model
                        .getElementAt(i);
                if (item.getPredicate().needsExamples()) {
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

        // PredicateListModel will never give a range, just single elements
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

            SelectablePredicate item = (SelectablePredicate) model
                    .getElementAt(index);
            if (!item.getPredicate().needsExamples()) {
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

            SelectablePredicate item = list.remove(index);
            if (item != null) {
                fireIntervalRemoved(this, index, index);
            }
            if (getSelectedItem() == item) {
                setSelectedItem(null);
            }
        }

        public void destroy() {
            model.removeListDataListener(this);
        }
    }

    private static class PredicateInstanceCellRenderer
            extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list,
                Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);
            // We may also get "No selection"
            if (value instanceof SelectablePredicate) {
                HyperFindPredicate p = ((SelectablePredicate) value)
                        .getPredicate();
                label.setText(p.getInstanceName());
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
        } else if (name.endsWith(".png")) {
            return "PNG";
        } else if (name.endsWith(".rgbimage")) {
            return "RGBImage";
        } else if (name.endsWith(".binary")) {
            return "Binary";
        } else if (name.endsWith(".patches")) {
            return BoundingBox.fromPatchesList(value).toString();
        } else {
            String str = Util.extractString(value);
            int len = Math.min(str.length(), 1024);
            return str.substring(0, len);
        }
    }

    public static PopupPanel createInstance(Main m, HyperFindResult hr,
            List<HyperFindPredicateFactory> examplePredicateFactories,
            PredicateListModel model) {

        Result r = hr.getResult();
        BufferedImage img = Util.extractImageFromResult(r);

        Map<String, byte[]> attributes = new HashMap<String, byte[]>();
        for (String k : r.getKeys()) {
            // skip "data" attribute
            if (!k.equals("")) {
                attributes.put(k, r.getValue(k));
            }
        }
        return createInstance(m, r.getObjectIdentifier(), img, r.getData(),
                hr.getActivePredicateSet().getActivePredicates(),
                examplePredicateFactories, hr.getRegions(), attributes, model);
    }

    public static PopupPanel createInstance(Main m, BufferedImage img,
            byte resultData[],
            List<HyperFindPredicateFactory> examplePredicateFactories,
            PredicateListModel model) {
        Map<String, byte[]> attributes = Collections.emptyMap();
        List<ActivePredicate> activePredicates = Collections.emptyList();
        ResultRegions regions = new ResultRegions();

        return createInstance(m, null, img, resultData, activePredicates,
                examplePredicateFactories, regions, attributes, model);
    }

    private static PopupPanel createInstance(Main m,
            ObjectIdentifier objectID, BufferedImage img, byte resultData[],
            List<ActivePredicate> activePredicates,
            List<HyperFindPredicateFactory> examplePredicateFactories,
            ResultRegions regions, final Map<String, byte[]> attributes,
            PredicateListModel predicateListModel) {
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
            ImageRegionsLabel image = new ImageRegionsLabel(img);
            scrollPane = new JScrollPane(image);
            scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
            scrollPane.getVerticalScrollBar().setUnitIncrement(20);
            leftSide.add(new RegionsListPanel(activePredicates, regions,
                    image));
            leftSide.add(new TestPredicatePanel(m, predicateListModel, image,
                    objectID, img, p));
            leftSide.add(new ExampleSearchPanel(predicateListModel, image,
                    img, examplePredicateFactories));
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

    private static class ExampleSearchPanel extends JPanel {
        private final PredicateListModel model;

        private final ImageRegionsLabel image;

        private final BufferedImage img;

        private final JButton addButton;

        private final JButton clearButton;

        private final JButton entireButton;

        private final JLabel countLabel;

        private final JButton addToExistingButton;

        private final JComboBox addToExistingCombo;

        public ExampleSearchPanel(final PredicateListModel model,
                final ImageRegionsLabel image, BufferedImage img,
                List<HyperFindPredicateFactory> examplePredicateFactories) {
            setBorder(BorderFactory.createTitledBorder("Example Search"));

            this.model = model;
            this.image = image;
            this.img = img;

            Box vBox = Box.createVerticalBox();
            add(vBox);

            addButton = new JButton("+");
            addButton.setEnabled(false);

            final JPopupMenu predicates = new JPopupMenu();

            addButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Component c = (Component) e.getSource();
                    predicates.show(c, 0, c.getHeight());
                }
            });

            for (final HyperFindPredicateFactory f :
                    examplePredicateFactories) {
                JMenuItem jm = new JMenuItem(f.getDisplayName());
                jm.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            // make examples
                            List<BufferedImage> examples = createExamples();

                            model.addPredicate(f.createHyperFindPredicate(
                                    examples));
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                });
                predicates.add(jm);
            }

            entireButton = new JButton("Select Entire");

            clearButton = new JButton("Clear Patches");
            clearButton.setEnabled(false);

            countLabel = new JLabel();
            updateCountLabel();

            final ListDataListener listener = new ListDataListener() {
                @Override
                public void contentsChanged(ListDataEvent e) {
                }

                @Override
                public void intervalAdded(ListDataEvent e) {
                    updateCountLabel();
                }

                @Override
                public void intervalRemoved(ListDataEvent e) {
                    updateCountLabel();
                }
            };
            model.addListDataListener(listener);
            countLabel.addHierarchyListener(new HierarchyListener() {
                @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    if ((e.getChangeFlags() & e.DISPLAYABILITY_CHANGED) != 0 &&
                            !countLabel.isDisplayable()) {
                        // System.out.println("removing count label listener");
                        model.removeListDataListener(listener);
                    }
                }
            });

            Box hBox = Box.createHorizontalBox();
            hBox.add(entireButton);
            hBox.add(Box.createHorizontalStrut(10));
            hBox.add(clearButton);
            vBox.add(hBox);

            addToExistingButton = new JButton("Add to Existing");
            final ExistingPredicateComboModel existingPredicateComboModel =
                    new ExistingPredicateComboModel(model);
            addToExistingCombo = new JComboBox(existingPredicateComboModel);
            addToExistingCombo.setRenderer(
                    new PredicateInstanceCellRenderer());

            addToExistingCombo.addHierarchyListener(new HierarchyListener() {
                @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    if ((e.getChangeFlags() & e.DISPLAYABILITY_CHANGED) != 0 &&
                            !addToExistingCombo.isDisplayable()) {
                        // System.out.println("destroying ExistingPredicateComboModel");
                        existingPredicateComboModel.destroy();
                    }
                }
            });

            addToExistingButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // get item
                    SelectablePredicate item = (SelectablePredicate)
                            existingPredicateComboModel.getSelectedItem();
                    // System.out.println(item);
                    item.getPredicate().addExamples(createExamples());
                }
            });

            addToExistingButton.setEnabled(false);
            addToExistingCombo.setEnabled(false);

            hBox = Box.createHorizontalBox();
            hBox.add(addButton);
            hBox.add(Box.createHorizontalStrut(10));
            hBox.add(addToExistingButton);
            hBox.add(Box.createHorizontalStrut(10));
            hBox.add(addToExistingCombo);
            vBox.add(Box.createVerticalStrut(10));
            vBox.add(hBox);

            hBox = Box.createHorizontalBox();
            hBox.add(countLabel);
            vBox.add(hBox);

            // enabled/disabled behaviors
            entireButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    image.addEntireDrawnPatch();
                    entireButton.setEnabled(false);
                    updateComponentsEnablement();
                }
            });

            clearButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    image.clearDrawnPatches();
                    entireButton.setEnabled(true);
                    updateComponentsEnablement();
                }
            });

            addToExistingCombo.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    updateComponentsEnablement();
                }
            });

            image.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    updateComponentsEnablement();
                }
            });
        }

        private List<BufferedImage> createExamples() {
            List<BufferedImage> examples = new ArrayList<BufferedImage>();
            for (Rectangle r : image.getDrawnPatches()) {
                BufferedImage b = new BufferedImage(r.width, r.height,
                        BufferedImage.TYPE_INT_RGB);

                Graphics2D g2 = b.createGraphics();
                g2.drawImage(img, 0, 0, r.width, r.height, r.x, r.y,
                        r.x + r.width, r.y + r.height, null);
                g2.dispose();

                examples.add(b);
            }
            return examples;
        }

        private void updateComponentsEnablement() {
            boolean b = !image.getDrawnPatches().isEmpty();
            addButton.setEnabled(b);
            clearButton.setEnabled(b);
            addToExistingCombo.setEnabled(b);

            addToExistingButton.setEnabled(b
                    && (addToExistingCombo.getSelectedIndex() != -1));
        }

        private void updateCountLabel() {
            int count = model.getSize();
            countLabel.setText(count + " predicate" + (count == 1 ? "" : "s"));
        }
    }

    private static class PredicateTestComboModel extends AbstractListModel
            implements ComboBoxModel, ListDataListener {

        private final PredicateListModel model;

        private Object selectedItem;

        private final String NO_ITEM = "No selection";

        public PredicateTestComboModel(PredicateListModel model) {
            this.model = model;
            setSelectedItem(NO_ITEM);

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
                return NO_ITEM;
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

            int size = getSize();
            Object item = getSelectedItem();
            // sub-optimal
            for (int i = 1; i < size; i++) {
                if (getElementAt(i) == item) {
                    item = null;
                    break;
                }
            }
            if (item != null) {
                // item was deleted, deselect it
                setSelectedItem(NO_ITEM);
            }
        }

        public void destroy() {
            model.removeListDataListener(this);
        }
    }

    private static class TestPredicatePanel extends JPanel {
        private final Main m;

        private final ImageRegionsLabel image;

        private final ObjectIdentifier objectID;

        private final BufferedImage img;

        private final PopupPanel pp;

        private final JLabel label;

        private HyperFindPredicate selected;

        public TestPredicatePanel(Main m, PredicateListModel model,
                ImageRegionsLabel image, ObjectIdentifier objectID,
                BufferedImage img, PopupPanel pp) {
            setBorder(BorderFactory.createTitledBorder("Test Predicate"));

            this.m = m;
            this.image = image;
            this.objectID = objectID;
            this.img = img;
            this.pp = pp;

            Box vBox = Box.createVerticalBox();
            add(vBox);

            final JComboBox c = new JComboBox(
                    new PredicateTestComboModel(model));
            c.setRenderer(new PredicateInstanceCellRenderer());
            vBox.add(c);

            c.addHierarchyListener(new HierarchyListener() {
                @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    if ((e.getChangeFlags() & e.DISPLAYABILITY_CHANGED) != 0 &&
                            !c.isDisplayable()) {
                        // System.out.println("destroying PredicateTestComboModel");
                        PredicateTestComboModel model =
                                (PredicateTestComboModel) c.getModel();
                        model.destroy();
                    }
                }
            });

            final ChangeListener listener = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    updateResult();
                }
            };
            c.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (selected != null) {
                        selected.removeChangeListener(listener);
                    }

                    if (c.getSelectedIndex() <= 0) {
                        // clear
                        selected = null;
                    } else {
                        SelectablePredicate sp = (SelectablePredicate) c
                                .getSelectedItem();
                        selected = sp.getPredicate();
                        selected.addChangeListener(listener);
                    }
                    updateResult();
                }
            });
            c.addHierarchyListener(new HierarchyListener() {
                @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    if ((e.getChangeFlags() & e.DISPLAYABILITY_CHANGED) != 0 &&
                            !c.isDisplayable() && selected != null) {
                        // System.out.println("removing change listener");
                        selected.removeChangeListener(listener);
                    }
                }
            });

            label = new JLabel();
            label.setAlignmentX(0.5f);
            vBox.add(label);

            updateResult();
        }

        private void updateResult() {
            if (selected == null) {
                // clear
                label.setText(" ");
                List<BoundingBox> patches = Collections.emptyList();
                List<BufferedImage> heatmaps = Collections.emptyList();
                image.setTestResultPatches(patches);
                image.setTestResultHeatmaps(heatmaps);
            } else {
                Cursor oldCursor = pp.getCursor();

                try {
                    pp.setCursor(Cursor
                            .getPredefinedCursor(Cursor.WAIT_CURSOR));

                    ResultRegions regions;
                    if (objectID != null) {
                        regions = m.getRegions(selected, objectID);
                    } else {
                        regions = m.getRegions(selected, encodePNM());
                    }
                    if (regions != null) {
                        label.setText("Object passed");
                    } else {
                        label.setText("Object dropped");
                        regions = new ResultRegions();
                    }
                    image.setTestResultHeatmaps(regions.getHeatmaps());
                    image.setTestResultPatches(regions.getPatches());
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } finally {
                    pp.setCursor(oldCursor);
                }
            }
        }

        private byte[] encodePNM() throws IOException {
            BufferedImage buf = new BufferedImage(img.getWidth(),
                    img.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = buf.createGraphics();
            g2.drawImage(img, 0, 0, null);
            g2.dispose();

            ByteArrayOutputStream ppmOut = new ByteArrayOutputStream();
            // System.out.println(buf);
            if (!ImageIO.write(buf, "PNM", ppmOut)) {
                throw new IOException("Can't write out PNM");
            }
            return ppmOut.toByteArray();
        }
    }

    private static class RegionsListPanel extends JPanel {
        public RegionsListPanel(List<ActivePredicate> activePredicates,
                ResultRegions regions, final ImageRegionsLabel image) {
            Box box = Box.createVerticalBox();

            setMinimumSize(new Dimension(PATCH_LIST_PREFERRED_WIDTH,
                    PATCH_LIST_MINIMUM_HEIGHT));
            setPreferredSize(new Dimension(PATCH_LIST_PREFERRED_WIDTH,
                    PATCH_LIST_MINIMUM_HEIGHT));
            setMaximumSize(new Dimension(PATCH_LIST_PREFERRED_WIDTH,
                    Integer.MAX_VALUE));
            setLayout(new BorderLayout());

            JScrollPane jsp = new JScrollPane(box,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            jsp.setBorder(BorderFactory.createTitledBorder("Result Regions"));
            jsp.getHorizontalScrollBar().setUnitIncrement(20);
            jsp.getVerticalScrollBar().setUnitIncrement(20);

            add(jsp);

            for (ActivePredicate h : activePredicates) {
                // extract patches and heatmaps
                String predicateName = h.getPredicateName();
                String name = h.getInstanceName();

                final List<BoundingBox> bbs = new ArrayList<BoundingBox>();
                final List<BufferedImage> heatmaps =
                        new ArrayList<BufferedImage>();
                double distance = 1;
                for (String fName : h.getFilterNames()) {
                    // patches
                    for (BoundingBox bb : regions.getPatches(fName)) {
                        bbs.add(bb);
                        // Find minimum distance
                        distance = Math.min(distance, bb.getDistance());
                    }

                    // heatmaps
                    BufferedImage heatmap = regions.getHeatmap(fName);
                    if (heatmap != null) {
                        heatmaps.add(heatmap);
                    }
                }

                if (bbs.size() > 0 || heatmaps.size() > 0) {
                    // System.out.println(" YES");
                    // regions found, add them
                    JCheckBox cb = new JCheckBox();
                    Formatter f = new Formatter();
                    f.format("%s", predicateName);
                    if (bbs.size() > 0) {
                        // similarity metric comes from the patches attribute
                        f.format(" (similarity %.0f%%)",
                                100 - 100.0 * distance);
                    }
                    PredicateList.updateCheckBox(cb, f.toString(), name, false);

                    cb.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            if (e.getStateChange() == ItemEvent.SELECTED) {
                                image.addResultHeatmap(heatmaps);
                                image.addResultPatch(bbs);
                            } else {
                                image.removeResultPatch(bbs);
                                image.removeResultHeatmap(heatmaps);
                            }
                        }
                    });
                    box.add(cb);
                }
            }
        }
    }

    public Image getImage() {
        return img;
    }
}
