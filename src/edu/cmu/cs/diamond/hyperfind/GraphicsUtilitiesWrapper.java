/*
 *  HyperFind, a search application for the OpenDiamond platform
 *
 *  Copyright (c) 2009-2014 Carnegie Mellon University
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class GraphicsUtilitiesWrapper {
    static final class WrappedClassException extends RuntimeException {}

    static private final String[] WRAPPED_CLASSES = new String[] {
        "org.jdesktop.swingx.util.GraphicsUtilities",  // SwingX >= 1.6.3
        "org.jdesktop.swingx.graphics.GraphicsUtilities",  // SwingX <= 1.6.4
    };

    static private Method toCompatibleImageImpl;
    static private Method getPixelsImpl;
    static private Method setPixelsImpl;

    static {
        // find class
        Class<?> wrappedClass = null;
        for (String className : WRAPPED_CLASSES) {
            try {
                wrappedClass = Class.forName(className);
                break;
            } catch (ClassNotFoundException e) {}
        }

        // find methods
        if (wrappedClass != null) {
            try {
                toCompatibleImageImpl = wrappedClass.getMethod(
                        "toCompatibleImage", BufferedImage.class);
                getPixelsImpl = wrappedClass.getMethod("getPixels",
                        BufferedImage.class, int.class, int.class, int.class,
                        int.class, int[].class);
                setPixelsImpl = wrappedClass.getMethod("setPixels",
                        BufferedImage.class, int.class, int.class, int.class,
                        int.class, int[].class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                wrappedClass = null;
            }
        }

        // fallback
        if (wrappedClass == null) {
            toCompatibleImageImpl = null;
            getPixelsImpl = null;
            setPixelsImpl = null;
        }
    }

    public static BufferedImage toCompatibleImage(BufferedImage img) {
        if (toCompatibleImageImpl == null) {
            throw new WrappedClassException();
        }
        try {
            return (BufferedImage) toCompatibleImageImpl.invoke(null, img);
        } catch (IllegalAccessException e) {
            throw new WrappedClassException();
        } catch (InvocationTargetException e) {
            // method doesn't throw
            throw new WrappedClassException();
        }
    }

    public static int[] getPixels(BufferedImage img, int x, int y, int w,
            int h, int[] pixels) {
        if (getPixelsImpl == null) {
            throw new WrappedClassException();
        }
        try {
            return (int[]) getPixelsImpl.invoke(null, img, x, y, w, h, pixels);
        } catch (IllegalAccessException e) {
            throw new WrappedClassException();
        } catch (InvocationTargetException e) {
            // method doesn't throw
            throw new WrappedClassException();
        }
    }

    public static void setPixels(BufferedImage img, int x, int y, int w,
            int h, int[] pixels) {
        if (setPixelsImpl == null) {
            throw new WrappedClassException();
        }
        try {
            setPixelsImpl.invoke(null, img, x, y, w, h, pixels);
        } catch (IllegalAccessException e) {
            throw new WrappedClassException();
        } catch (InvocationTargetException e) {
            // method doesn't throw
            throw new WrappedClassException();
        }
    }
}
