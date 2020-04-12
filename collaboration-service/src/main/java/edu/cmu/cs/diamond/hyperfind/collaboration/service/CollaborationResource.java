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

package edu.cmu.cs.diamond.hyperfind.collaboration.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.Empty;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.Bundle;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.BundleState;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.CollaborationServiceGrpc;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.CreateSearchRequest;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.Filter;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.FilterBuilderReference;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.GetFiltersRequest;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.GetResultsDataRequest;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.GetResultsRequest;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.LabelExamplesRequest;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.LabeledExample;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.RetrainFilterRequest;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.SearchId;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.SearchResult;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.SearchResultResponse;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.SearchStats;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.UpdateCookiesRequest;
import edu.cmu.cs.diamond.hyperfind.connection.api.Connection;
import edu.cmu.cs.diamond.hyperfind.connection.api.ObjectId;
import edu.cmu.cs.diamond.hyperfind.connection.api.Search;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchFactory;
import edu.cmu.cs.diamond.hyperfind.proto.FromProto;
import edu.cmu.cs.diamond.hyperfind.proto.ToProto;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CollaborationResource extends CollaborationServiceGrpc.CollaborationServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(CollaborationResource.class);

    private final Connection connection;
    private final Map<SearchId, Search> searches;

    public CollaborationResource(Connection connection) {
        this.connection = connection;

        this.searches = Caffeine.newBuilder()
                .maximumSize(10000)
                .removalListener(new RemovalListener<SearchId, Search>() {
                    @Override
                    public void onRemoval(SearchId key, Search value, RemovalCause cause) {
                        if (!cause.equals(RemovalCause.EXPLICIT)) {
                            log.warn("Discarding non-finished search {} (cause: {})", key.getValue(), cause);
                        }

                        value.close();
                        log.info("Closed search {}", key.getValue());
                    }
                })
                .build().asMap();
    }

    @Override
    public void getResults(GetResultsRequest request, StreamObserver<SearchResult> observer) {
        try {
            SearchFactory searchFactory = buildSearchFactory(request.getFiltersList());

            List<ObjectId> objectIds =
                    request.getObjectIdsList().stream().map(FromProto::convert).collect(Collectors.toList());

            Map<ObjectId, edu.cmu.cs.diamond.hyperfind.connection.api.SearchResult> results =
                    searchFactory.getResults(objectIds, ImmutableSet.copyOf(request.getAttributesList()));
            results.values().forEach(r -> observer.onNext(ToProto.convert(r)));
            observer.onCompleted();
        } catch (RuntimeException | Error e) {
            log.error("Failed to get results", e);
            observer.onError(e);
        }
    }

    @Override
    public void getResult(GetResultsDataRequest request, StreamObserver<SearchResult> observer) {
        try {
            SearchFactory searchFactory = buildSearchFactory(request.getFiltersList());
            edu.cmu.cs.diamond.hyperfind.connection.api.SearchResult result = searchFactory.getResult(
                    request.getData().toByteArray(),
                    ImmutableSet.copyOf(request.getAttributesList()));
            observer.onNext(ToProto.convert(result));
            observer.onCompleted();
        } catch (RuntimeException | Error e) {
            log.error("Failed to get result", e);
            observer.onError(e);
        }
    }

    @Override
    public void createSearch(CreateSearchRequest request, StreamObserver<SearchId> observer) {
        try {
            SearchFactory searchFactory = buildSearchFactory(request.getFiltersList());

            SearchId searchId = SearchId.newBuilder().setValue(UUID.randomUUID().toString()).build();
            Search search = searchFactory.createSearch(ImmutableSet.copyOf(request.getAttributesList()));
            searches.put(searchId, search);
            observer.onNext(searchId);
            observer.onCompleted();
        } catch (RuntimeException | Error e) {
            log.error("Failed to create search", e);
            observer.onError(e);
        }
    }

    @Override
    public void getSearchResults(SearchId request, StreamObserver<SearchResultResponse> observer) {
        try {
            Search search = searches.get(request);
            Preconditions.checkNotNull(search, "Search %s not found", request.getValue());

            // The frontend should make sure not too many objects get sent at once, but if we run into memory
            // issues we can impleement manual flow control
            while (true) {
                Optional<edu.cmu.cs.diamond.hyperfind.connection.api.SearchResult> result = search.getNextResult();
                SearchResultResponse.Builder builder = SearchResultResponse.newBuilder();
                if (result.isPresent()) {
                    observer.onNext(builder.setResult(ToProto.convert(result.get())).build());
                } else {
                    observer.onNext(builder.setEmpty(Empty.newBuilder().build()).build());
                    break;
                }
            }

            observer.onCompleted();
        } catch (RuntimeException | Error e) {
            log.error("Failed to create search", e);
            observer.onError(e);
        }
    }

    @Override
    public void getSearchStats(SearchId request, StreamObserver<SearchStats> observer) {
        try {
            Search search = searches.get(request);
            Preconditions.checkNotNull(search, "Search %s not found", request.getValue());

            search.getStats().forEach((serverName, stats) -> observer.onNext(ToProto.convert(serverName, stats)));
            observer.onCompleted();
        } catch (RuntimeException | Error e) {
            log.error("Failed to get search stats", e);
            observer.onError(e);
        }
    }

    @Override
    public void labelExamples(LabelExamplesRequest request, StreamObserver<Empty> observer) {
        try {
            SearchId searchId = request.getSearchId();
            Search search = searches.get(searchId);
            Preconditions.checkNotNull(search, "Search %s not found", searchId.getValue());

            search.labelExamples(request.getExamplesList().stream()
                    .collect(Collectors.toMap(e -> FromProto.convert(e.getId()), LabeledExample::getLabel)));

            observer.onNext(Empty.newBuilder().build());
            observer.onCompleted();
        } catch (RuntimeException | Error e) {
            log.error("Failed to label examples", e);
            observer.onError(e);
        }
    }

    @Override
    public void retrainFilter(RetrainFilterRequest request, StreamObserver<Empty> observer) {
        try {
            SearchId searchId = request.getSearchId();
            Search search = searches.get(searchId);
            Preconditions.checkNotNull(search, "Search %s not found", searchId.getValue());

            search.retrainFilter(request.getObjectsList().stream()
                    .map(FromProto::convert)
                    .collect(Collectors.toList()));

            observer.onNext(Empty.newBuilder().build());
            observer.onCompleted();
        } catch (RuntimeException | Error e) {
            log.error("Failed to retrain filter", e);
            observer.onError(e);
        }
    }

    @Override
    public void closeSearch(SearchId request, StreamObserver<com.google.protobuf.Empty> observer) {
        try {
            searches.remove(request);
            observer.onNext(Empty.newBuilder().build());
            observer.onCompleted();
        } catch (RuntimeException | Error e) {
            log.error("Failed to close search", e);
            observer.onError(e);
        }
    }

    @Override
    public void getBundles(Empty _request, StreamObserver<Bundle> observer) {
        try {
            List<edu.cmu.cs.diamond.hyperfind.connection.api.bundle.Bundle> bundles = connection.getBundles();
            for (int i = 0; i < bundles.size(); i++) {
                observer.onNext(ToProto.convert(
                        bundles.get(i),
                        FilterBuilderReference.newBuilder().setIndex(i).build()));
            }

            observer.onCompleted();
        } catch (RuntimeException | Error e) {
            log.error("Failed to get bundles", e);
            observer.onError(e);
        }
    }

    @Override
    public void getBundle(BytesValue request, StreamObserver<Bundle> observer) {
        try {
            ByteString value = request.getValue();

            observer.onNext(ToProto.convert(
                    connection.getBundle(value.newInput()),
                    FilterBuilderReference.newBuilder().setInput(value).build()));

            observer.onCompleted();
        } catch (RuntimeException | Error e) {
            log.error("Failed to get bundle", e);
            observer.onError(e);
        }
    }

    private edu.cmu.cs.diamond.hyperfind.connection.api.bundle.Bundle getBundle(FilterBuilderReference reference) {
        switch (reference.getValueCase()) {
            case INDEX:
                return connection.getBundles().get(reference.getIndex());
            case INPUT:
                return connection.getBundle(reference.getInput().newInput());
            case BUNDLESTATE:
                return connection.restoreBundle(FromProto.convert(reference.getBundleState()));
            case VALUE_NOT_SET:
        }

        throw new IllegalArgumentException("Unrecognized filter builder reference: " + reference);
    }

    @Override
    public void restoreBundle(BundleState request, StreamObserver<Bundle> observer) {
        try {
            observer.onNext(ToProto.convert(
                    connection.restoreBundle(FromProto.convert(request)),
                    FilterBuilderReference.newBuilder().setBundleState(request).build()));

            observer.onCompleted();
        } catch (RuntimeException | Error e) {
            log.error("Failed to restore bundle", e);
            observer.onError(e);
        }
    }

    @Override
    public void updateCookies(UpdateCookiesRequest request, StreamObserver<Empty> observer) {
        try {
            connection.updateCookies(request.hasProxyIp()
                    ? Optional.of(request.getProxyIp().getValue())
                    : Optional.empty());
        } catch (RuntimeException | Error e) {
            log.error("Failed to update cookies", e);
            observer.onError(e);
        }
    }

    @Override
    public void getFilters(GetFiltersRequest request, StreamObserver<Filter> observer) {
        try {
            Optional<List<byte[]>> examples = request.getExamplesCount() > 0
                    ? Optional.of(request.getExamplesList().stream()
                    .map(ByteString::toByteArray)
                    .collect(Collectors.toList()))
                    : Optional.empty();

            List<edu.cmu.cs.diamond.hyperfind.connection.api.Filter> filters =
                    getBundle(request.getReference()).filterBuilder().getFilters(request.getOptionMapMap(), examples);

            filters.forEach(f -> observer.onNext(ToProto.convert(f)));

            observer.onCompleted();
        } catch (RuntimeException | Error e) {
            log.error("Failed to get filters", e);
            observer.onError(e);
        }
    }

    private SearchFactory buildSearchFactory(List<Filter> filters) {
        return connection.getSearchFactory(filters.stream().map(FromProto::convert).collect(Collectors.toList()));
    }
}
