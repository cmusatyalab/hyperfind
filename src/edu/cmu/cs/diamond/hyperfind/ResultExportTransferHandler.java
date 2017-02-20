/*
 *  HyperFind, a search application for the OpenDiamond platform
 *
 *  Copyright (c) 2010 Carnegie Mellon University
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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import edu.cmu.cs.diamond.opendiamond.SearchFactory;
import edu.cmu.cs.diamond.opendiamond.Util;

/**
 * Handles content export when drag from ResultList.
 * Images are first downloaded to a temp directory.
 * A URI list is return.
 */
public class ResultExportTransferHandler extends TransferHandler {
    private static final DataFlavor uriListFlavor = new DataFlavor(
            "text/uri-list; class=java.lang.String", "URI list");

    private static final DataFlavor textPlainFlavor = new DataFlavor(
            "text/plain; class=java.lang.String", "Plain text");

    private static final List<DataFlavor> flavors = new ArrayList<DataFlavor>();
    static {
        flavors.add(uriListFlavor);
        flavors.add(textPlainFlavor);
    }

    private final SearchFactory factory;

    private final ExecutorService executor;

    private class ExportTransferable implements Transferable {
        private final List<Future<File>> futureFiles;

        private final Future<String> futureURIList;

        public ExportTransferable(final List<ResultIcon> results) {
            futureFiles = new ArrayList<Future<File>>();
            for (final ResultIcon r : results) {
                futureFiles.add(executor.submit(new Callable<File>() {
                    @Override
                    public File call() throws Exception {
                        // System.out.println("running...");
                        BufferedImage img = Util
                                .extractImageFromResultIdentifier(r
                                        .getResult().getResult()
                                        .getObjectIdentifier(), factory);
                        File f = File.createTempFile("hyperfind-export-",
                                ".png");
                        f.deleteOnExit();

                        ImageIO.write(img, "png", f);

                        // System.out.println("done");

                        return f;
                    }
                }));
            }

            futureURIList = executor.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    // System.out.println(f);

                    StringBuilder sb = new StringBuilder();

                    for (Future<File> future : futureFiles) {
                        File f = future.get();
                        URI u = f.toURI();
                        sb.append(u.toASCIIString() + "\r\n");
                    }
                    return sb.toString();
                }
            });
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            // System.out.println(flavor);
            return flavors.contains(flavor);
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return flavors.toArray(new DataFlavor[0]);
        }

        @Override
        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException {
            // System.out.println(flavor);

            try {
                if (flavor.equals(uriListFlavor)
                        || flavor.equals(textPlainFlavor)) {
                    String uriList = futureURIList.get();
                    // System.out.println(uriList);
                    return uriList;
                } else {
                    throw new UnsupportedFlavorException(flavor);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (ExecutionException e) {
                Throwable c = e.getCause();
                if (c instanceof RuntimeException) {
                    throw (RuntimeException) c;
                } else if (c instanceof IOException) {
                    throw (IOException) c;
                }
                e.printStackTrace();
                return null;
            }
        }
    }

    public ResultExportTransferHandler(SearchFactory factory,
            ExecutorService executor) {
        this.factory = factory;
        this.executor = executor;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        // System.out.println("****create transferable");

        JList list = (JList) c;

        final Object[] values = list.getSelectedValues();

        List<ResultIcon> results = new ArrayList<ResultIcon>();

        for (Object o : values) {
            ResultIcon r = (ResultIcon) o;
            results.add(r);
        }

        ExportTransferable t = new ExportTransferable(results);

        // force the computation in the UI thread here to avoid terrible
        // drag and drop race conditions
        try {
            t.futureURIList.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            Throwable th = e.getCause();
            if (th instanceof RuntimeException) {
                throw (RuntimeException) th;
            }
            e.printStackTrace();
        }

        return t;
    }
}
