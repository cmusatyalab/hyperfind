/*
 *  HyperFind, a search application for the OpenDiamond platform
 *
 *  Copyright (c) 2009-2011 Carnegie Mellon University
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.cmu.cs.diamond.opendiamond.SearchFactory;

public class ActivePredicateSet {

    private final Main main;

    // Display-name -> filter bindings for the predicates listed in the UI
    private final List<ActivePredicate> predicates;

    private final SearchFactory factory;

    public ActivePredicateSet(Main main,
            List<HyperFindPredicate> selectedPredicates,
            SearchFactory factory) {
        this.main = main;
        this.predicates = new ArrayList<ActivePredicate>(
                selectedPredicates.size());
        for (HyperFindPredicate p : selectedPredicates) {
            this.predicates.add(new ActivePredicate(p.getPredicateName(),
                    p.getInstanceName(), p.getFilterNames()));
        }
        this.factory = factory;
    }

    Main getMain() {
        return main;
    }

    public List<ActivePredicate> getActivePredicates() {
        return Collections.unmodifiableList(predicates);
    }

    public List<String> getFilterNames() {
        List<String> names = new ArrayList<String>();
        for (ActivePredicate p : predicates) {
            names.addAll(p.getFilterNames());
        }
        return names;
    }

    public SearchFactory getSearchFactory() {
        return factory;
    }
}
