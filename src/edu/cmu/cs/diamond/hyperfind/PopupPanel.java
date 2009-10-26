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

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.cmu.cs.diamond.opendiamond.Result;

public class PopupPanel extends JPanel {
    private final BufferedImage img;
    private final List<HyperFindSearch> activeSearches;
    private final List<SnapFindSearchFactory> exampleSearchFactories;
    private final Map<String, byte[]> attributes;

    private PopupPanel(BufferedImage img, List<HyperFindSearch> activeSearches,
            List<SnapFindSearchFactory> exampleSearchFactories,
            Map<String, byte[]> attributes) {
        this.img = img;
        this.activeSearches = new ArrayList<HyperFindSearch>(activeSearches);
        this.exampleSearchFactories = new ArrayList<SnapFindSearchFactory>(
                exampleSearchFactories);
        this.attributes = new HashMap<String, byte[]>(attributes);

    }

    public static PopupPanel createInstance(Result r,
            List<HyperFindSearch> activeSearches,
            List<SnapFindSearchFactory> exampleSearchFactories)
            throws IOException {

        InputStream in = new ByteArrayInputStream(r.getData());
        BufferedImage img = ImageIO.read(in);

        Map<String, byte[]> attributes = new HashMap<String, byte[]>();
        for (String k : r.getKeys()) {
            if (!k.equals("")) {
                attributes.put(k, r.getValue(k));
            }
        }

        PopupPanel p = new PopupPanel(img, activeSearches,
                exampleSearchFactories, attributes);
        p.setLayout(new BorderLayout());
        p.add(new JLabel(new ImageIcon(img)));

        return p;
    }
}
