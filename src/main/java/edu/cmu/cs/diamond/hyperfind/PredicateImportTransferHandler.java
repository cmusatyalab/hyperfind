/*
 *  HyperFind, a search application for the OpenDiamond platform
 *
 *  Copyright (c) 2008-2011 Carnegie Mellon University
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

import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.imageio.ImageIO;

import edu.cmu.cs.diamond.opendiamond.BundleFactory;

public class PredicateImportTransferHandler extends URIImportTransferHandler {
    private final Main main;

    private final PredicateListModel model;

    private final BundleFactory bundleFactory;

    public PredicateImportTransferHandler(Main main, PredicateListModel model,
            BundleFactory bundleFactory) {
        this.main = main;
        this.model = model;
        this.bundleFactory = bundleFactory;
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        try {
            List<URI> uris = getURIs(support);
            for (URI u : uris) {
                try {
                    // first try to load it as a predicate bundle
                    HyperFindPredicate p = HyperFindPredicateFactory
                            .createHyperFindPredicate(bundleFactory, u);
                    model.addPredicate(p);
                    p.edit();
                } catch (IOException e) {
                    /* XXX If more than one image is contained in the list
                     * only the last one will shown in popup,
                      * because there is only one popup window and previous images will be overwritten.*/
                    // now try to read it as an example image
                    System.out.println("Image " + u.toURL() + " is dropped in predicate list. Opening in pop up.");
                    BufferedImage img = ImageIO.read(u.toURL());
                    main.popup(u.toString(), img);
                }

            }
        } catch (UnsupportedFlavorException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
