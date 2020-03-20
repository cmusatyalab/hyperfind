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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.TransferHandler;

public abstract class URIImportTransferHandler extends TransferHandler {
    protected static final DataFlavor uriListFlavor = new DataFlavor(
            "text/uri-list; class=java.lang.String", "URI list");

    @Override
    public boolean canImport(TransferSupport support) {
        if (support.isDataFlavorSupported(uriListFlavor)) {
            // assume the URIs are valid
            return true;
        } else if (support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            // may or may not contain URIs, so try to parse
            try {
                getURIs(support);
                return true;
            } catch (UnsupportedFlavorException e1) {
                return false;
            } catch (URISyntaxException e2) {
                return false;
            } catch (IOException e3) {
                return false;
            }
        } else {
            return false;
        }
    }

    protected List<URI> getURIs(TransferSupport support)
            throws IOException, UnsupportedFlavorException,
            URISyntaxException {
        Transferable t = support.getTransferable();
        String ss;
        try {
            ss = (String) t.getTransferData(uriListFlavor);
        } catch (UnsupportedFlavorException e) {
            ss = (String) t.getTransferData(DataFlavor.stringFlavor);
        }

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
