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

import edu.cmu.cs.diamond.hyperfind.connector.api.FeedbackObject;
import edu.cmu.cs.diamond.hyperfind.connector.api.ObjectId;
import edu.cmu.cs.diamond.hyperfind.connector.api.Search;
import edu.cmu.cs.diamond.hyperfind.connector.api.SearchResult;
import edu.cmu.cs.diamond.hyperfind.connector.api.SearchStats;
import edu.cmu.cs.diamond.opendiamond.LabeledExample;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import one.util.streamex.EntryStream;

public final class DirectSearch implements Search {

    private final edu.cmu.cs.diamond.opendiamond.Search delegate;

    public DirectSearch(edu.cmu.cs.diamond.opendiamond.Search delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<SearchResult> getNextResult() {
        try {
            return Optional.ofNullable(delegate.getNextResult()).map(FromDiamond::convert);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException("Failed to get next result", e);
        }
    }

    @Override
    public Map<String, SearchStats> getStats() {
        try {
            return EntryStream.of(delegate.getStatistics()).mapValues(FromDiamond::convert).toMap();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to get search stats", e);
        }
    }

    @Override
    public void labelExamples(Map<ObjectId, Integer> examples) {
        delegate.labelExamples(examples.entrySet().stream()
                .map(e -> new LabeledExample(ToDiamond.convert(e.getKey()), e.getValue()))
                .collect(Collectors.toSet()));
    }

    @Override
    public void retrainFilter(Collection<FeedbackObject> objects) {
        try {
            delegate.retrainFilter(objects.stream()
                    .collect(Collectors.toMap(o -> o.id().objectId(), ToDiamond::convert)));
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException("Failed to retrain filter", e);
        }
    }

    @Override
    public void close() {
        delegate.close();
    }
}
