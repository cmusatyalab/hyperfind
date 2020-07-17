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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.google.protobuf.util.Timestamps;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.CollaborationServiceGrpc;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.FilterBuilderReference;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.GetFiltersRequest;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.SearchId;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.UpdateCookiesRequest;
import edu.cmu.cs.diamond.hyperfind.connection.api.Connection;
import edu.cmu.cs.diamond.hyperfind.connection.api.Filter;
import edu.cmu.cs.diamond.hyperfind.connection.api.RunningSearch;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchFactory;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchInfo;
import edu.cmu.cs.diamond.hyperfind.connection.api.bundle.Bundle;
import edu.cmu.cs.diamond.hyperfind.connection.api.bundle.BundleState;
import edu.cmu.cs.diamond.hyperfind.connection.api.bundle.FilterBuilder;
import edu.cmu.cs.diamond.hyperfind.grpc.BlockingStreamObserver;
import edu.cmu.cs.diamond.hyperfind.grpc.Channels;
import edu.cmu.cs.diamond.hyperfind.grpc.UnaryStreamObserver;
import edu.cmu.cs.diamond.hyperfind.proto.FromProto;
import edu.cmu.cs.diamond.hyperfind.proto.ToProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public final class CollaborationConnection implements Connection {

    private final CollaborationServiceGrpc.CollaborationServiceStub service;
    private final ExecutorService resultExecutor;

    public CollaborationConnection(String host, String port) {
        this(host, Integer.parseInt(port), false, Optional.empty());
    }

    public CollaborationConnection(String host, String port, String useSsl) {
        this(host, Integer.parseInt(port), Boolean.parseBoolean(useSsl), Optional.empty());
    }

    public CollaborationConnection(String host, String port, String useSsl, String trustStorePath) {
        this(host, Integer.parseInt(port), Boolean.parseBoolean(useSsl), Optional.of(trustStorePath));
    }

    private CollaborationConnection(String host, int port, boolean useSsl, Optional<String> trustStorePath) {
        ManagedChannel channel = useSsl
                ? Channels.createSslChannel(host, port, trustStorePath)
                : ManagedChannelBuilder.forAddress(host, port)
                        .maxInboundMessageSize(Integer.MAX_VALUE)
                        .usePlaintext()
                        .build();

        this.service = CollaborationServiceGrpc.newStub(channel);

        this.resultExecutor = Executors.newCachedThreadPool();
    }

    @Override
    public SearchFactory getSearchFactory(List<Filter> filters) {
        return new CollaboratorSearchFactory(service, filters, resultExecutor);
    }

    @Override
    public List<SearchInfo> getRunningSearches() {
        List<SearchInfo> searches = new ArrayList<>();

        BlockingStreamObserver<edu.cmu.cs.diamond.hyperfind.collaboration.api.SearchInfo> observer =
                new BlockingStreamObserver<>() {
                    @Override
                    public void onNext(edu.cmu.cs.diamond.hyperfind.collaboration.api.SearchInfo value) {
                        searches.add(SearchInfo.of(
                                () -> getSearch(value.getSearchId()),
                                Instant.ofEpochMilli(Timestamps.toMillis(value.getStartTime()))));
                    }
                };

        service.getRunningSearches(Empty.newBuilder().build(), observer);
        observer.waitForFinish();

        return searches;
    }

    private RunningSearch getSearch(SearchId searchId) {
        UnaryStreamObserver<edu.cmu.cs.diamond.hyperfind.collaboration.api.RunningSearch> observer =
                new UnaryStreamObserver<>();
        service.getSearch(searchId, observer);
        edu.cmu.cs.diamond.hyperfind.collaboration.api.RunningSearch search = observer.value();
        return RunningSearch.of(
                new CollaboratorSearch(service, searchId, resultExecutor),
                search.getFiltersList().stream().map(FromProto::convert).collect(Collectors.toList()),
                search.getPredicateStatesList().stream().map(FromProto::convert).collect(Collectors.toList()));
    }

    @Override
    public List<Bundle> getBundles() {
        List<Bundle> bundles = new ArrayList<>();

        BlockingStreamObserver<edu.cmu.cs.diamond.hyperfind.collaboration.api.Bundle> observer =
                new BlockingStreamObserver<>() {
                    @Override
                    public void onNext(edu.cmu.cs.diamond.hyperfind.collaboration.api.Bundle value) {
                        FilterBuilderReference reference = FilterBuilderReference.newBuilder()
                                .setIndex(bundles.size())
                                .build();

                        bundles.add(FromProto.convert(value, new DelegatingFilterBuilder(reference)));
                    }
                };

        service.getBundles(Empty.newBuilder().build(), observer);
        observer.waitForFinish();

        return bundles;
    }

    @Override
    public Bundle getBundle(InputStream inputStream) {
        try {
            UnaryStreamObserver<edu.cmu.cs.diamond.hyperfind.collaboration.api.Bundle> observer =
                    new UnaryStreamObserver<>();
            ByteString contents = ByteString.readFrom(inputStream);
            service.getBundle(BytesValue.newBuilder().setValue(contents).build(), observer);

            FilterBuilderReference reference = FilterBuilderReference.newBuilder().setInput(contents).build();
            return FromProto.convert(observer.value(), new DelegatingFilterBuilder(reference));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from input stream", e);
        }
    }

    @Override
    public Bundle restoreBundle(BundleState state) {
        UnaryStreamObserver<edu.cmu.cs.diamond.hyperfind.collaboration.api.Bundle> obs = new UnaryStreamObserver<>();
        edu.cmu.cs.diamond.hyperfind.collaboration.api.BundleState converted = ToProto.convert(state);
        service.restoreBundle(converted, obs);

        FilterBuilderReference reference = FilterBuilderReference.newBuilder().setBundleState(converted).build();
        return FromProto.convert(obs.value(), new DelegatingFilterBuilder(reference));
    }

    @Override
    public void updateCookies(Optional<String> proxyIp) {
        UpdateCookiesRequest.Builder request = UpdateCookiesRequest.newBuilder();
        proxyIp.ifPresent(p -> request.setProxyIp(StringValue.newBuilder().setValue(p).build()));
        UnaryStreamObserver<Empty> observer = new UnaryStreamObserver<>();
        service.updateCookies(request.build(), observer);
        observer.waitForFinish();
    }

    private final class DelegatingFilterBuilder implements FilterBuilder {

        private final FilterBuilderReference reference;

        private DelegatingFilterBuilder(FilterBuilderReference reference) {
            this.reference = reference;
        }

        @Override
        public List<Filter> getFilters(Map<String, String> optionMap, Optional<List<byte[]>> examples) {
            ImmutableList.Builder<Filter> filters = ImmutableList.builder();

            BlockingStreamObserver<edu.cmu.cs.diamond.hyperfind.collaboration.api.Filter> observer =
                    new BlockingStreamObserver<>() {
                        @Override
                        public void onNext(edu.cmu.cs.diamond.hyperfind.collaboration.api.Filter value) {
                            filters.add(FromProto.convert(value));
                        }
                    };

            GetFiltersRequest.Builder request = GetFiltersRequest.newBuilder()
                    .setReference(reference)
                    .putAllOptionMap(optionMap);

            examples.ifPresent(e -> request.addAllExamples(e.stream()
                    .map(ByteString::copyFrom)
                    .collect(Collectors.toList())));

            service.getFilters(request.build(), observer);
            observer.waitForFinish();

            return filters.build();
        }
    }
}
