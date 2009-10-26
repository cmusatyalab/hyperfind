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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import edu.cmu.cs.diamond.opendiamond.Filter;

final class SearchList extends JPanel {
    private static class SelectableSearch {
        private boolean selected;

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public HyperFindSearch getSearch() {
            return search;
        }

        private final HyperFindSearch search;

        public SelectableSearch(HyperFindSearch search, boolean selected) {
            this.search = search;
            this.selected = selected;
        }
    }

    private static final int PREFERRED_WIDTH = 300;

    private static final int MINIMUM_HEIGHT = 300;

    private static final boolean INITIALLY_SELECTED = true;

    private final List<SelectableSearch> searches = new ArrayList<SelectableSearch>();

    private final Box box = Box.createVerticalBox();

    public SearchList() {
        setMinimumSize(new Dimension(PREFERRED_WIDTH, MINIMUM_HEIGHT));
        setPreferredSize(new Dimension(PREFERRED_WIDTH, MINIMUM_HEIGHT));
        setMaximumSize(new Dimension(PREFERRED_WIDTH, Integer.MAX_VALUE));
        setLayout(new BorderLayout());

        JScrollPane jsp = new JScrollPane(box,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp.setBorder(BorderFactory.createTitledBorder("Filters"));
        jsp.getVerticalScrollBar().setUnitIncrement(20);

        add(jsp);
    }

    public void addSearch(final HyperFindSearch s) {
        final JCheckBox cb = new JCheckBox("", INITIALLY_SELECTED);

        JButton edit = new JButton("Edit");
        JButton delete = new JButton("X");

        updateCheckBox(cb, s.getSearchName(), s.getInstanceName());

        edit.setEnabled(s.isEditable());
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    s.edit();
                    updateCheckBox(cb, s.getSearchName(), s.getInstanceName());
                    revalidate();
                    repaint();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        });

        final Box item = Box.createHorizontalBox();
        item.add(cb);
        item.add(Box.createHorizontalGlue());
        item.add(edit);
        item.add(delete);

        final SelectableSearch ss = new SelectableSearch(s, INITIALLY_SELECTED);
        searches.add(ss);
        box.add(item);

        cb.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                ss.setSelected(e.getStateChange() == ItemEvent.SELECTED);
            }
        });

        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                box.remove(item);
                boolean success = searches.remove(ss);
                assert success;
                revalidate();
                repaint();
            }
        });

        revalidate();
        repaint();
    }

    private static void updateCheckBox(JCheckBox cb, String searchName,
            String instanceName) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append(htmlEscape(searchName));
        sb.append("<br><font size=-2>");
        sb.append(htmlEscape(instanceName));
        sb.append("</font></html>");

        cb.setText(sb.toString());
    }

    private static String htmlEscape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;");
    }

    List<HyperFindSearch> getSelectedSearches() {
        List<HyperFindSearch> result = new ArrayList<HyperFindSearch>();

        for (SelectableSearch s : searches) {
            if (s.isSelected()) {
                result.add(s.getSearch());
            }
        }

        return result;
    }

    List<Filter> createFilters() throws IOException {
        List<Filter> result = new ArrayList<Filter>();

        for (SelectableSearch s : searches) {
            if (s.isSelected())
                result.addAll(s.getSearch().createFilters());
        }

        return result;
    }
}
