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
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
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
            PredicateListModel model, Result prevResult) {

        Result r = hr.getResult();
        BufferedImage img = Util.extractImageFromResult(r);

        Set<String> keys= new HashSet<String>();
        keys.addAll(r.getKeys());
        if (prevResult != null) {
            keys.addAll(prevResult.getKeys());
        }

        Map<String, byte[]> attributes = new HashMap<String, byte[]>();
        for (String k : keys) {
            // skip "data" attribute
            if (!k.equals("")) {
                if(r.getValue(k)!=null)
                    attributes.put(k, r.getValue(k));
                else if(prevResult != null && prevResult.getValue(k)!=null) {
                    //get attributes from previous run
                    if (prevResult.getValue(k).length != 0)
                        attributes.put(k, prevResult.getValue(k));
                }
            }
        }
        return createInstance(m, r.getObjectIdentifier(), img, r.getData(),
                hr.getActivePredicateSet().getActivePredicates(),
                examplePredicateFactories, hr.getRegions(), attributes, model);
    }

    public static PopupPanel createInstance(Main m, HyperFindResult hr,
            List<HyperFindPredicateFactory> examplePredicateFactories,
            PredicateListModel model) {

        return createInstance(m, hr, examplePredicateFactories, model, null);
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
        
        // create leftSide only when object is image
        byte[] displayURLBytes = attributes.get("hyperfind.object-display-url");
        ImageRegionsLabel image = null;
        if (displayURLBytes == null && img != null) {
            image = new ImageRegionsLabel(img);
            Box leftSide = Box.createVerticalBox();
            leftSide.add(new RegionsListPanel(activePredicates, regions,
                    image));
            leftSide.add(new TestPredicatePanel(m, predicateListModel, image,
                    objectID, img, p));
            leftSide.add(new ExampleSearchPanel(predicateListModel, image,
                    img, examplePredicateFactories));
            hBox.add(leftSide);
        }

        // create rightSide for arbitrary data pane, image pane or text pane
        Box vBox = Box.createVerticalBox();
        if (displayURLBytes != null) {    // arbitrary data
            final String displayURL = Util.extractString(displayURLBytes);
            JButton button = new JButton("View Object");
            button.setAlignmentX(CENTER_ALIGNMENT);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    Desktop desktop = Desktop.getDesktop();
                    try {
                        desktop.browse(new URI(displayURL));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            JPanel rightSide = new JPanel();
            rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.Y_AXIS));
            rightSide.add(Box.createRigidArea(new Dimension(0, 10)));
            rightSide.add(button, BoxLayout.Y_AXIS);
            rightSide.add(Box.createRigidArea(new Dimension(0, 10)));
            rightSide.add(propertiesPane);
            vBox.add(rightSide);
        } else {
            JScrollPane scrollPane;
            if (image != null) {    // image
                scrollPane = new JScrollPane(image);
                scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
                scrollPane.getVerticalScrollBar().setUnitIncrement(20);
            } else {    // text
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
            JSplitPane rightSide = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
                    scrollPane, propertiesPane);
            int scrollPaneHeight = Math.min(700,
                    (int) scrollPane.getPreferredSize().getHeight() + 1);
            rightSide.setDividerLocation(scrollPaneHeight);
            vBox.add(rightSide);
        }

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

        private final JButton addToExistingButton;

        public ExampleSearchPanel(final PredicateListModel model,
                final ImageRegionsLabel image, BufferedImage img,
                List<HyperFindPredicateFactory> examplePredicateFactories) {
            setBorder(BorderFactory.createTitledBorder("Example Search"));

            this.model = model;
            this.image = image;
            this.img = img;

            final ListDataListener listener = new ListDataListener() {
                public void intervalAdded(ListDataEvent e) {
                    updateComponentsEnablement();
                }

                public void intervalRemoved(ListDataEvent e) {
                    updateComponentsEnablement();
                }

                public void contentsChanged(ListDataEvent e) {
                    updateComponentsEnablement();
                }
            };
            model.addListDataListener(listener);
            final ExampleSearchPanel esp = this;
            this.addHierarchyListener(new HierarchyListener() {
                @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    if ((e.getChangeFlags() & e.DISPLAYABILITY_CHANGED) != 0 &&
                            !esp.isDisplayable()) {
                        // System.out.println("deregistering ExampleSearchPanel data listener");
                        model.removeListDataListener(listener);
                    }
                }
            });

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

                            HyperFindPredicate p = f.createHyperFindPredicate(
                                    examples);
                            model.addPredicate(p);
                            p.edit();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
                predicates.add(jm);
            }

            entireButton = new JButton("Select Entire");

            clearButton = new JButton("Clear Patches");
            clearButton.setEnabled(false);

            Box hBox = Box.createHorizontalBox();
            hBox.add(entireButton);
            hBox.add(Box.createHorizontalStrut(10));
            hBox.add(clearButton);
            vBox.add(hBox);

            addToExistingButton = new JButton("Add to Existing");
            addToExistingButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JPopupMenu currentPredicates = new JPopupMenu();

                    // build menu
                    for (int i = 0; i < model.getSize(); i++) {
                        SelectablePredicate item = (SelectablePredicate)
                                model.getElementAt(i);
                        final HyperFindPredicate p = item.getPredicate();
                        if (p.needsExamples()) {
                            JMenuItem jm = new JMenuItem(p.getInstanceName());
                            jm.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    // add examples
                                    p.addExamples(createExamples());
                                    p.edit();
                                }
                            });
                            currentPredicates.add(jm);
                        }
                    }

                    // show menu
                    Component c = (Component) e.getSource();
                    currentPredicates.show(c, 0, c.getHeight());
                }
            });
            addToExistingButton.setEnabled(false);

            hBox = Box.createHorizontalBox();
            hBox.add(addButton);
            hBox.add(Box.createHorizontalStrut(10));
            hBox.add(addToExistingButton);
            vBox.add(Box.createVerticalStrut(10));
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

            boolean haveExamplePredicates = false;
            for (int i = 0; i < model.getSize(); i++) {
                SelectablePredicate item = (SelectablePredicate)
                        model.getElementAt(i);
                if (item.getPredicate().needsExamples()) {
                    haveExamplePredicates = true;
                    break;
                }
            }
            addToExistingButton.setEnabled(b && haveExamplePredicates);
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
