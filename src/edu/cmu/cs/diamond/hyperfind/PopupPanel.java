/*
 *  HyperFind, an search application for the OpenDiamond platform
 *
 *  Copyright (c) 2008-2009 Carnegie Mellon University
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import edu.cmu.cs.diamond.opendiamond.Result;
import edu.cmu.cs.diamond.opendiamond.Util;

public class PopupPanel extends JPanel {
    private static final int PATCH_LIST_MINIMUM_HEIGHT = 300;

    private static final int PATCH_LIST_PREFERRED_WIDTH = 300;

    private final BufferedImage img;

    private final List<HyperFindSearch> activeSearches;

    private final List<SnapFindSearchFactory> exampleSearchFactories;

    private final Map<String, byte[]> attributes;

    private PopupPanel(BufferedImage img, List<HyperFindSearch> activeSearches,
            List<SnapFindSearchFactory> exampleSearchFactories,
            Map<String, byte[]> attributes) {
        this.img = img;
        this.activeSearches = new ArrayList<HyperFindSearch>(activeSearches);
        this.exampleSearchFactories = new ArrayList<SnapFindSearchFactory>(
                exampleSearchFactories);
        this.attributes = new HashMap<String, byte[]>(attributes);

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
            return BoundingBox.fromPatchesList(ByteBuffer.wrap(value))
                    .toString();
        } else {
            return Util.extractString(value);
        }
    }

    public static PopupPanel createInstance(Result r,
            List<HyperFindSearch> activeSearches,
            List<SnapFindSearchFactory> exampleSearchFactories)
            throws IOException {

        InputStream in = new ByteArrayInputStream(r.getData());
        BufferedImage img = ImageIO.read(in);

        Map<String, byte[]> attributes = new HashMap<String, byte[]>();
        for (String k : r.getKeys()) {
            // skip "data" attribute
            if (!k.equals("")) {
                attributes.put(k, r.getValue(k));
            }
        }
        return createInstance(img, activeSearches, exampleSearchFactories,
                attributes);
    }

    public static PopupPanel createInstance(BufferedImage img,
            List<HyperFindSearch> activeSearches,
            List<SnapFindSearchFactory> exampleSearchFactories) {
        Map<String, byte[]> attributes = Collections.emptyMap();

        return createInstance(img, activeSearches, exampleSearchFactories,
                attributes);
    }

    private static PopupPanel createInstance(BufferedImage img,
            List<HyperFindSearch> activeSearches,
            List<SnapFindSearchFactory> exampleSearchFactories,
            final Map<String, byte[]> attributes) {
        PopupPanel p = new PopupPanel(img, activeSearches,
                exampleSearchFactories, attributes);

        // image pane
        ImagePatchesLabel image = new ImagePatchesLabel(img);
        JScrollPane imagePane = new JScrollPane(image);
        imagePane.getHorizontalScrollBar().setUnitIncrement(20);
        imagePane.getVerticalScrollBar().setUnitIncrement(20);

        // sort keys
        final List<String> keys = new ArrayList<String>(attributes.keySet());
        Collections.sort(keys);

        TableModel model = new AbstractTableModel() {
            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public int getRowCount() {
                return keys.size();
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                String key = keys.get(rowIndex);

                switch (columnIndex) {
                case 0:
                    return key;
                case 1:
                    return attributeToString(key, attributes.get(key));
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

        leftSide.add(createPatchesList(activeSearches, attributes, image));

        hBox.add(leftSide);

        JSplitPane rightSide = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
                imagePane, propertiesPane);

        Box vBox = Box.createVerticalBox();
        vBox.add(rightSide);
        hBox.add(vBox);

        // done
        p.setLayout(new BorderLayout());
        p.add(hBox);

        return p;
    }

    private static JPanel createPatchesList(
            List<HyperFindSearch> activeSearches,
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

        for (HyperFindSearch h : activeSearches) {
            // extract patches
            String searchName = h.getSearchName();
            String name = h.getInstanceName();
            String mName = h.getMangledName();

            String key = "_filter." + mName + ".patches";

            System.out.println(key);
            if (attributes.containsKey(key)) {
                System.out.println(" YES");
                // patches found, add them
                final List<BoundingBox> bb = BoundingBox
                        .fromPatchesList(ByteBuffer.wrap(attributes.get(key)));

                JCheckBox cb = new JCheckBox();
                Formatter f = new Formatter();
                f.format("%s (similarity %.0f%%)", searchName, 100 - 100.0 * bb
                        .get(0).getDistance());
                SearchList.updateCheckBox(cb, f.toString(), name); // TODO
                // distance
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
}
