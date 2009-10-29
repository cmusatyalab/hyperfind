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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.graphics.GraphicsUtilities;

class ImagePatchesLabel extends JLabel {

    final List<Rectangle> drawnPatches = new ArrayList<Rectangle>();
    final List<Rectangle> resultPatches = new ArrayList<Rectangle>();

    int mouseDownX;
    int mouseDownY;

    public ImagePatchesLabel(BufferedImage img) {
        super(new ImageIcon(GraphicsUtilities.toCompatibleImage(img)));

        setHorizontalAlignment(SwingConstants.LEFT);
        setVerticalAlignment(SwingConstants.TOP);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                mouseDownX = e.getX();
                mouseDownY = e.getY();

                // create new rectangle
                Rectangle r = new Rectangle(mouseDownX, mouseDownY, 0, 0);
                drawnPatches.add(r);

                repaint();
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);

                // get current rectangle
                Rectangle r = drawnPatches.get(drawnPatches.size() - 1);

                // figure coordinates
                int x = e.getX();
                int y = e.getY();

                int x0 = Math.min(mouseDownX, x);
                int y0 = Math.min(mouseDownY, y);
                int x1 = Math.max(mouseDownX, x);
                int y1 = Math.max(mouseDownY, y);

                // mutate it
                r.setBounds(x0, y0, x1 - x0, y1 - y0);

                repaint();
            }
        });
    }

    public void clearDrawnPatches() {
        drawnPatches.clear();
        repaint();
    }

    public List<Rectangle> getDrawnPatches() {
        List<Rectangle> result = new ArrayList<Rectangle>(drawnPatches.size());

        for (Rectangle r : drawnPatches) {
            result.add(new Rectangle(r));
        }

        return result;
    }

    public void addResultPatch(Rectangle r) {
        resultPatches.add(new Rectangle(r));
        repaint();
    }

    public void clearResultPatches() {
        resultPatches.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        for (Rectangle r : resultPatches) {
            g2.setColor(Color.GREEN);
            g2.draw(r);
        }

        for (Rectangle r : drawnPatches) {
            g2.setColor(new Color(255, 0, 0, 32));
            g2.fill(r);
            g2.setColor(Color.RED);
            g2.draw(r);
        }
    }
}
