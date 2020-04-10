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

import com.google.protobuf.ByteString;
import edu.cmu.cs.diamond.hyperfind.connector.api.FeedbackObject;
import edu.cmu.cs.diamond.hyperfind.connector.api.Filter;
import edu.cmu.cs.diamond.hyperfind.connector.api.ObjectId;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.BundleState;
import one.util.streamex.EntryStream;

public final class ToProto {

    private ToProto() {
    }

    public static edu.cmu.cs.diamond.hyperfind.collaboration.api.BundleState convert(BundleState value) {
        return edu.cmu.cs.diamond.hyperfind.collaboration.api.BundleState.newBuilder()
                .putAllBundleContents(EntryStream.of(value.bundleContents()).mapValues(ByteString::copyFrom).toMap())
                .addAllMemberDirs(value.memberDirs())
                .build();
    }

    public static edu.cmu.cs.diamond.hyperfind.collaboration.api.ObjectId convert(ObjectId value) {
        return edu.cmu.cs.diamond.hyperfind.collaboration.api.ObjectId.newBuilder()
                .setObjectId(value.objectId())
                .setDeviceName(value.deviceName())
                .setHostname(value.hostname())
                .build();
    }

    public static edu.cmu.cs.diamond.hyperfind.collaboration.api.Filter convert(Filter value) {
        return edu.cmu.cs.diamond.hyperfind.collaboration.api.Filter.newBuilder()
                .setCode(ByteString.copyFrom(value.code()))
                .addAllDependences(value.dependencies())
                .addAllArguments(value.arguments())
                .setName(value.name())
                .setMinScore(value.minScore())
                .setMaxScore(value.maxScore())
                .setBlob(ByteString.copyFrom(value.blob()))
                .build();
    }

    public static edu.cmu.cs.diamond.hyperfind.collaboration.api.FeedbackObject convert(FeedbackObject value) {
        return edu.cmu.cs.diamond.hyperfind.collaboration.api.FeedbackObject.newBuilder()
                .setId(convert(value.id()))
                .setLabel(value.label())
                .setFeatureVector(ByteString.copyFrom(value.featureVector()))
                .build();
    }
}
