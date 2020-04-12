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

package edu.cmu.cs.diamond.hyperfind.connector.collaboration;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.CollaborationServiceGrpc;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.FilterBuilderReference;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.GetFiltersRequest;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.UpdateCookiesRequest;
import edu.cmu.cs.diamond.hyperfind.connector.api.Connection;
import edu.cmu.cs.diamond.hyperfind.connector.api.Filter;
import edu.cmu.cs.diamond.hyperfind.connector.api.SearchFactory;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.Bundle;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.BundleState;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.FilterBuilder;
import edu.cmu.cs.diamond.hyperfind.connector.collaboration.grpc.BlockingStreamObserver;
import edu.cmu.cs.diamond.hyperfind.connector.collaboration.grpc.UnaryStreamObserver;
import edu.cmu.cs.diamond.hyperfind.proto.FromProto;
import edu.cmu.cs.diamond.hyperfind.proto.ToProto;
import io.grpc.ManagedChannel;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.net.ssl.SSLException;

public final class CollaborationConnection implements Connection {

    private final CollaborationServiceGrpc.CollaborationServiceStub service;
    private final ExecutorService resultExecutor;

    public CollaborationConnection(String host, String port) {
        this(host, Integer.parseInt(port), Optional.empty());
    }

    public CollaborationConnection(String host, String port, String trustStorePath) {
        this(host, Integer.parseInt(port), Optional.of(trustStorePath));
    }

    private CollaborationConnection(String host, int port, Optional<String> trustStorePath) {
        io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder sslContextBuilder
                = io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts.forClient();
        trustStorePath.ifPresent(t -> sslContextBuilder.trustManager(Paths.get(t).toFile()));

        try {
            ManagedChannel channel = io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder.forAddress(host, port)
                    .sslContext(sslContextBuilder.build())
                    .build();
            this.service = CollaborationServiceGrpc.newStub(channel);
        } catch (SSLException e) {
            throw new RuntimeException("Failed to create channel", e);
        }

        this.resultExecutor = Executors.newCachedThreadPool();
    }

    @Override
    public SearchFactory getSearchFactory(List<Filter> filters) {
        return new CollaboratorSearchFactory(service, filters, resultExecutor);
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
