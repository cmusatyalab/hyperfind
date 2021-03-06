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

package edu.cmu.cs.diamond.hyperfind.connection.diamond;

import edu.cmu.cs.diamond.hyperfind.connection.api.Filter;
import edu.cmu.cs.diamond.hyperfind.connection.api.HyperFindPredicateState;
import edu.cmu.cs.diamond.hyperfind.connection.api.ObjectId;
import edu.cmu.cs.diamond.hyperfind.connection.api.Search;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchFactory;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchResult;
import edu.cmu.cs.diamond.opendiamond.CookieMap;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import one.util.streamex.EntryStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DiamondSearchFactory implements SearchFactory {

    private static final Logger log = LoggerFactory.getLogger(DiamondSearchFactory.class);

    private final Optional<Path> downloadPathRoot;
    private final edu.cmu.cs.diamond.opendiamond.SearchFactory delegate;
    private final ExecutorService downloadExecutor;

    public DiamondSearchFactory(
            List<Filter> filters,
            CookieMap cookieMap,
            Optional<Path> downloadPathRoot,
            ExecutorService downloadExecutor) {
        this.downloadPathRoot = downloadPathRoot;
        this.delegate = new edu.cmu.cs.diamond.opendiamond.SearchFactory(
                filters.stream().map(ToDiamond::convert).collect(Collectors.toList()),
                cookieMap);
        this.downloadExecutor = downloadExecutor;
    }

    @Override
    public Search createSearch(Set<String> attributes, List<HyperFindPredicateState> _predicateState) {
        try {
            return new DiamondSearch(
                    delegate.createSearch(attributes),
                    downloadPathRoot.map(p -> p.resolve("search-" + System.currentTimeMillis())));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to create search", e);
        }
    }

    @Override
    public SearchResult getResult(ObjectId objectId, Set<String> attributes) {
        try {
            return FromDiamond.convert(delegate.generateResult(ToDiamond.convert(objectId), attributes));
        } catch (IOException e) {
            throw new RuntimeException("Failed to get result", e);
        }
    }

    @Override
    public SearchResult getResult(byte[] data, Set<String> attributes) {
        try {
            return FromDiamond.convert(delegate.generateResult(data, attributes));
        } catch (IOException e) {
            throw new RuntimeException("Failed to get result", e);
        }
    }

    @Override
    public Map<ObjectId, SearchResult> getResults(Collection<ObjectId> objectIds, Set<String> attributes) {
        Map<ObjectId, Future<SearchResult>> downloadFutures =
                objectIds.stream().collect(Collectors.toMap(i -> i, i -> downloadExecutor.submit(() -> {
                    log.info("Fetching item: {}", i.objectId());
                    return getResult(i, attributes);
                })));

        return EntryStream.of(downloadFutures).mapValues(f -> {
            try {
                return f.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Failed to download image", e);
            }
        }).toMap();
    }
}
