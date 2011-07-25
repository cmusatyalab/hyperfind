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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import edu.cmu.cs.diamond.opendiamond.Filter;
import edu.cmu.cs.diamond.opendiamond.FilterCode;

class SnapFindSearch extends HyperFindSearch {

    private final File pluginRunner;

    private final String displayName;

    private final String internalName;

    private final HyperFindSearchType type;

    private final boolean isEditable;

    private final boolean needsPatches;

    private byte[] config;

    private String fspec;

    private String fspecFilterName;

    private byte[] blob;

    private String instanceName;

    private List<BufferedImage> patches;

    private File searchletLib;

    public SnapFindSearch(File pluginRunner, String displayName,
            String internalName, HyperFindSearchType type, boolean needsPatches)
            throws IOException, InterruptedException {
        this(pluginRunner, displayName, internalName, type, needsPatches,
                new ArrayList<BufferedImage>());
    }

    public SnapFindSearch(File pluginRunner, String displayName,
            String internalName, HyperFindSearchType type,
            boolean needsPatches, List<BufferedImage> patches)
            throws IOException, InterruptedException {
        this.pluginRunner = pluginRunner;
        this.displayName = displayName;
        this.internalName = internalName;
        this.type = type;
        this.needsPatches = needsPatches;

        // populate the configs
        ProcessBuilder pb = new ProcessBuilder(pluginRunner.getPath(),
                "get-plugin-initial-config", type.toString(), internalName);
        // System.out.println(pb.command());

        Process p = null;
        try {
            p = pb.start();

            DataInputStream in = new DataInputStream(p.getInputStream());

            Map<String, byte[]> map = SnapFindSearchFactory.readKeyValueSet(in);
            isEditable = Boolean.parseBoolean(new String(SnapFindSearchFactory
                    .getOrFail(map, "is-editable"), "UTF-8"));

            readConfigs(map);
            this.patches = new ArrayList<BufferedImage>(patches);

            if (p.waitFor() != 0) {
                throw new IOException(
                        "Bad result for get-plugin-initial-config");
            }
        } finally {
            if (p != null) {
                p.destroy();
            }
        }

        reprocessConfig("normalize-plugin-config");
    }

