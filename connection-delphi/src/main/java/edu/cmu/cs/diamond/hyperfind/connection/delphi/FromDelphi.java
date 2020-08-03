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

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;
import edu.cmu.cs.delphi.api.DelphiObject;
import edu.cmu.cs.delphi.api.InferResult;
import edu.cmu.cs.diamond.hyperfind.connection.api.ModelStats;
import edu.cmu.cs.diamond.hyperfind.connection.api.ObjectId;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchResult;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchStats;
import java.awt.Color;
import java.util.Optional;
import java.util.OptionalLong;
import one.util.streamex.EntryStream;

public final class FromDelphi {

    private FromDelphi() {
    }

    public static SearchResult convert(InferResult value, String hostname, boolean colorByModelVersion) {
        int modelVersion = value.getModelVersion();
        // Adding 1.5 to model version because otherwise the border is red which can
        // get confused with the ground truth borders

        return new SearchResult(
                ObjectId.of(value.getObjectId(), hostname, hostname),
                EntryStream.of(value.getAttributesMap()).mapValues(ByteString::toByteArray).toMap(),
                colorByModelVersion && modelVersion != 0
                        ? Optional.of(Color.getHSBColor((float) ((0.1 * modelVersion + 1.5) % 1), 1, 1))
                        : Optional.empty());
    }

    public static SearchResult convert(DelphiObject value, String hostname) {
        ImmutableMap.Builder<String, byte[]> attributes = ImmutableMap.builder();
        value.getAttributesMap().forEach((key, val) -> attributes.put(key, val.toByteArray()));
        attributes.put(SearchResult.DATA_ATTR, value.getContent().toByteArray());

        return new SearchResult(
                ObjectId.of(value.getObjectId(), hostname, hostname),
                attributes.build(),
                Optional.empty());
    }

    public static SearchStats convert(
            edu.cmu.cs.delphi.api.SearchStats searchStats,
            Optional<edu.cmu.cs.delphi.api.ModelStats> modelStats) {
        return SearchStats.of(
                searchStats.getTotalObjects(),
                searchStats.getProcessedObjects(),
                searchStats.getDroppedObjects(),
                searchStats.hasPassedObjects()
                        ? OptionalLong.of(searchStats.getPassedObjects().getValue())
                        : OptionalLong.empty(),
                searchStats.getFalseNegatives(),
                modelStats.map(FromDelphi::convert)
        );
    }

    private static ModelStats convert(edu.cmu.cs.delphi.api.ModelStats value) {
        return ModelStats.of(
                value.getVersion(),
                value.getTestExamples(),
                value.getAuc(),
                value.getValidationMetrics().getPrecision(),
                value.getValidationMetrics().getRecall(),
                value.getValidationMetrics().getF1Score());
    }
}
