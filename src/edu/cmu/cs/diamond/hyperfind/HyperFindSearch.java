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

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.List;
import java.util.ArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.cmu.cs.diamond.opendiamond.Filter;

public abstract class HyperFindSearch {
    private List<ChangeListener> listeners = new ArrayList<ChangeListener>();

    public abstract boolean isEditable();

    public abstract boolean needsPatches();

    public abstract void edit(Component parentComponent) throws IOException,
            InterruptedException;

    public abstract List<BoundingBox> runLocally(BufferedImage image)
            throws IOException, InterruptedException;

    public abstract List<Filter> createFilters() throws IOException;

    public abstract String getInstanceName();

    public abstract String getSearchName();

    public abstract String getDigestedName();

    public abstract void addPatches(List<BufferedImage> patches)
            throws IOException, InterruptedException;

    public abstract void dispose();

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    protected void fireChangeEvent() {
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : listeners) {
            l.stateChanged(ev);
        }
    }

    @Override
    public String toString() {
        return getSearchName();
    }

    @Override
    public int hashCode() {
        return getDigestedName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SnapFindSearch) {
            SnapFindSearch s = (SnapFindSearch) obj;
            return getDigestedName().equals(s.getDigestedName());
        }
        return false;
    }

    protected static String digest(byte[]... datas) {
        try {
            MessageDigest m = MessageDigest.getInstance("SHA-256");
            for (byte[] data : datas) {
                m.update(data);
            }
            byte[] digest = m.digest();
            // System.out.println(digest.length);
            Formatter f = new Formatter();
            for (byte b : digest) {
                f.format("%02x", b & 0xFF);
            }
            return "z" + f.toString();
        } catch (NoSuchAlgorithmException e) {
            // can't happen on java 6?
            e.printStackTrace();
        }

        return "";
    }
}
