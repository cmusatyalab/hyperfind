/*
 *  HyperFind, an search application for the OpenDiamond platform
 *
 *  Copyright (c) 2008-2010 Carnegie Mellon University
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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.TransferHandler;

public class SearchImportTransferHandler extends TransferHandler {
    private final SearchListModel model;

    public SearchImportTransferHandler(SearchListModel model) {
        this.model = model;
    }

    private static final DataFlavor uriListFlavor;
    static {
        DataFlavor z = null;
        try {
            z = new DataFlavor("text/uri-list; class=java.lang.String");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        uriListFlavor = z;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        // System.out.println(Arrays.toString(support.getDataFlavors()));
        return support.isDataFlavorSupported(uriListFlavor);
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        try {
            List<URI> uris = getURIs(support);
            for (URI u : uris) {
                HyperFindSearch s = HyperFindSearchFactory
                        .createHyperFindSearch(u);
                if (s != null) {
                    model.addSearch(s);
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

    private List<URI> getURIs(TransferSupport support)
            throws UnsupportedFlavorException, IOException, URISyntaxException {
        String ss = (String) support.getTransferable().getTransferData(
                uriListFlavor);
        // System.out.println("\"" + ss + "\"");
        String uriList[] = ss.split("\r\n");

        List<URI> result = new ArrayList<URI>();
        for (String s : uriList) {
            if (!s.startsWith("#")) {
                URI u = new URI(s);
                result.add(u);
            }
        }

        return result;
    }
}
