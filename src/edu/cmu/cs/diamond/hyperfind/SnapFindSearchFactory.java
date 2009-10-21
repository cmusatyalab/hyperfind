package edu.cmu.cs.diamond.hyperfind;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SnapFindSearchFactory implements Comparable<SnapFindSearchFactory> {

    private final File pluginRunner;

    public String getDisplayName() {
        return displayName;
    }

    public String getInternalName() {
        return internalName;
    }

    public SnapFindSearchType getType() {
        return type;
    }

    private final String displayName;

    private final String internalName;

    private final SnapFindSearchType type;

    public SnapFindSearchFactory(File pluginRunner, Map<String, byte[]> map) {
        this.pluginRunner = pluginRunner;

        displayName = new String(getOrFail(map, "display-name"));
        internalName = new String(getOrFail(map, "internal-name"));
        type = SnapFindSearchType
                .fromString(new String(getOrFail(map, "type")));
    }

    public HyperFindSearch createHyperFindSearch() throws IOException,
            InterruptedException {
        return new SnapFindSearch(pluginRunner, displayName, internalName, type);
    }

    static <V> V getOrFail(Map<String, V> map, String key) {
        V value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Cannot find key: " + key);
        }

        return value;
    }

    public static List<SnapFindSearchFactory> createSnapFindSearchFactories(
            File pluginRunner) throws IOException, InterruptedException {
        List<SnapFindSearchFactory> result = new ArrayList<SnapFindSearchFactory>();

        Process p = new ProcessBuilder(pluginRunner.getPath(), "list-plugins")
                .start();

        DataInputStream in = new DataInputStream(p.getInputStream());

        List<Map<String, byte[]>> listPluginsResult = readKeyValueSetList(in);
        for (Map<String, byte[]> map : listPluginsResult) {
            result.add(new SnapFindSearchFactory(pluginRunner, map));
        }

        if (p.waitFor() != 0) {
            throw new IOException("Bad result for list-plugins");
        }

        System.out.println(result);
        return result;
    }

    static List<Map<String, byte[]>> readKeyValueSetList(DataInputStream in)
            throws IOException {
        List<Map<String, byte[]>> result = new ArrayList<Map<String, byte[]>>();

        while (true) {
            Map<String, byte[]> m = readKeyValueSet(in);
            if (m.isEmpty()) {
                break;
            }
            result.add(m);
        }

        return result;
    }

    static Map<String, byte[]> readKeyValueSet(DataInputStream in)
            throws IOException {
        Map<String, byte[]> result = new HashMap<String, byte[]>();

        boolean inMiddle = false;
        try {
            while (true) {
                expect("K ".getBytes(), in);

                inMiddle = true;

                int keyLen = readLength(in);
                String key = new String(readData(in, keyLen));

                expect("V ".getBytes(), in);
                int valueLen = readLength(in);
                byte[] value = readData(in, valueLen);

                result.put(key, value);
                inMiddle = false;
            }
        } catch (IOException e) {
            if (inMiddle) {
                throw e;
            }
        }

        return result;
    }

    private static byte[] readData(DataInputStream in, int len)
            throws IOException {
        byte[] buf = new byte[len];
        in.readFully(buf);

        // read newline
        in.readByte();

        return buf;
    }

    private static int readLength(DataInputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int c = in.readByte() & 0xFF;
            if (c == '\n') {
                return Integer.parseInt(sb.toString());
            } else {
                sb.append((char) c);
            }
        }
    }

    private static void expect(byte[] expected, DataInputStream in)
            throws IOException {
        for (int i = 0; i < expected.length; i++) {
            int c = in.readByte() & 0xFF;
            if (c != (expected[i] & 0xFF)) {
                throw new IOException("expected " + (expected[i] & 0xFF)
                        + ", got " + c);
            }
        }
    }

    @Override
    public String toString() {
        return displayName;
    }

    @Override
    public int compareTo(SnapFindSearchFactory o) {
        return getDisplayName().compareTo(o.getDisplayName());
    }
}