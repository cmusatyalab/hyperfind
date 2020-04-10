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

import com.google.common.base.Preconditions;

public final class UnaryStreamObserver<T> extends BlockingStreamObserver<T> {

    private T result = null;
    private boolean valueSet = false;

    @Override
    public void onNext(T value) {
        Preconditions.checkArgument(!valueSet, "Value already set to %s, now encountering %s", result, value);
        result = value;
        valueSet = true;
    }

    public T value() {
        waitForFinish();
        Preconditions.checkArgument(valueSet, "Value not set");
        return result;
    }
}
