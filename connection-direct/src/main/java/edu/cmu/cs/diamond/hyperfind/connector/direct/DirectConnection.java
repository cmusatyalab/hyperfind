/*
 * HyperFind, a search application for the OpenDiamond platform
 *
 * Copyright (c) 2009-2020 Carnegie Mellon University
 * All rights reserved.
 *
 * HyperFind is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2.
 *
 * HyperFind is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HyperFind. If not, see <http://www.gnu.org/licenses/>.
 *
 * Linking HyperFind statically or dynamically with other modules is
 * making a combined work based on HyperFind. Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *
 * In addition, as a special exception, the copyright holders of
 * HyperFind give you permission to combine HyperFind with free software
 * programs or libraries that are released under the GNU LGPL, the
 * Eclipse Public License 1.0, or the Apache License 2.0. You may copy and
 * distribute such a system following the terms of the GNU GPL for
 * HyperFind and the licenses of the other code concerned, provided that
 * you include the source code of that other code when and as the GNU GPL
 * requires distribution of source code.
 *
 * Note that people who make modified versions of HyperFind are not
 * obligated to grant this special exception for their modified versions;
 * it is their choice whether to do so. The GNU General Public License
 * gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version
 * which carries forward this exception.
 */

package edu.cmu.cs.diamond.hyperfind.connector.direct;

import edu.cmu.cs.diamond.hyperfind.connector.api.Connection;
import edu.cmu.cs.diamond.hyperfind.connector.api.ObjectId;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.Bundle;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.BundleState;
import edu.cmu.cs.diamond.opendiamond.BundleFactory;
import edu.cmu.cs.diamond.opendiamond.CookieMap;
import edu.cmu.cs.diamond.opendiamond.SearchFactory;
import edu.cmu.cs.diamond.opendiamond.Util;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import one.util.streamex.EntryStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DirectConnection implements Connection {

    private static final Logger log = LoggerFactory.getLogger(DirectConnection.class);

    private final BundleFactory bundleFactory;
    private final SearchFactory codecFactory;
    private final ExecutorService downloadExecutor;

    public DirectConnection(List<File> bundleDirectories, List<File> filterDirectories) {
        this.bundleFactory = new BundleFactory(bundleDirectories, filterDirectories);


        CookieMap defaultCookieMap = CookieMap.emptyCookieMap();
        try {
            defaultCookieMap = CookieMap.createDefaultCookieMap(null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create cookie map", e);
        }

        this.downloadExecutor = Executors.newCachedThreadPool();

        this.codecFactory = codecFactory;
    }

    @Override
    public Map<ObjectId, Image> downloadItems(Set<ObjectId> items) {
        Map<ObjectId, Future<Image>> downloadFutures =
                items.stream().collect(Collectors.toMap(i -> i, i -> downloadExecutor.submit(() -> {
                    log.info("Downloading item: {}", i.objectId());
                    return Util.extractImageFromResultIdentifier(ToDiamond.convert(i), codecFactory);
                })));

        return EntryStream.of(downloadFutures).mapValues(f -> {
            try {
                return f.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Failed to download image", e);
            }
        }).toMap();
    }

    @Override
    public List<Bundle> getBundles() {
        return bundleFactory.getBundles().stream().map(FromDiamond::convert).collect(Collectors.toList());
    }

    @Override
    public Bundle getBundle(InputStream inputStream) {
        try {
            return FromDiamond.convert(bundleFactory.getBundle(inputStream));
        } catch (IOException e) {
            throw new RuntimeException("Failed to get bundle", e);
        }
    }

    @Override
    public Bundle restoreBundle(BundleState state) {
        try {
            return FromDiamond.convert(new edu.cmu.cs.diamond.opendiamond.Bundle(
                    new edu.cmu.cs.diamond.opendiamond.Bundle.PreparedFileLoader(
                            state.bundleContents(),
                            state.memberDirs().stream().map(File::new).collect(Collectors.toList()))));
        } catch (IOException e) {
            throw new RuntimeException("Failed to restore bundle", e);
        }
    }
}
