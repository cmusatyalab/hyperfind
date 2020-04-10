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

package edu.cmu.cs.diamond.hyperfind.connector.collaborator;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.Empty;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.CollaborationServiceGrpc;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.LabelExamplesRequest;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.LabeledExample;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.RetrainFilterRequest;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.SearchId;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.SearchResultResponse;
import edu.cmu.cs.diamond.hyperfind.connector.api.FeedbackObject;
import edu.cmu.cs.diamond.hyperfind.connector.api.ObjectId;
import edu.cmu.cs.diamond.hyperfind.connector.api.Search;
import edu.cmu.cs.diamond.hyperfind.connector.api.SearchResult;
import edu.cmu.cs.diamond.hyperfind.connector.api.SearchStats;
import edu.cmu.cs.diamond.hyperfind.connector.collaborator.grpc.BlockingStreamObserver;
import edu.cmu.cs.diamond.hyperfind.connector.collaborator.grpc.UnaryStreamObserver;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CollaboratorSearch implements Search {

    private static final Logger log = LoggerFactory.getLogger(CollaboratorSearch.class);

    private final CollaborationServiceGrpc.CollaborationServiceStub service;
    private final SearchId searchId;
    private final ListeningExecutorService resultExecutor;

    private final LinkedBlockingQueue<Optional<SearchResult>> results = new LinkedBlockingQueue<>(1000);
    private final AtomicReference<Throwable> downloadThrowable = new AtomicReference<>();

    public CollaboratorSearch(
            CollaborationServiceGrpc.CollaborationServiceStub service,
            SearchId searchId,
            ExecutorService resultExecutor) {
        this.service = service;
        this.searchId = searchId;
        this.resultExecutor = MoreExecutors.listeningDecorator(resultExecutor);

        ListenableFuture<?> downloadFuture = this.resultExecutor.submit(() -> {
            BlockingStreamObserver<SearchResultResponse> observer = new BlockingStreamObserver<>() {
                @Override
                public void onNext(SearchResultResponse value) {
                    queueResult(convert(value));
                }
            };
            service.getSearchResults(searchId, observer);
            observer.waitForFinish();
        });

        Futures.addCallback(downloadFuture, new FutureCallback<Object>() {
            @Override
            public void onSuccess(@Nullable Object result) {
                log.info("Download thread completed successfully");
            }

            @Override
            public void onFailure(Throwable throwable) {
                downloadThrowable.set(throwable);
                queueResult(Optional.empty());
            }
        }, MoreExecutors.directExecutor());
    }

    @Override
    public Optional<SearchResult> getNextResult() {
        try {
            Optional<SearchResult> result = results.take();

            if (result.isEmpty() && downloadThrowable.get() != null) {
                throw new RuntimeException("Download thread ran into exception", downloadThrowable.get());
            }

            return result;
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to take object from result queue", e);
        }
    }

    @Override
    public Map<String, SearchStats> getStats() {
        ImmutableMap.Builder<String, SearchStats> builder = ImmutableMap.builder();
        BlockingStreamObserver<edu.cmu.cs.diamond.hyperfind.collaboration.api.SearchStats> observer =
                new BlockingStreamObserver<>() {
                    @Override
                    public void onNext(edu.cmu.cs.diamond.hyperfind.collaboration.api.SearchStats value) {
                        builder.put(value.getServerName(), FromProto.convert(value));
                    }
                };

        service.getSearchStats(searchId, observer);
        observer.waitForFinish();

        return builder.build();
    }

    @Override
    public void labelExamples(Map<ObjectId, Integer> examples) {
        UnaryStreamObserver<Empty> observer = new UnaryStreamObserver<>();
        LabelExamplesRequest request = LabelExamplesRequest.newBuilder()
                .setSearchId(searchId)
                .addAllExamples(examples.entrySet().stream()
                        .map(e -> LabeledExample.newBuilder()
                                .setId(ToProto.convert(e.getKey()))
                                .setLabel(e.getValue())
                                .build())
                        .collect(Collectors.toList())
                )
                .build();

        service.labelExamples(request, observer);
        observer.waitForFinish();
    }

    @Override
    public void retrainFilter(Collection<FeedbackObject> objects) {
        UnaryStreamObserver<Empty> observer = new UnaryStreamObserver<>();
        RetrainFilterRequest request = RetrainFilterRequest.newBuilder()
                .setSearchId(searchId)
                .addAllObjects(objects.stream().map(ToProto::convert).collect(Collectors.toList()))
                .build();

        service.retrainFilter(request, observer);
        observer.waitForFinish();
    }

    @Override
    public void close() {
        UnaryStreamObserver<Empty> observer = new UnaryStreamObserver<>();
        service.closeSearch(searchId, observer);
        observer.waitForFinish();
    }

    private void queueResult(Optional<SearchResult> result) {
        try {
            results.put(result);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to put object in result queue", e);
        }
    }

    private static Optional<SearchResult> convert(SearchResultResponse response) {
        switch (response.getValueCase()) {
            case RESULT:
                return Optional.of(FromProto.convert(response.getResult()));
            case EMPTY:
                return Optional.empty();
            case VALUE_NOT_SET:
        }

        throw new IllegalArgumentException("Unrecognized search response type: " + response);
    }
}
