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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractListModel;

import edu.cmu.cs.diamond.opendiamond.Filter;

class SearchListModel extends AbstractListModel {
    private static final boolean INITIALLY_SELECTED = true;

    private final List<SelectablePredicate> predicates =
            new ArrayList<SelectablePredicate>();

    @Override
    public Object getElementAt(int index) {
        return predicates.get(index);
    }

    @Override
    public int getSize() {
        return predicates.size();
    }

    public boolean remove(SelectablePredicate sp) {
        int index = predicates.indexOf(sp);
        boolean result = index != -1;

        if (result) {
            predicates.remove(index);
            fireIntervalRemoved(this, index, index);
        }
        return result;
    }

    public void addPredicate(final HyperFindPredicate p) {
        SelectablePredicate sp = new SelectablePredicate(p,
                INITIALLY_SELECTED);
        predicates.add(sp);

        int index = predicates.size() - 1;
        fireIntervalAdded(this, index, index);
    }

    List<HyperFindPredicate> getSelectedPredicates() {
        List<HyperFindPredicate> result = new ArrayList<HyperFindPredicate>();

        for (SelectablePredicate sp : predicates) {
            if (sp.isSelected()) {
                result.add(sp.getPredicate());
            }
        }

        return result;
    }

    List<Filter> createFilters() throws IOException {
        // first, eliminate duplicates
        Set<HyperFindPredicate> set = new HashSet<HyperFindPredicate>();
        for (SelectablePredicate sp : predicates) {
            if (sp.isSelected())
                set.add(sp.getPredicate());
        }

        // System.out.println("set: " + set);

        List<Filter> result = new ArrayList<Filter>();
        for (HyperFindPredicate p : set) {
            result.addAll(p.createFilters());
        }

        return result;
    }

    public void updated(SelectablePredicate sp) {
        int index = predicates.indexOf(sp);
        if (index != -1) {
            fireContentsChanged(this, index, index);
        }
    }
}
