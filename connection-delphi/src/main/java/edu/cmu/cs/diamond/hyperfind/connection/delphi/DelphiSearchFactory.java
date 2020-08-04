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

package edu.cmu.cs.diamond.hyperfind.connection.delphi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import edu.cmu.cs.delphi.api.Dataset;
import edu.cmu.cs.delphi.api.DelphiObject;
import edu.cmu.cs.delphi.api.DiamondDataset;
import edu.cmu.cs.delphi.api.ExampleSet;
import edu.cmu.cs.delphi.api.ExampleSetWrapper;
import edu.cmu.cs.delphi.api.GetObjectsRequest;
import edu.cmu.cs.delphi.api.LabeledExample;
import edu.cmu.cs.delphi.api.LearningModuleServiceGrpc;
import edu.cmu.cs.delphi.api.LearningModuleServiceGrpc.LearningModuleServiceStub;
import edu.cmu.cs.delphi.api.SearchConfig;
import edu.cmu.cs.delphi.api.SearchId;
import edu.cmu.cs.delphi.api.SearchRequest;
import edu.cmu.cs.diamond.hyperfind.connection.api.Filter;
import edu.cmu.cs.diamond.hyperfind.connection.api.HyperFindPredicateState;
import edu.cmu.cs.diamond.hyperfind.connection.api.ObjectId;
import edu.cmu.cs.diamond.hyperfind.connection.api.Search;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchFactory;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchResult;
import edu.cmu.cs.diamond.hyperfind.grpc.BlockingStreamObserver;
import edu.cmu.cs.diamond.hyperfind.grpc.UnaryStreamObserver;
import edu.cmu.cs.diamond.opendiamond.Cookie;
import edu.cmu.cs.diamond.opendiamond.CookieMap;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DelphiSearchFactory implements SearchFactory {

    private static final Logger log = LoggerFactory.getLogger(DelphiSearchFactory.class);

    private static final ImmutableSet<String> VALID_LABELS = ImmutableSet.of("0", "1");
    private static final ImmutableMap<String, ExampleSet> VALID_EXAMPLE_SETS = ImmutableMap.of(
            "train", ExampleSet.TRAIN, "test", ExampleSet.TEST);

    private final DelphiConfiguration config;
    private final List<Filter> filters;
    private final CookieMap cookieMap;

    private final ListeningExecutorService resultExecutor;
    private final Supplier<Map<String, LearningModuleServiceStub>> learningModules;

    public DelphiSearchFactory(
            DelphiConfiguration config,
            List<Filter> filters,
            CookieMap cookieMap,
            ExecutorService resultExecutor) {
        this.config = config;

        this.filters = filters;
        this.cookieMap = cookieMap;
        this.resultExecutor = MoreExecutors.listeningDecorator(resultExecutor);
        this.learningModules = () -> Arrays.stream(cookieMap.getHosts()).collect(Collectors.toMap(
                h -> h,
                h -> LearningModuleServiceGrpc.newStub(Channels.create(config, h))));
    }

    @Override
    public Search createSearch(Set<String> attributes, List<HyperFindPredicateState> predicateState) {
        SearchId searchId = SearchId.newBuilder().setValue(UUID.randomUUID().toString()).build();
        String[] hosts = cookieMap.getHosts();
        List<String> nodes = Arrays.stream(hosts)
                .map(h -> String.format("%s:%d", h, config.port()))
                .collect(Collectors.toList());

        String metadata = getMetadata(predicateState);
        Map<String, LearningModuleServiceStub> modules = learningModules.get();

        try {
            for (int i = 0; i < hosts.length; i++) {
                UnaryStreamObserver<Empty> observer = new UnaryStreamObserver<>();
                StreamObserver<SearchRequest> request = modules.get(hosts[i]).startSearch(observer);

                request.onNext(SearchRequest.newBuilder()
                        .setConfig(SearchConfig.newBuilder()
                                .setSearchId(searchId)
                                .addAllNodes(nodes)
                                .setNodeIndex(i)
                                .addAllTrainStrategy(config.trainStrategy())
                                .setRetrainPolicy(config.retrainPolicy())
                                .setOnlyUseBetterModels(config.onlyUseBetterModels())
                                .setDataset(getDataset(hosts[i], attributes))
                                .setSelector(config.selector())
                                .setMetadata(metadata)
                                .build())
                        .build());

                if (config.shouldIncludeExamples()) {
                    try (Stream<Path> paths = Files.list(Paths.get(config.examplePath()))) {
                        paths.forEach(path -> addExamples(path, request, Optional.empty()));
                    } catch (IOException e) {
                        throw new RuntimeException(
                                "Failed to list files in example directory: " + config.examplePath());
                    }
                }

                request.onCompleted();

                observer.waitForFinish();
            }
        } finally {
            modules.forEach((host, learningModule) -> {
                ManagedChannel channel = (ManagedChannel) learningModule.getChannel();
                Channels.shutdown(host, channel);
            });
        }

        return new DelphiSearch(
                learningModules.get(),
                searchId,
                config.shouldDownload()
                        ? Optional.of(Paths.get(config.downloadPathRoot()).resolve(searchId.getValue()))
                        : Optional.empty(),
                resultExecutor,
                config.colorByModelVersion());
    }

    @Override
    public SearchResult getResult(ObjectId objectId, Set<String> attributes) {
        Map<ObjectId, SearchResult> results = getResults(ImmutableSet.of(objectId), attributes);
        Preconditions.checkArgument(results.containsKey(objectId), "Object id not found in results: %s", objectId);
        return results.get(objectId);
    }

    @Override
    public SearchResult getResult(byte[] _data, Set<String> _attributes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<ObjectId, SearchResult> getResults(Collection<ObjectId> objectIds, Set<String> attributes) {
        Multimap<String, String> resultsByHost = HashMultimap.create();
        objectIds.forEach(id -> resultsByHost.put(id.hostname(), id.objectId()));

        Map<ObjectId, SearchResult> results = new ConcurrentHashMap<>();

        Map<String, LearningModuleServiceStub> modules = learningModules.get();
        try {
            List<ListenableFuture<?>> downloadTasks = resultsByHost.asMap().entrySet().stream()
                    .map(e -> resultExecutor.submit(() -> {
                        BlockingStreamObserver<DelphiObject> observer = new BlockingStreamObserver<>() {
                            @Override
                            public void onNext(DelphiObject value) {
                                SearchResult converted = FromDelphi.convert(value, e.getKey());
                                results.put(converted.getId(), converted);
                            }
                        };
                        GetObjectsRequest request = GetObjectsRequest.newBuilder()
                                .setDataset(getDataset(e.getKey(), attributes))
                                .addAllObjectIds(e.getValue())
                                .addAllAttributes(attributes)
                                .build();
                        modules.get(e.getKey()).getObjects(request, observer);
                        observer.waitForFinish();
                    }))
                    .collect(Collectors.toList());

            Futures.allAsList(downloadTasks).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to get results", e);
        } finally {
            modules.forEach((host, learningModule) -> {
                ManagedChannel channel = (ManagedChannel) learningModule.getChannel();
                Channels.shutdown(host, channel);
            });
        }

        return results;
    }

    private void addExamples(
            Path exampleCandidate,
            StreamObserver<SearchRequest> request,
            Optional<ExampleSet> exampleSet) {
        File exampleCandidateFile = exampleCandidate.toFile();
        if (!exampleCandidateFile.isDirectory()) {
            log.warn("Ignoring non-directory file {}", exampleCandidate);
            return;
        }

        String label = exampleCandidateFile.getName();
        if (exampleSet.isEmpty() && VALID_EXAMPLE_SETS.containsKey(label)) {
            try (Stream<Path> paths = Files.list(Paths.get(config.examplePath()))) {
                paths.forEach(path -> addExamples(path, request, Optional.of(VALID_EXAMPLE_SETS.get(label))));
            } catch (IOException e) {
                throw new RuntimeException("Failed to list files in example directory: " + exampleCandidate);
            }
            return;
        }

        if (!VALID_LABELS.contains(label)) {
            log.warn("Ignoring path corresponding to invalid label: {}", exampleCandidate);
            return;
        }

        try (Stream<Path> paths = Files.list(exampleCandidate)) {
            paths.forEach(path -> {
                if (!path.toFile().isFile()) {
                    log.warn("Ignoring directory file {} in example path {}", exampleCandidate, path);
                }

                try {
                    LabeledExample.Builder exampleBuilder = LabeledExample.newBuilder()
                            .setLabel(label)
                            .setContent(ByteString.readFrom(new FileInputStream(path.toFile())));

                    exampleSet.ifPresent(e -> exampleBuilder.setExampleSet(ExampleSetWrapper.newBuilder()
                            .setValue(e)
                            .build()));

                    request.onNext(SearchRequest.newBuilder()
                            .setExample(exampleBuilder)
                            .build());
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read content from file: " + path);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to list files in example directory: " + exampleCandidate);
        }
    }

    private Dataset getDataset(String host, Set<String> attributes) {
        return Dataset.newBuilder()
                .setDiamond(DiamondDataset.newBuilder()
                        .addAllFilters(convert(filters))
                        .addAllCookies(cookieMap.get(host).stream().map(Cookie::getCookie)
                                .collect(Collectors.toList()))
                        .addAllAttributes(attributes)
                        .build())
                .build();
    }

    private String getMetadata(List<HyperFindPredicateState> predicateState) {
        try {
            return ObjectMappers.MAPPER.writeValueAsString(SearchMetadata.of(filters, predicateState,
                    System.currentTimeMillis()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize search metadata", e);
        }
    }

    private static List<edu.cmu.cs.delphi.api.Filter> convert(List<Filter> filters) {
        ImmutableList.Builder<edu.cmu.cs.delphi.api.Filter> filtersBuilder = ImmutableList.builder();
        if (filters.size() > 2) {
            // Then there are some filters besides RGB and Thumbnail. We can't remove RGB since filters might be
            // relying on its output, but remove Thumbnail as Delphi can generate this independently
            filtersBuilder.add(ToDelphi.convert(filters.get(0)));
            filters.stream().skip(2).map(ToDelphi::convert).forEach(filtersBuilder::add);
        }

        return filtersBuilder.build();
    }
}