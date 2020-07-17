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

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.Empty;
import edu.cmu.cs.delphi.api.Dataset;
import edu.cmu.cs.delphi.api.DelphiObject;
import edu.cmu.cs.delphi.api.DiamondDataset;
import edu.cmu.cs.delphi.api.GetObjectsRequest;
import edu.cmu.cs.delphi.api.LearningModuleServiceGrpc;
import edu.cmu.cs.delphi.api.LearningModuleServiceGrpc.LearningModuleServiceStub;
import edu.cmu.cs.delphi.api.ModelConditionConfig;
import edu.cmu.cs.delphi.api.RetrainPolicyConfig;
import edu.cmu.cs.delphi.api.SearchConfig;
import edu.cmu.cs.delphi.api.SearchId;
import edu.cmu.cs.delphi.api.SearchRequest;
import edu.cmu.cs.delphi.api.SelectorConfig;
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
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import java.nio.file.Path;
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
import java.util.function.Function;
import java.util.stream.Collectors;

public final class DelphiSearchFactory implements SearchFactory {

    private final List<ModelConditionConfig> trainStrategy;
    private final RetrainPolicyConfig retrainPolicy;
    private final SelectorConfig selector;
    private final boolean onlyUseBetterModels;
    private final Optional<Path> downloadPathRoot;

    private final List<edu.cmu.cs.delphi.api.Filter> filters;

    private final CookieMap cookieMap;
    private final int delphiPort;
    private final boolean colorByModelVersion;

    private final ListeningExecutorService resultExecutor;
    private final Map<String, LearningModuleServiceStub> learningModules;

    public DelphiSearchFactory(
            List<ModelConditionConfig> trainStrategy,
            RetrainPolicyConfig retrainPolicy,
            SelectorConfig selector,
            boolean onlyUseBetterModels,
            Optional<Path> downloadPathRoot,
            List<Filter> filters,
            CookieMap cookieMap,
            int delphiPort,
            boolean colorByModelVersion,
            ExecutorService resultExecutor,
            Function<String, Channel> channelBuilder) {
        this.trainStrategy = trainStrategy;
        this.retrainPolicy = retrainPolicy;
        this.selector = selector;
        this.onlyUseBetterModels = onlyUseBetterModels;
        this.downloadPathRoot = downloadPathRoot;
        this.filters = filters.stream().map(ToDelphi::convert).collect(Collectors.toList());
        this.cookieMap = cookieMap;
        this.delphiPort = delphiPort;
        this.colorByModelVersion = colorByModelVersion;
        this.resultExecutor = MoreExecutors.listeningDecorator(resultExecutor);
        this.learningModules = Arrays.stream(this.cookieMap.getHosts()).collect(Collectors.toMap(
                h -> h,
                h -> LearningModuleServiceGrpc.newStub(channelBuilder.apply(h))));
    }

    @Override
    public Search createSearch(Set<String> attributes, List<HyperFindPredicateState> _predicateState) {
        SearchId searchId = SearchId.newBuilder().setValue(UUID.randomUUID().toString()).build();
        String[] hosts = cookieMap.getHosts();
        List<String> nodes = Arrays.stream(hosts)
                .map(h -> String.format("%s:%d", h, delphiPort))
                .collect(Collectors.toList());

        for (int i = 0; i < hosts.length; i++) {
            UnaryStreamObserver<Empty> observer = new UnaryStreamObserver<>();
            StreamObserver<SearchRequest> request = learningModules.get(hosts[i]).startSearch(observer);

            request.onNext(SearchRequest.newBuilder()
                    .setConfig(SearchConfig.newBuilder()
                            .setSearchId(searchId)
                            .addAllNodes(nodes)
                            .setNodeIndex(i)
                            .addAllTrainStrategy(trainStrategy)
                            .setRetrainPolicy(retrainPolicy)
                            .setOnlyUseBetterModels(onlyUseBetterModels)
                            .setDataset(getDataset(hosts[i], attributes))
                            .setSelector(selector)
                            .build())
                    .build());
            observer.waitForFinish();
        }

        return new DelphiSearch(learningModules, searchId, downloadPathRoot.map(p -> p.resolve(searchId.getValue())),
                resultExecutor, colorByModelVersion);
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
                    learningModules.get(e.getKey()).getObjects(request, observer);
                    observer.waitForFinish();
                }))
                .collect(Collectors.toList());

        try {
            Futures.allAsList(downloadTasks).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to get results", e);
        }

        return results;
    }

    private Dataset getDataset(String host, Set<String> attributes) {
        return Dataset.newBuilder()
                .setDiamond(DiamondDataset.newBuilder()
                        .addAllFilters(filters)
                        .addAllCookies(cookieMap.get(host).stream().map(Cookie::getCookie)
                                .collect(Collectors.toList()))
                        .addAllAttributes(attributes)
                        .build())
                .build();
    }
}