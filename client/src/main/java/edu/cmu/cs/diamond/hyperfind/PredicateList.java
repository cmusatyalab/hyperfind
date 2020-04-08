/*
 *  HyperFind, a search application for the OpenDiamond platform
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

final class PredicateList extends JPanel implements ListDataListener {
    private static class ListElement {
        public ListElement(JCheckBox cb, Box box) {
            this.cb = cb;
            this.box = box;

        }

        public JCheckBox getCb() {
            return cb;
        }

        public Box getBox() {
            return box;
        }

        private final JCheckBox cb;

        private final Box box;
    }

    private static final int PREFERRED_WIDTH = 300;

    private static final int MINIMUM_HEIGHT = 300;

    private final Box box = Box.createVerticalBox();

    private final PredicateListModel model;

    private final List<ListElement> elements = new ArrayList<ListElement>();

    public PredicateList(final PredicateListModel model) {
        this.model = model;
        model.addListDataListener(this);

        setMinimumSize(new Dimension(PREFERRED_WIDTH, MINIMUM_HEIGHT));
        setPreferredSize(new Dimension(PREFERRED_WIDTH, MINIMUM_HEIGHT));
        setMaximumSize(new Dimension(PREFERRED_WIDTH, Integer.MAX_VALUE));
        setLayout(new BorderLayout());

        JScrollPane jsp = new JScrollPane(box,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp.setBorder(BorderFactory.createTitledBorder("Predicates"));
        jsp.getHorizontalScrollBar().setUnitIncrement(20);
        jsp.getVerticalScrollBar().setUnitIncrement(20);

        add(jsp);

        // Ensure all filters are disposed when the containing window is
        // closed
        addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if (!e.getComponent().isDisplayable()) {
                    while (model.getSize() > 0) {
                        SelectablePredicate sp = (SelectablePredicate)
                                model.getElementAt(0);
                        HyperFindPredicate p = sp.getPredicate();
                        model.remove(sp);
                        p.dispose();
                    }
                }
            }
        });
    }

    static void updateCheckBox(JCheckBox cb, String predicateName,
            String instanceName, boolean needsExamples) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append(htmlEscape(predicateName));
        sb.append("<br><font size=-2>");
        sb.append(htmlEscape(instanceName));
        if (needsExamples) {
            sb.append("<br><font color=red>Needs examples</font>");
        }
        sb.append("</font></html>");

        cb.setText(sb.toString());
    }

    private static String htmlEscape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;");
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        SelectablePredicate sp = (SelectablePredicate) model.getElementAt(e
                .getIndex0());
        ListElement element = elements.get(e.getIndex0());
        HyperFindPredicate p = sp.getPredicate();

        updateCheckBox(element.getCb(), p.getPredicateName(),
                p.getInstanceName(), p.missingExamples());
        revalidate();
        repaint();
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
        final SelectablePredicate sp = (SelectablePredicate)
                model.getElementAt(e.getIndex0());
        final HyperFindPredicate p = sp.getPredicate();

        p.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                model.updated(sp);
            }
        });

        JCheckBox cb = new JCheckBox("", sp.isSelected());

        JButton edit = new JButton("Edit");
        JButton delete = new JButton("X");
        JButton up = new JButton("\u2191"); // up arrow

        updateCheckBox(cb, p.getPredicateName(), p.getInstanceName(),
                p.missingExamples());

        edit.setEnabled(p.isEditable());
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                p.edit();
                model.updated(sp);
            }
        });

        Box item = Box.createHorizontalBox();
        item.add(cb);
        item.add(Box.createHorizontalGlue());
        item.add(edit);
        item.add(delete);
        item.add(up);

        box.add(item, e.getIndex0());

        elements.add(e.getIndex0(), new ListElement(cb, item));

        cb.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                sp.setSelected(e.getStateChange() == ItemEvent.SELECTED);
                model.updated(sp);
            }
        });

        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.remove(sp);
                p.dispose();
            }
        });

        up.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.moveUp(sp);
            }
        });

        revalidate();
        repaint();
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {

        ListElement element = elements.remove(e.getIndex0());

        box.remove(element.getBox());

        revalidate();
        repaint();

    }
}
