/*
 *  HyperFind, a search application for the OpenDiamond platform
 *
 *  Copyright (c) 2008-2011 Carnegie Mellon University
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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.cmu.cs.diamond.opendiamond.Bundle;
import edu.cmu.cs.diamond.opendiamond.Filter;

public class HyperFindPredicate {
    private final List<ChangeListener> listeners =
            new ArrayList<ChangeListener>();

    private final Bundle bundle;

    private final BundleOptionsFrame frame;

    private List<Filter> cachedFilters;

    HyperFindPredicate(Bundle bundle) throws IOException {
        this.bundle = bundle;
        if (bundle.isCodec()) {
            this.frame = new BundleOptionsFrame(bundle.getDisplayName(),
                    bundle.getOptions());
        } else {
            this.frame = new BundleOptionsFrame(bundle.getDisplayName(),
                    "untitled", bundle.getOptions());
        }

        frame.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // Invalidate any cached filters
                cachedFilters = null;
                // Pass option changes along to our listeners
                fireChangeEvent();
            }
        });
    }

    public boolean isEditable() {
        return frame.isEditable();
    }

    public boolean needsExamples() {
        return frame.needsExamples();
    }

    public String getPredicateName() {
        return bundle.getDisplayName();
    }

    public String getInstanceName() {
        return frame.getInstanceName();
    }

    public List<String> getFilterNames() {
        List<String> names = new ArrayList<String>();
        try {
            for (Filter f : createFilters()) {
                names.add(f.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return names;
    }

    public void edit(Component parentComponent) {
        frame.setVisible(true);
    }

    public void addExamples(List<BufferedImage> examples) {
        frame.addExamples(examples);
    }

    public List<Filter> createFilters() throws IOException {
        if (cachedFilters == null) {
            List<Filter> list;
            if (frame.needsExamples()) {
                list = bundle.getFilters(frame.getOptionMap(),
                        frame.getExamples());
            } else {
                list = bundle.getFilters(frame.getOptionMap());
            }
            cachedFilters = Collections.unmodifiableList(list);
        }
        return cachedFilters;
    }

    public void dispose() {
        frame.dispose();
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChangeEvent() {
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : listeners) {
            l.stateChanged(ev);
        }
    }

    @Override
    public String toString() {
        return getPredicateName();
    }

    @Override
    public int hashCode() {
        return getPredicateName().hashCode() + getFilterNames().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HyperFindPredicate) {
            HyperFindPredicate p = (HyperFindPredicate) obj;
            return getPredicateName().equals(p.getPredicateName()) &&
                    getFilterNames().equals(p.getFilterNames());
        }
        return false;
    }
}
