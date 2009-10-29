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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractListModel;

import edu.cmu.cs.diamond.opendiamond.Filter;

class SearchListModel extends AbstractListModel {
    private static final boolean INITIALLY_SELECTED = true;
    private final List<SelectableSearch> searches = new ArrayList<SelectableSearch>();

    @Override
    public Object getElementAt(int index) {
        return searches.get(index);
    }

    @Override
    public int getSize() {
        return searches.size();
    }

    public boolean remove(SelectableSearch ss) {
        int index = searches.indexOf(ss);
        boolean result = index != -1;

        if (result) {
            searches.remove(index);
            fireIntervalRemoved(this, index, index);
        }
        return result;
    }

    public void addSearch(final HyperFindSearch s) {
        SelectableSearch ss = new SelectableSearch(s, INITIALLY_SELECTED);
        searches.add(ss);

        int index = searches.size() - 1;
        fireIntervalAdded(this, index, index);
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
        // first, eliminate duplicates
        Set<HyperFindSearch> set = new HashSet<HyperFindSearch>();
        for (SelectableSearch s : searches) {
            if (s.isSelected())
                set.add(s.getSearch());
        }

        System.out.println("set: " + set);

        List<Filter> result = new ArrayList<Filter>();
        for (HyperFindSearch s : set) {
            result.addAll(s.createFilters());
        }

        return result;
    }

    public void updated(SelectableSearch ss) {
        int index = searches.indexOf(ss);
        if (index != -1) {
            fireContentsChanged(this, index, index);
        }
    }
}