    private void reprocessConfig(String command) throws IOException,
            InterruptedException {
        Process p = null;
        try {
            p = new ProcessBuilder(pluginRunner.getPath(), command, type
                    .toString(), internalName).start();

            OutputStream out = p.getOutputStream();

            DataInputStream in = new DataInputStream(p.getInputStream());

            writeConfig(out);
            out.close();

            Map<String, byte[]> map = SnapFindSearchFactory.readKeyValueSet(in);
            readConfigs(map);

            if (p.waitFor() != 0) {
                throw new IOException(
                        "Bad result for get-plugin-initial-config");
            }
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
    }

    private void readConfigs(Map<String, byte[]> map) throws IOException {
        config = map.get("config");

        blob = SnapFindSearchFactory.getOrFail(map, "blob");
        searchletLib = new File(new String(SnapFindSearchFactory.getOrFail(map,
                "searchlet-lib-path"), "UTF-8"));
        instanceName = new String(SnapFindSearchFactory.getOrFail(map, "name"),
                "UTF-8");

        // fspec and digest
        byte[] fspecBytes = SnapFindSearchFactory.getOrFail(map, "fspec");
        fspecFilterName = digestForFspec(fspecBytes);

        // replace the filter name with a hash of the args, etc.
        fspec = new String(fspecBytes, "UTF-8").replace("*", fspecFilterName);

        patches = new ArrayList<BufferedImage>();
        if (map.containsKey("patch-count")) {
            int patchCount = Integer.parseInt(new String(
                    map.get("patch-count"), "UTF-8"));
            for (int i = 0; i < patchCount; i++) {
                ByteArrayInputStream in = new ByteArrayInputStream(
                        SnapFindSearchFactory.getOrFail(map, "patch-" + i));
                patches.add(ImageIO.read(in));
            }
        }
    }

    private String digestForFspec(byte[] fspecBytes) {
        return digest(type.toString().getBytes(), internalName.getBytes(),
                blob, fspecBytes);
    }

    @Override
    public void edit(Component parentComponent) throws IOException,
            InterruptedException {
        reprocessConfig("edit-plugin-config");
        fireChangeEvent();
    }

    private void writeConfig(OutputStream out) throws IOException {
        if (config != null) {
            writeKey(out, "config", config);
        }
        writeKey(out, "patch-count", Integer.toString(patches.size())
                .getBytes());

        writeKey(out, "name", instanceName.getBytes());

        int i = 0;
        for (BufferedImage b : patches) {
            writeKey(out, "patch-" + (i++), encodePNM(b));
        }
    }

    private static void writeKey(OutputStream out, String key, byte[] value)
            throws IOException {
        // System.out.println(key + " " + value.length);
        byte[] bytes = key.getBytes();
        String k = "K " + bytes.length + "\n";
        out.write(k.getBytes());
        out.write(bytes);
        out.write('\n');

        String v = "V " + value.length + "\n";
        out.write(v.getBytes());
        out.write(value);
        out.write('\n');
    }

    @Override
    public boolean isEditable() {
        return isEditable;
    }

    private byte[] encodePNM(BufferedImage image) throws IOException {
        BufferedImage buf = new BufferedImage(image.getWidth(), image
                .getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = buf.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();

        ByteArrayOutputStream ppmOut = new ByteArrayOutputStream();
        // System.out.println(buf);
        if (!ImageIO.write(buf, "PNM", ppmOut)) {
            throw new IOException("Can't write out PNM");
        }
        return ppmOut.toByteArray();
    }

    private static double parseDouble(String str) {
        // Handle more forms of infinity than Double.parseDouble().
        // Copy-n-pasted from SearchOptionsFrame.
        if (str.equalsIgnoreCase("inf") || str.equalsIgnoreCase("infinity")) {
            return Double.POSITIVE_INFINITY;
        }
        if (str.equalsIgnoreCase("-inf") ||
                str.equalsIgnoreCase("-infinity")) {
            return Double.NEGATIVE_INFINITY;
        }
        return Double.parseDouble(str);
    }

    private int intOrFail(Map<String, byte[]> m, String key) throws IOException {
        return Integer.parseInt(new String(SnapFindSearchFactory.getOrFail(m,
                key), "UTF-8"));
    }

    @Override
    public List<Filter> createFilters() throws IOException {
        List<Filter> filters = new ArrayList<Filter>();

        // get FilterCode
        InputStream in = new BufferedInputStream(new FileInputStream(
                searchletLib));
        FilterCode fc = null;
        try {
            fc = new FilterCode(in);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }

        // parse fspec (boo)
        double min_score = Double.NaN;
        double max_score = Double.POSITIVE_INFINITY;
        String name = null;
        List<String> dependencies = null;
        List<String> arguments = null;

        for (String l : fspec.split("\n")) {
            l = l.trim();
            if (l.startsWith("#") || l.isEmpty()) {
                continue;
            }

            String[] tokens = l.split("[ \\t]+");
            String cmd = tokens[0];
            String arg = tokens[1];

            // System.out.println(" " + cmd + " " + arg);

            if (cmd.equals("FILTER")) {
                if (name != null) {
                    // commit last filter, if there is one
                    filters.add(new Filter(name, fc, min_score, max_score,
                            dependencies, arguments));
                }

                // init anew
                min_score = Double.NaN;
                max_score = Double.POSITIVE_INFINITY;
                name = arg;
                dependencies = new ArrayList<String>();
                arguments = new ArrayList<String>();
            } else if (cmd.equals("THRESHOLD") || cmd.equals("THRESHHOLD")) {
                min_score = parseDouble(arg);
            } else if (cmd.equals("UPPERTHRESHOLD")) {
                max_score = parseDouble(arg);
            } else if (cmd.equals("ARG")) {
                arguments.add(arg);
            } else if (cmd.equals("REQUIRES")) {
                dependencies.add(arg);
            }
        }

        // finally, commit
        filters.add(new Filter(name, fc, min_score, max_score, dependencies,
                arguments, blob));

        return filters;
    }

    @Override
    public String getSearchName() {
        return displayName;
    }

    @Override
    public String getInstanceName() {
        return instanceName;
    }

    @Override
    public List<String> getFilterNames() {
        List<String> names = new ArrayList<String>();
        // We only return the mangled filter name substituted for "*" in
        // the fspec.  We should be returning all filter names, but with
        // the current set of SnapFind plugins it doesn't matter.
        names.add(fspecFilterName);
        return names;
    }

    @Override
    public boolean needsPatches() {
        return needsPatches;
    }

    @Override
    public void addPatches(List<BufferedImage> patches) throws IOException,
            InterruptedException {
        this.patches.addAll(patches);
        reprocessConfig("normalize-plugin-config");
        fireChangeEvent();
    }

    @Override
    public void dispose() {
    }
}
