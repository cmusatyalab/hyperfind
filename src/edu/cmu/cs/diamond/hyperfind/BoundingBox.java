/*
 *  HyperFind, a search application for the OpenDiamond platform
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class BoundingBox {
    private final int x0;
    private final int y0;
    private final int x1;
    private final int y1;
    private final double distance;

    public BoundingBox(int x0, int y0, int x1, int y1, double distance) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.distance = distance;
    }

    public int getX0() {
        return x0;
    }

    public int getY0() {
        return y0;
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public double getDistance() {
        return distance;
    }

    public static List<BoundingBox> fromPatchesList(byte[] patches) {
        ByteBuffer bb = ByteBuffer.wrap(patches);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        int count = bb.getInt();
        double distance = bb.getDouble();

        List<BoundingBox> result = new ArrayList<BoundingBox>(count);

        for (int i = 0; i < count; i++) {
            int x0 = bb.getInt();
            int y0 = bb.getInt();
            int x1 = bb.getInt();
            int y1 = bb.getInt();

            result.add(new BoundingBox(x0, y0, x1, y1, distance));
        }

        return result;
    }

    @Override
    public String toString() {
        Formatter f = new Formatter();
        f.format("(%d,%d), (%d,%d), distance: %g", x0, y0, x1, y1, distance);
        return f.toString();
    }
}
