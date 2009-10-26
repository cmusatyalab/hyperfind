/*
 *  HyperFind, an search application for the OpenDiamond platform
 *
 *  Copyright (c) 2008-2009 Carnegie Mellon University
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import edu.cmu.cs.diamond.opendiamond.Filter;
import edu.cmu.cs.diamond.opendiamond.FilterCode;

class SnapFindSearch implements HyperFindSearch {

    private final File pluginRunner;

    private final String displayName;

    private final String internalName;

    private final SnapFindSearchType type;

    private final boolean isEditable;

    private byte[] config;

    private String fspec;

    private String fspecFilterName;

    private byte[] blob;

    private String instanceName;

    private List<BufferedImage> patches;

    private File searchletLib;

    public SnapFindSearch(File pluginRunner, String displayName,
            String internalName, SnapFindSearchType type) throws IOException,
            InterruptedException {
        this.pluginRunner = pluginRunner;
        this.displayName = displayName;
        this.internalName = internalName;
        this.type = type;

        // populate the configs
        ProcessBuilder pb = new ProcessBuilder(pluginRunner.getPath(),
                "get-plugin-initial-config", type.toString(), internalName);
        System.out.println(pb.command());

        Process p = null;
        try {
            p = pb.start();

            DataInputStream in = new DataInputStream(p.getInputStream());

            Map<String, byte[]> map = SnapFindSearchFactory.readKeyValueSet(in);
            isEditable = Boolean.parseBoolean(new String(SnapFindSearchFactory
                    .getOrFail(map, "is-editable")));

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

        // fspec and digest
        byte[] fspecBytes = SnapFindSearchFactory.getOrFail(map, "fspec");
        fspecFilterName = digestFspec(fspecBytes);

        // replace the filter name with a hash of the args, etc.
        fspec = new String(fspecBytes).replace("*", "f" + fspecFilterName);

        blob = SnapFindSearchFactory.getOrFail(map, "blob");
        searchletLib = new File(new String(SnapFindSearchFactory.getOrFail(map,
                "searchlet-lib-path")));
        instanceName = new String(SnapFindSearchFactory.getOrFail(map, "name"));

        patches = new ArrayList<BufferedImage>();
        if (map.containsKey("patch-count")) {
            int patchCount = Integer
                    .parseInt(new String(map.get("patch-count")));
            for (int i = 0; i < patchCount; i++) {
                ByteArrayInputStream in = new ByteArrayInputStream(
                        SnapFindSearchFactory.getOrFail(map, "patch-" + i));
                patches.add(ImageIO.read(in));
            }
        }
    }

    private static String digestFspec(byte[] fspecBytes) {
        try {
            MessageDigest m = MessageDigest.getInstance("SHA-256");
            byte[] digest = m.digest(fspecBytes);
            System.out.println(Arrays.toString(digest));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(Integer.toString(b & 0xFF, 36));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // can't happen on java 6?
            e.printStackTrace();
        }

        return "";
    }

    @Override
    public void edit() throws IOException, InterruptedException {
        if (!isEditable) {
            return;
        }

        Process p = null;
        try {
            p = new ProcessBuilder(pluginRunner.getPath(),
                    "edit-plugin-config", type.toString(), internalName)
                    .start();

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

    private void writeConfig(OutputStream out) throws IOException {
        if (config != null) {
            writeKey(out, "config", config);
        }
        writeKey(out, "patch-count", Integer.toString(patches.size())
                .getBytes());

        writeKey(out, "name", instanceName.getBytes());

        int i = 0;
        for (BufferedImage b : patches) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(b, "ppm", baos);
            writeKey(out, "patch-" + (i++), baos.toByteArray());
        }
    }

    private static void writeKey(OutputStream out, String key, byte[] value)
            throws IOException {
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

    @Override
    public List<BoundingBox> runLocally(BufferedImage image) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toString() {
        return displayName;
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
        int threshold = -1;
        String name = null;
        String evalFunction = null;
        String initFunction = null;
        String finiFunction = null;
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

            System.out.println(" " + cmd + " " + arg);

            if (cmd.equals("FILTER")) {
                if (name != null) {
                    // commit last filter, if there is one
                    filters.add(new Filter(name, fc, evalFunction,
                            initFunction, finiFunction, threshold,
                            dependencies, arguments));
                }

                // init anew
                threshold = -1;
                name = arg;
                evalFunction = null;
                initFunction = null;
                finiFunction = null;
                dependencies = new ArrayList<String>();
                arguments = new ArrayList<String>();
            } else if (cmd.equals("THRESHOLD") || cmd.equals("THRESHHOLD")) {
                threshold = Integer.parseInt(arg);
            } else if (cmd.equals("ARG")) {
                arguments.add(arg);
            } else if (cmd.equals("EVAL_FUNCTION")) {
                evalFunction = arg;
            } else if (cmd.equals("INIT_FUNCTION")) {
                initFunction = arg;
            } else if (cmd.equals("FINI_FUNCTION")) {
                finiFunction = arg;
            } else if (cmd.equals("REQUIRES")) {
                dependencies.add(arg);
            }
        }

        // finally, commit
        filters.add(new Filter(name, fc, evalFunction, initFunction,
                finiFunction, threshold, dependencies, arguments, blob));

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
}
