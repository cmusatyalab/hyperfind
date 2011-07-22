/*
 *  HyperFind, an search application for the OpenDiamond platform
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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import edu.cmu.cs.diamond.opendiamond.Util;

public class BundledSearchFactory extends HyperFindSearchFactory {

    @Override
    public String getDisplayName() {
        return "Bundled Search";
    }

    @Override
    public HyperFindSearchType getType() {
        return HyperFindSearchType.FILTER;
    }

    @Override
    public HyperFindSearch createHyperFindSearch() throws IOException,
            InterruptedException {
        return null;
    }

    @Override
    public boolean needsPatches() {
        return false;
    }

    @Override
    public HyperFindSearch createHyperFindSearch(List<BufferedImage> patches)
            throws IOException, InterruptedException {
        return null;
    }

    /* Format of opendiamond-manifest.txt:
       Filter: display name of filter code
       Dependencies: comma-separated list of filter dependencies
       <other keys as described in SearchSettingsFrame>
    */
    public static HyperFindSearch createHyperFindSearch(InputStream in)
            throws IOException {
        // readZipFile will close in
        Map<String, byte[]> zipMap = Util.readZipFile(in);
        Properties p = Util.extractManifest(zipMap);

        String name = p.getProperty("Filter");
        if (name == null) {
            return null;
        }

        ArrayList<String> dependencies = new ArrayList<String>();
        String deplist = p.getProperty("Dependencies");
        if (deplist != null) {
            dependencies.addAll(Arrays.asList(deplist.split(",")));
        }

        byte[] filter = zipMap.get("filter");

        byte[] blob = zipMap.get("blob");
        if (blob == null) {
            blob = new byte[0];
        }

        SearchSettingsFrame settings;
        try {
            settings = SearchSettingsFrame.createFromProperties(name, p);
        } catch (IllegalArgumentException e) {
            /* Parse error reading properties */
            e.printStackTrace();
            return null;
        }

        return new BundledSearch(name, settings, filter, blob, dependencies);
    }
}
