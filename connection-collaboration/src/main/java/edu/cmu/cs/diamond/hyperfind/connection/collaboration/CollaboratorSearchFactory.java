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

package edu.cmu.cs.diamond.hyperfind.connection.collaboration;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.ByteString;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.CollaborationServiceGrpc;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.CreateSearchRequest;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.GetResultsDataRequest;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.GetResultsRequest;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.SearchId;
import edu.cmu.cs.diamond.hyperfind.connection.api.Filter;
import edu.cmu.cs.diamond.hyperfind.connection.api.HyperFindPredicateState;
import edu.cmu.cs.diamond.hyperfind.connection.api.ObjectId;
import edu.cmu.cs.diamond.hyperfind.connection.api.Search;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchFactory;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchResult;
import edu.cmu.cs.diamond.hyperfind.connection.collaboration.grpc.BlockingStreamObserver;
import edu.cmu.cs.diamond.hyperfind.connection.collaboration.grpc.UnaryStreamObserver;
import edu.cmu.cs.diamond.hyperfind.proto.FromProto;
import edu.cmu.cs.diamond.hyperfind.proto.ToProto;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public final class CollaboratorSearchFactory implements SearchFactory {

    private final CollaborationServiceGrpc.CollaborationServiceStub service;
    private final List<edu.cmu.cs.diamond.hyperfind.collaboration.api.Filter> filters;
    private final ExecutorService resultExecutor;

    public CollaboratorSearchFactory(
            CollaborationServiceGrpc.CollaborationServiceStub service,
            List<Filter> filters,
            ExecutorService resultExecutor) {
        this.service = service;
        this.filters = filters.stream().map(ToProto::convert).collect(Collectors.toList());
        this.resultExecutor = resultExecutor;
    }

    @Override
    public Search createSearch(Set<String> attributes, List<HyperFindPredicateState> predicateState) {
        CreateSearchRequest request = CreateSearchRequest.newBuilder()
                .addAllFilters(filters)
                .addAllPredicateStates(predicateState.stream().map(ToProto::convert).collect(Collectors.toList()))
                .addAllAttributes(attributes)
                .build();

        UnaryStreamObserver<SearchId> observer = new UnaryStreamObserver<>();
        service.createSearch(request, observer);

        return new CollaboratorSearch(service, observer.value(), resultExecutor);
    }

    @Override
    public SearchResult getResult(ObjectId objectId, Set<String> attributes) {
        Map<ObjectId, SearchResult> results = getResults(ImmutableSet.of(objectId), attributes);
        Preconditions.checkArgument(results.containsKey(objectId), "Object id not found in results: %s", objectId);
        return results.get(objectId);
    }

    @Override
    public SearchResult getResult(byte[] data, Set<String> attributes) {
        GetResultsDataRequest request = GetResultsDataRequest.newBuilder()
                .setData(ByteString.copyFrom(data))
                .addAllFilters(filters)
                .addAllAttributes(attributes)
                .build();

        UnaryStreamObserver<edu.cmu.cs.diamond.hyperfind.collaboration.api.SearchResult> observer =
                new UnaryStreamObserver<>();
        service.getResult(request, observer);

        return FromProto.convert(observer.value());
    }

    @Override
    public Map<ObjectId, SearchResult> getResults(Collection<ObjectId> objectIds, Set<String> attributes) {
        GetResultsRequest request = GetResultsRequest.newBuilder()
                .addAllObjectIds(objectIds.stream().map(ToProto::convert).collect(Collectors.toList()))
                .addAllFilters(filters)
                .addAllAttributes(attributes)
                .build();

        ImmutableMap.Builder<ObjectId, SearchResult> results = ImmutableMap.builder();
        BlockingStreamObserver<edu.cmu.cs.diamond.hyperfind.collaboration.api.SearchResult> observer =
                new BlockingStreamObserver<>() {
                    @Override
                    public void onNext(edu.cmu.cs.diamond.hyperfind.collaboration.api.SearchResult value) {
                        SearchResult converted = FromProto.convert(value);
                        results.put(converted.getId(), converted);
                    }
                };

        service.getResults(request, observer);
        observer.waitForFinish();

        return results.build();
    }
}
