/*
 * The OpenDiamond Platform for Interactive Search
 *
 * Copyright (c) 2011-2020 Carnegie Mellon University
 * All rights reserved.
 *
 * This software is distributed under the terms of the Eclipse Public
 * License, Version 1.0 which can be found in the file named LICENSE.
 * ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
 * RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT.
 */

package edu.cmu.cs.diamond.hyperfind.connector.collaborator.grpc;

import io.grpc.stub.StreamObserver;
import java.util.concurrent.CountDownLatch;

public abstract class BlockingStreamObserver<T> implements StreamObserver<T> {
    private final CountDownLatch doneSignal = new CountDownLatch(1);
    private Throwable throwable = null;

    @Override
    public final void onError(Throwable th) {
        throwable = th;
        doneSignal.countDown();
    }

    @Override
    public final void onCompleted() {
        doneSignal.countDown();
    }

    public final void waitForFinish() {
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while waiting for finish", e);
        }

        if (throwable != null) {
            throw new RuntimeException("Observer ran into exception", throwable);
        }
    }
}
