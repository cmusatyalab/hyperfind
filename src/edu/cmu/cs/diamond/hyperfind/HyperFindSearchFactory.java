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

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import edu.cmu.cs.diamond.opendiamond.Util;

public abstract class HyperFindSearchFactory {

    public abstract String getDisplayName();

    public abstract HyperFindSearchType getType();

    public abstract HyperFindSearch createHyperFindSearch() throws IOException,
            InterruptedException;

    public abstract boolean needsPatches();

    public abstract boolean needsBundle();

    public abstract HyperFindSearch createHyperFindSearch(
            List<BufferedImage> patches) throws IOException,
            InterruptedException;

    public abstract HyperFindSearch createHyperFindSearchFromZipMap(
            Map<String, byte[]> zipMap, Properties p);

    private static HyperFindSearch createHyperFindSearch(
            List<HyperFindSearchFactory> factories,
            Map<String, byte[]> zipMap, Properties p) {
        for (HyperFindSearchFactory f : factories) {
            // System.out.println(f);
            HyperFindSearch s = f.createHyperFindSearchFromZipMap(zipMap, p);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    public static HyperFindSearch createHyperFindSearch(
            List<HyperFindSearchFactory> factories, URI uri)
            throws IOException {
        // System.out.println("trying " + uri);
        Map<String, byte[]> zipMap = new HashMap<String, byte[]>();
        InputStream in = null;
        try {
            in = uri.toURL().openStream();
            // System.out.println("in:" + in);
            ZipInputStream zip = new ZipInputStream(in);

            // System.out.println(zip);

            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                // get the name
                String name = entry.getName();

                // get value
                zipMap.put(name, Util.readFully(zip));
            }

            // System.out.println(zipMap);

            byte manifest[] = zipMap.get("hyperfind-manifest.txt");
            Properties p = new Properties();
            if (manifest != null) {
                ByteArrayInputStream bIn = new ByteArrayInputStream(manifest);
                Reader r = new InputStreamReader(bIn, "UTF-8");
                p.load(r);
            }

            // System.out.println(p);

            HyperFindSearch s = HyperFindSearchFactory.createHyperFindSearch(
                    factories, zipMap, p);

            return s;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
        }
    }

    public static List<HyperFindSearchFactory>
            createHyperFindSearchFactories(File pluginRunner)
            throws IOException, InterruptedException {
        List<HyperFindSearchFactory> factories =
                new ArrayList<HyperFindSearchFactory>();

        for (HyperFindSearchFactory f : factoryLoader) {
            factories.add(f);
        }
        factories.addAll(SnapFindSearchFactory
                .createHyperFindSearchFactories(pluginRunner));

        Collections.sort(factories, new Comparator<HyperFindSearchFactory>() {
            @Override
            public int compare(HyperFindSearchFactory o1,
                    HyperFindSearchFactory o2) {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        });
        return factories;
    }

    private static ServiceLoader<HyperFindSearchFactory> factoryLoader = ServiceLoader
            .load(HyperFindSearchFactory.class);
}
