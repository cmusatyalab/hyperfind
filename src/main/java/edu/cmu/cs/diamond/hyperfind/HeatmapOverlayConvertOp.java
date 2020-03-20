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

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;

class HeatmapOverlayConvertOp implements BufferedImageOp {
    private final Color overlayColor;

    public HeatmapOverlayConvertOp(Color overlayColor) {
        this.overlayColor = overlayColor;
    }

    public Color getOverlayColor() {
        return overlayColor;
    }

    private void checkColorModel(ColorModel model) {
        if (!model.hasAlpha() || model.isAlphaPremultiplied()) {
            throw new IllegalArgumentException(
                    "Unsupported destination color model");
        }
    }

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src,
            ColorModel destCM) {
        if (destCM != null) {
            checkColorModel(destCM);
        } else {
            destCM = ColorModel.getRGBdefault();
        }

        WritableRaster raster = destCM.createCompatibleWritableRaster(
                src.getWidth(), src.getHeight());
        return new BufferedImage(destCM, raster,
                destCM.isAlphaPremultiplied(), null);
    }

    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        if (dest != null) {
            if (dest.getWidth() != src.getWidth() ||
                    dest.getHeight() != src.getHeight()) {
                throw new IllegalArgumentException(
                        "Destination dimensions do not match source");
            }
            checkColorModel(dest.getColorModel());
        } else {
            dest = createCompatibleDestImage(src, null);
        }

        final int rgb = (overlayColor.getRed() << 16) |
                        (overlayColor.getGreen() << 8) |
                        overlayColor.getBlue();
        final int baseAlpha = overlayColor.getAlpha();

        int[] pixels = GraphicsUtilitiesWrapper.getPixels(src, 0, 0,
                src.getWidth(), src.getHeight(), null);
        for (int i = 0; i < pixels.length; i++) {
            int a = (pixels[i] & 0xff) * baseAlpha / 255;
            pixels[i] = (a << 24) | rgb;
        }
        GraphicsUtilitiesWrapper.setPixels(dest, 0, 0, src.getWidth(),
                src.getHeight(), pixels);

        return dest;
    }

    @Override
    public Rectangle2D getBounds2D(BufferedImage src) {
        return new Rectangle(src.getWidth(), src.getHeight());
    }

    @Override
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null) {
            dstPt = new Point();
        }
        dstPt.setLocation(srcPt);
        return dstPt;
    }

    @Override
    public RenderingHints getRenderingHints() {
        return null;
    }
}
