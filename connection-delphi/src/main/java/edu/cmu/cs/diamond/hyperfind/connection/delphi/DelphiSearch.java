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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.Empty;
import com.google.protobuf.Int64Value;
import edu.cmu.cs.delphi.api.AddLabeledExampleIdsRequest;
import edu.cmu.cs.delphi.api.InferResult;
import edu.cmu.cs.delphi.api.LearningModuleServiceGrpc.LearningModuleServiceStub;
import edu.cmu.cs.delphi.api.ModelArchive;
import edu.cmu.cs.delphi.api.ModelStats;
import edu.cmu.cs.delphi.api.SearchId;
import edu.cmu.cs.diamond.hyperfind.connection.api.FeedbackObject;
import edu.cmu.cs.diamond.hyperfind.connection.api.ObjectId;
import edu.cmu.cs.diamond.hyperfind.connection.api.Search;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchResult;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchStats;
import edu.cmu.cs.diamond.hyperfind.connection.delphi.grpc.BlockingStreamObserver;
import edu.cmu.cs.diamond.hyperfind.connection.delphi.grpc.Channels;
import edu.cmu.cs.diamond.hyperfind.connection.delphi.grpc.UnaryStreamObserver;
import io.grpc.ManagedChannel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DelphiSearch implements Search {

    private static final Logger log = LoggerFactory.getLogger(DelphiSearch.class);

    private final Map<String, LearningModuleServiceStub> learningModules;

    private final SearchId searchId;
    private final Optional<Path> exportDir;

    private final LinkedBlockingQueue<Optional<SearchResult>> results = new LinkedBlockingQueue<>(1);
    private final AtomicReference<Throwable> downloadThrowable = new AtomicReference<>();

    private final AtomicReference<ModelStats> latestModelStats = new AtomicReference<>();
    private int hostsFinished = 0;

    public DelphiSearch(
            Map<String, LearningModuleServiceStub> learningModules,
            SearchId searchId,
            Optional<Path> exportDir,
            ExecutorService resultExecutor,
            boolean colorByModelVersion) {
        this.learningModules = learningModules;
        this.searchId = searchId;
        this.exportDir = exportDir;
        ListeningExecutorService executor = MoreExecutors.listeningDecorator(resultExecutor);

        learningModules.forEach((host, learningModule) -> {
            ListenableFuture<?> downloadFuture = executor.submit(() -> {
                AtomicInteger lastObservedVersion = new AtomicInteger(0);
                BlockingStreamObserver<InferResult> observer = new BlockingStreamObserver<>(host) {
                    @Override
                    public void onNext(InferResult value) {
                        if (value.getModelVersion() > lastObservedVersion.get()) {
                            lastObservedVersion.set(updateModelStats());
                        }

                        queueResult(Optional.of(FromDelphi.convert(value, host, colorByModelVersion)));
                    }
                };
                learningModule.getResults(searchId, observer);
                observer.waitForFinish();
                queueResult(Optional.empty());
            });

            Futures.addCallback(downloadFuture, new FutureCallback<Object>() {
                @Override
                public void onSuccess(@Nullable Object _result) {
                    log.info("Download thread for {} completed successfully", host);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    downloadThrowable.set(throwable);
                    queueResult(Optional.empty());
                }
            }, MoreExecutors.directExecutor());
        });
    }

    @Override
    public synchronized Optional<SearchResult> getNextResult() {
        if (hostsFinished == learningModules.size()) {
            return Optional.empty();
        }

        try {
            Optional<SearchResult> result = results.take();

            if (result.isEmpty()) {
                if (downloadThrowable.get() != null) {
                    throw new RuntimeException("Download thread ran into exception", downloadThrowable.get());
                }

                hostsFinished += 1;
                return getNextResult();
            } else {
                return result;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to take object from result queue", e);
        }
    }

    @Override
    public SearchStats getStats() {
        edu.cmu.cs.delphi.api.SearchStats searchStats = learningModules.entrySet().stream()
                .map(e -> {
                    UnaryStreamObserver<edu.cmu.cs.delphi.api.SearchStats> observer =
                            new UnaryStreamObserver<>(e.getKey());
                    e.getValue().getSearchStats(searchId, observer);
                    return observer.value();
                })
                .reduce(DelphiSearch::merge)
                .get();

        return FromDelphi.convert(searchStats, Optional.ofNullable(latestModelStats.get()));
    }

    @Override
    public void labelExamples(Map<ObjectId, Integer> examples) {
        Map<String, Map<String, String>> examplesByHost = new HashMap<>();
        examples.forEach((objectId, label) -> {
            examplesByHost.putIfAbsent(objectId.hostname(), new HashMap<>());
            examplesByHost.get(objectId.hostname()).put(objectId.objectId(), Integer.toString(label));
        });

        examplesByHost.forEach((host, hostExamples) -> {
            UnaryStreamObserver<Empty> observer = new UnaryStreamObserver<>(host);
            learningModules.get(host).addLabeledExampleIds(AddLabeledExampleIdsRequest.newBuilder()
                    .setSearchId(searchId)
                    .putAllExamples(hostExamples)
                    .build(), observer);
            observer.waitForFinish();
        });
    }

    @Override
    public void retrainFilter(Collection<FeedbackObject> _objects) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        learningModules.forEach((host, learningModule) -> {
            UnaryStreamObserver<Empty> observer = new UnaryStreamObserver<>(host);
            learningModule.stopSearch(searchId, observer);
            observer.waitForFinish();

            ManagedChannel channel = (ManagedChannel) learningModule.getChannel();
            Channels.shutdown(host, channel);
        });
    }

    @Override
    public Optional<Path> getExportDir() {
        return exportDir;
    }

    public void downloadModel() {
        String host = learningModules.keySet().stream().sorted().findFirst().get();
        LearningModuleServiceStub learningModule = learningModules.get(host);

        UnaryStreamObserver<ModelArchive> observer = new UnaryStreamObserver<>(host);
        learningModule.exportModel(searchId, observer);
        ModelArchive archive = observer.value();
        Path modelDir =
                exportDir.orElseThrow(() -> new IllegalArgumentException("Export dir is disabled")).resolve("models");
        modelDir.toFile().mkdirs();

        Path modelPath = modelDir.resolve(String.format("model-%d.zip", archive.getVersion()));
        try {
            Files.write(modelPath, archive.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write model archive to path: " + modelPath);
        }
    }

    private int updateModelStats() {
        String host = learningModules.keySet().stream().sorted().findFirst().get();
        LearningModuleServiceStub learningModule = learningModules.get(host);
        UnaryStreamObserver<ModelStats> observer = new UnaryStreamObserver<>(host);
        learningModule.getModelStats(searchId, observer);
        ModelStats stats = observer.value();
        latestModelStats.set(stats);
        return stats.getVersion();
    }

    private void queueResult(Optional<SearchResult> result) {
        try {
            results.put(result);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to put object in result queue", e);
        }
    }

    private static edu.cmu.cs.delphi.api.SearchStats merge(
            edu.cmu.cs.delphi.api.SearchStats left,
            edu.cmu.cs.delphi.api.SearchStats right) {
        edu.cmu.cs.delphi.api.SearchStats.Builder builder = edu.cmu.cs.delphi.api.SearchStats.newBuilder()
                .setTotalObjects(left.getTotalObjects() + right.getTotalObjects())
                .setProcessedObjects(left.getProcessedObjects() + right.getProcessedObjects())
                .setDroppedObjects(left.getDroppedObjects() + right.getDroppedObjects())
                .setFalseNegatives(left.getFalseNegatives() + right.getFalseNegatives());

        if (left.hasPassedObjects()) {
            builder.setPassedObjects(Int64Value.newBuilder()
                    .setValue(left.getPassedObjects().getValue() + right.getPassedObjects().getValue())
                    .build());
        }

        return builder.build();
    }
}
