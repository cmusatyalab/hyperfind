/*
 *  HyperFind, a search application for the OpenDiamond platform
 *
 *  Copyright (c) 2009-2012 Carnegie Mellon University
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

import edu.cmu.cs.diamond.hyperfind.connector.api.SearchResult;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;

public class ResultRegions {

    private final Map<String, List<BoundingBox>> patches =
            new HashMap<String, List<BoundingBox>>();

    private final Map<String, BufferedImage> heatmaps =
            new HashMap<String, BufferedImage>();

    ResultRegions() {
        // construct a dummy object with no regions
    }

    ResultRegions(Collection<String> filterNames, SearchResult r) {
        for (String name : filterNames) {
            // patches
            byte[] patch = r.attributes().get(getPatchAttributeName(name));
            if (patch != null) {
                patches.put(name, BoundingBox.fromPatchesList(patch));
            }

            // heatmap
            byte[] heatmap = r.attributes().get(getHeatmapAttributeName(name));
            if (heatmap != null) {
                ByteArrayInputStream in = new ByteArrayInputStream(heatmap);
                try {
                    heatmaps.put(name, ImageIO.read(in));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<BoundingBox> getPatches() {
        List<BoundingBox> boxes = new ArrayList<BoundingBox>();
        for (List<BoundingBox> cur : patches.values()) {
            boxes.addAll(cur);
        }
        return boxes;
    }

    public List<BoundingBox> getPatches(String filterName) {
        if (patches.containsKey(filterName)) {
            return patches.get(filterName);
        } else {
            return Collections.emptyList();
        }
    }

    public List<BufferedImage> getHeatmaps() {
        return new ArrayList<BufferedImage>(heatmaps.values());
    }

    public BufferedImage getHeatmap(String filterName) {
        return heatmaps.get(filterName);
    }

    static Set<String> getPushAttributes(Collection<String> filterNames) {
        Set<String> attrs = new HashSet<String>();
        for (String name : filterNames) {
            attrs.add(getPatchAttributeName(name));
            attrs.add(getHeatmapAttributeName(name));
            attrs.add(getScoreAttributeName(name));
        }
        return attrs;
    }

    private static String getPatchAttributeName(String filterName) {
        return "_filter." + filterName + ".patches";
    }

    private static String getScoreAttributeName(String filterName) {
        return "_filter." + filterName + "_score";
    }
    private static String getHeatmapAttributeName(String filterName) {
        return "_filter." + filterName + ".heatmap.png";
    }
}
