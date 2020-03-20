/*
 *  HyperFind, a search application for the OpenDiamond platform
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

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Graphics;
import javax.imageio.ImageIO;
import java.io.IOException;

import edu.cmu.cs.diamond.opendiamond.ObjectIdentifier;

class ResultIcon {

    private final HyperFindResult result;

    private final String name;

    private final ResultIconSetting displaySelection;
    
    private final BufferedImage originalImage;

    private static final BufferedImage checkMarkImage;

    private static final BufferedImage crossMarkImage;

    private ImageIcon icon;

    private int score;

    static {
        BufferedImage check = null;
        BufferedImage cross = null;
        try {
            check =
            ImageIO.read(ResultIcon.class.getClassLoader().getResourceAsStream(
                        "check.png"));
            cross =
            ImageIO.read(ResultIcon.class.getClassLoader().getResourceAsStream(
                        "cross.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkMarkImage = check;
        crossMarkImage = cross;
    }

    public enum ResultIconSetting {
        ICON_ONLY, LABEL_ONLY, ICON_AND_LABEL
    };

    public enum ResultType {
        Negative (0),
        Positive (1),
        Ignore   (2);

        private static class TypeHolder {
            static Map<Integer, ResultType> typeMap = new HashMap<>();
        }

        private final int value;

        private ResultType(int value) {
            this.value = value;
            TypeHolder.typeMap.put(value, this);
        }

        public int getValue() {
            return value;
        }

        public static ResultType getType(int value) {
            return TypeHolder.typeMap.get(value);
        }
    }

    public ResultIcon(HyperFindResult result, String name, ImageIcon icon,
            ResultIconSetting displaySelection, int score) {
        this.result = result;
        this.name = name;
        this.icon = icon;
        this.displaySelection = displaySelection;
        this.score = score;
        this.originalImage = (icon == null) ? null : setOriginal();
        //resize before displaying 
        if (icon != null) {
            drawOverlay(ResultType.getType(score)); 
        }
    }

    public ResultIcon(HyperFindResult result, String name, ImageIcon icon,
            ResultIconSetting displaySelection) {
        this(result, name, icon, displaySelection, 2);
    }

    private BufferedImage copyImage(BufferedImage source, double scale){
        if(scale == 1)
            return copyImage(source);

        int w = (int) (source.getWidth()*scale);
        w = (w>250) ? 250 : w;
        Image tmp = (Image) source.getScaledInstance(w, -1, Image.SCALE_SMOOTH);
        BufferedImage b = new BufferedImage(tmp.getWidth(null), tmp.getHeight(null), source.getType());
        Graphics2D g = (Graphics2D) b.getGraphics();
        g.drawImage(tmp, 0, 0, null);
        g.dispose();
        return b;
    }

    private BufferedImage copyImage(BufferedImage source){
        BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics2D g = (Graphics2D) b.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }

    public void drawOverlay(ResultType r) {
        switch(r) 
        {
            case Positive:
                drawOverlay(checkMarkImage);
                break;
            case Negative:
                drawOverlay(crossMarkImage);
                break;
            default:
                getOriginal();
        }
    }

    private void drawOverlay(BufferedImage overlay) {
        BufferedImage input = (BufferedImage) copyImage(this.originalImage);
        Graphics2D g = (Graphics2D) input.getGraphics();
        g.drawImage(input, 0, 0, null);
        g.drawImage(overlay, 0, 0, null);
        g.dispose();
        icon = new ImageIcon(input);
    }

    public BufferedImage setOriginal() {
        double scale = (score == 2) ? 1: 1.5;
        return copyImage((BufferedImage)icon.getImage(), scale);
    }

    public void getOriginal() {
        BufferedImage input = (BufferedImage) copyImage(this.originalImage);
        icon = new ImageIcon(input);
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public String getName() {
        return name;
    }

    public HyperFindResult getResult() {
        return result;
    }

    public Icon getIcon() {
        return icon;
    }

    public ResultIconSetting getDisplaySelection() {
        return displaySelection;
    }
}
