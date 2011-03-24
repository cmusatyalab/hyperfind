/*
 *  HyperFind, an search application for the OpenDiamond platform
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

import edu.cmu.cs.diamond.opendiamond.Filter;
import edu.cmu.cs.diamond.opendiamond.FilterCode;

public class BundledSearch extends HyperFindSearch {

    private final String searchName;

    private String digestedName;

    private int threshold;

    private final ArrayList<String> dependencies;

    private final byte[] filter;

    private final byte[] blob;

    /* @blob may be null */
    BundledSearch(String filterName, byte[] filter, int threshold,
            Collection<String> dependencies, byte[] blob) {
        this.searchName = filterName;
        this.filter = filter;
        this.threshold = threshold;
        this.dependencies = new ArrayList<String>(dependencies);
        this.blob = blob;
        updateDigestedName();
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public boolean needsPatches() {
        return false;
    }

    @Override
    public void edit(Component parentComponent) throws IOException,
            InterruptedException {
    }

    @Override
    public List<BoundingBox> runLocally(BufferedImage image)
            throws IOException, InterruptedException {
        List<BoundingBox> empty = Collections.emptyList();
        return empty;
    }

    @Override
    public List<Filter> createFilters() throws IOException {
        List<Filter> filters = new ArrayList<Filter>();
        List<String> arguments = Collections.emptyList();
        filters.add(new Filter(digestedName, new FilterCode(filter),
                threshold, dependencies, arguments, blob));
        return filters;
    }

    @Override
    public String getInstanceName() {
        return "filter";
    }

    @Override
    public String getSearchName() {
        return searchName;
    }

    @Override
    public String getDigestedName() {
        return digestedName;
    }

    private void updateDigestedName() {
        digestedName = digest(searchName.getBytes(), filter, blob);
    }

    @Override
    public void addPatches(List<BufferedImage> patches)
            throws IOException, InterruptedException {
    }

    @Override
    public void dispose() {
    }
}
