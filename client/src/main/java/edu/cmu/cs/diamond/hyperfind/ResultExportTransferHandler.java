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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import edu.cmu.cs.diamond.hyperfind.connection.api.ObjectId;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchFactory;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchResult;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * Handles content export when drag from ResultList. Images are first downloaded to a temp directory. A URI list is
 * return.
 */
public class ResultExportTransferHandler extends TransferHandler {

    private static final DataFlavor URI_LIST_FLAVOR =
            new DataFlavor("text/uri-list; class=java.lang.String", "URI list");

    private static final DataFlavor TEXT_PLAIN_FLAVOR = new DataFlavor(
            "text/plain; class=java.lang.String", "Plain text");

    private static final List<DataFlavor> FLAVORS = ImmutableList.of(URI_LIST_FLAVOR, TEXT_PLAIN_FLAVOR);

    private static final int IMAGE_DOWNLOAD_BATCH_SIZE = 50;

    private final SearchFactory factory;
    private final ExecutorService executor;

    public ResultExportTransferHandler(SearchFactory factory, ExecutorService executor) {
        this.factory = factory;
        this.executor = executor;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Transferable createTransferable(JComponent c) {
        JList<ResultIcon> list = (JList<ResultIcon>) c;
        List<ResultIcon> results = list.getSelectedValuesList();
        ExportTransferable t = new ExportTransferable(results);

        // force the computation in the UI thread here to avoid terrible
        // drag and drop race conditions
        try {
            t.uriFutures.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to get uris", e);
        }

        return t;
    }

    private class ExportTransferable implements Transferable {

        private final Future<String> uriFutures;

        public ExportTransferable(List<ResultIcon> resultIcons) {
            List<Future<File>> externalDownloadFutures = new ArrayList<>();
            List<ObjectId> toReexecute = new ArrayList<>();

            for (ResultIcon icon : resultIcons) {
                // if the attribute "hyperfind.external-link" is present, use it as download link
                SearchResult result = icon.getResult().getResult();
                Optional<String> externalLink = result.getString("hyperfind.external-link");
                if (externalLink.isPresent()) {
                    externalDownloadFutures.add(executor.submit(() -> {
                        String downloadLink = externalLink.get();
                        System.out.println("Downloading from " + downloadLink);
                        URL url = new URL(downloadLink);
                        File file =
                                File.createTempFile("hyperfind-export-", "-" + FilenameUtils.getName(url.getFile()));
                        file.deleteOnExit();
                        FileUtils.copyURLToFile(url, file);
                        return file;
                    }));
                } else if (result.getBytes(SearchResult.DATA_ATTR).isPresent()) {
                    // save to disk without re-execute if the '' attr is present
                    externalDownloadFutures.add(executor.submit( () -> {
                        String ext = result.getString("hyperfind.save-ext").orElse("jpg");
                        System.out.println("Skip re-execution. Saving the '' attr to disk using ext: " + ext);
                        File file = File.createTempFile("hyperfind-export-", "."+ext);
                        file.deleteOnExit();
                        Files.write(result.getData(), file);
                        return file;
                    }));
                }
                 else {
                    toReexecute.add(result.getId());
                }
            }

            Future<List<File>> downloadFuture = executor.submit(() -> StreamSupport.stream(
                    Iterables.partition(toReexecute, IMAGE_DOWNLOAD_BATCH_SIZE).spliterator(),
                    false)
                    .flatMap(ids -> factory.getResults(ids, ImmutableSet.of(SearchResult.DATA_ATTR)).entrySet().stream()
                            .map(entry -> writeToFile(entry.getKey(), entry.getValue().getData())))
                    .collect(Collectors.toList()));

            uriFutures = executor.submit(() -> {
                StringBuilder sb = new StringBuilder();

                for (Future<File> future : externalDownloadFutures) {
                    File f = future.get();
                    URI u = f.toURI();
                    sb.append(u.toASCIIString()).append("\r\n");
                }

                for (File file : downloadFuture.get()) {
                    URI u = file.toURI();
                    sb.append(u.toASCIIString()).append("\r\n");
                }

                return sb.toString();
            });
        }

        private File writeToFile(ObjectId id, byte[] data) {
            try {
                String ext = FilenameUtils.getExtension(id.objectId());
                File file = File.createTempFile("hyperfind-export-", "." + ext);
                file.deleteOnExit();
                Files.write(data, file);

                return file;
            } catch (IOException e) {
                throw new RuntimeException("Failed to write image", e);
            }
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return FLAVORS.contains(flavor);
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return FLAVORS.toArray(new DataFlavor[0]);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {

            try {
                if (flavor.equals(URI_LIST_FLAVOR) || flavor.equals(TEXT_PLAIN_FLAVOR)) {
                    return uriFutures.get();
                } else {
                    throw new UnsupportedFlavorException(flavor);
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Failed to get transfer data", e);
            }
        }
    }
}
