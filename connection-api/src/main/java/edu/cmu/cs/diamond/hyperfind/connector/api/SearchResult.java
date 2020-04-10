/*
 * HyperFind, a search application for the OpenDiamond platform
 *
 * Copyright (c) 2009-2020 Carnegie Mellon University
 * All rights reserved.
 *
 * HyperFind is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2.
 *
 * HyperFind is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HyperFind. If not, see <http://www.gnu.org/licenses/>.
 *
 * Linking HyperFind statically or dynamically with other modules is
 * making a combined work based on HyperFind. Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *
 * In addition, as a special exception, the copyright holders of
 * HyperFind give you permission to combine HyperFind with free software
 * programs or libraries that are released under the GNU LGPL, the
 * Eclipse Public License 1.0, or the Apache License 2.0. You may copy and
 * distribute such a system following the terms of the GNU GPL for
 * HyperFind and the licenses of the other code concerned, provided that
 * you include the source code of that other code when and as the GNU GPL
 * requires distribution of source code.
 *
 * Note that people who make modified versions of HyperFind are not
 * obligated to grant this special exception for their modified versions;
 * it is their choice whether to do so. The GNU General Public License
 * gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version
 * which carries forward this exception.
 */

package edu.cmu.cs.diamond.hyperfind.connector.api;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

public final class SearchResult {

    public static final String DATA_ATTR = "";

    private static final String NAME_ATTR = "Display-Name";

    private final ObjectId id;
    private final Map<String, byte[]> attributes;

    public SearchResult(ObjectId id, Map<String, byte[]> attributes) {
        this.id = id;
        this.attributes = attributes;
    }

    public ObjectId getId() {
        return id;
    }

    public Set<String> getKeys() {
        return attributes.keySet();
    }

    public String getName() {
        return getString(NAME_ATTR)
                .orElseThrow(() -> new IllegalArgumentException(NAME_ATTR + " attribute not defined in earch result"));
    }

    public byte[] getData() {
        return attributes.get(DATA_ATTR);
    }

    public Optional<byte[]> getBytes(String attrName) {
        return Optional.ofNullable(attributes.get(attrName));
    }

    public OptionalInt getInt(String attrName) {
        Optional<byte[]> bytes = getBytes(attrName);

        if (bytes.isEmpty()) {
            return OptionalInt.empty();
        }

        return OptionalInt.of(ByteBuffer.wrap(bytes.get()).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt());
    }

    public OptionalLong getLong(String attrName) {
        Optional<byte[]> bytes = getBytes(attrName);

        if (bytes.isEmpty()) {
            return OptionalLong.empty();
        }

        return OptionalLong.of(ByteBuffer.wrap(bytes.get()).order(java.nio.ByteOrder.LITTLE_ENDIAN).getLong());
    }

    public OptionalDouble getDouble(String attrName) {
        Optional<byte[]> bytes = getBytes(attrName);

        if (bytes.isEmpty()) {
            return OptionalDouble.empty();
        }

        return OptionalDouble.of(Double.longBitsToDouble(ByteBuffer.wrap(bytes.get())
                .order(java.nio.ByteOrder.LITTLE_ENDIAN)
                .getLong()));
    }

    // TODO: deduplicate with opendiamond-java's Utils class
    public Optional<String> getString(String attrName) {
        return getBytes(attrName).map(value -> {
            if (value.length == 0) {
                return "";
            } else {
                return new String(value, 0, value.length - 1, StandardCharsets.UTF_8);
            }
        });
    }
}
