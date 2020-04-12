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

package edu.cmu.cs.diamond.hyperfind.proto;

import com.google.protobuf.ByteString;
import edu.cmu.cs.diamond.hyperfind.connector.api.FeedbackObject;
import edu.cmu.cs.diamond.hyperfind.connector.api.Filter;
import edu.cmu.cs.diamond.hyperfind.connector.api.ObjectId;
import edu.cmu.cs.diamond.hyperfind.connector.api.SearchResult;
import edu.cmu.cs.diamond.hyperfind.connector.api.SearchStats;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.BooleanOption;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.Bundle;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.BundleState;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.BundleType;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.Choice;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.ChoiceOption;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.ExampleOption;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.FileOption;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.FilterBuilder;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.NumberOption;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.Option;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.OptionGroup;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.StringOption;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import one.util.streamex.EntryStream;

public final class FromProto {

    private FromProto() {
    }

    public static ObjectId convert(edu.cmu.cs.diamond.hyperfind.collaboration.api.ObjectId value) {
        return ObjectId.of(value.getObjectId(), value.getDeviceName(), value.getHostname());
    }

    public static SearchResult convert(edu.cmu.cs.diamond.hyperfind.collaboration.api.SearchResult value) {
        return new SearchResult(
                convert(value.getId()),
                EntryStream.of(value.getAttributesMap()).mapValues(ByteString::toByteArray).toMap());
    }

    public static Filter convert(edu.cmu.cs.diamond.hyperfind.collaboration.api.Filter value) {
        return Filter.of(
                value.getCode().toByteArray(),
                value.getDependencesList(),
                value.getArgumentsList(),
                value.getName(),
                value.getMinScore(),
                value.getMaxScore(),
                value.getBlob().toByteArray());
    }

    public static Bundle convert(
            edu.cmu.cs.diamond.hyperfind.collaboration.api.Bundle value, FilterBuilder filterBuilder) {
        return Bundle.of(
                value.getDisplayName(),
                BundleType.valueOf(value.getType().name()),
                value.getOptionsList().stream().map(FromProto::convert).collect(Collectors.toList()),
                convert(value.getState()),
                filterBuilder);
    }

    public static BundleState convert(edu.cmu.cs.diamond.hyperfind.collaboration.api.BundleState value) {
        return BundleState.of(
                EntryStream.of(value.getBundleContentsMap()).mapValues(ByteString::toByteArray).toMap(),
                value.getMemberDirsList());
    }

    public static OptionGroup convert(edu.cmu.cs.diamond.hyperfind.collaboration.api.OptionGroup value) {
        return OptionGroup.of(
                value.hasDisplayName() ? Optional.of(value.getDisplayName().getValue()) : Optional.empty(),
                value.getOptionsList().stream().map(FromProto::convert).collect(Collectors.toList()));
    }

    public static Option convert(edu.cmu.cs.diamond.hyperfind.collaboration.api.Option value) {
        switch (value.getValueCase()) {
            case EXAMPLE:
                return ExampleOption.of(value.getExample().getDisplayName(), value.getExample().getName());
            case CHOICE:
                return convert(value.getChoice());
            case BOOLEAN:
                edu.cmu.cs.diamond.hyperfind.collaboration.api.BooleanOption bool = value.getBoolean();
                return BooleanOption.of(bool.getDisplayName(), bool.getName(), bool.getDefaultValue());
            case NUMBER:
                return convert(value.getNumber());
            case STRING:
                return convert(value.getString());
            case FILE:
                return FileOption.of(value.getFile().getDisplayName(), value.getFile().getName());
            case VALUE_NOT_SET:
        }

        throw new IllegalArgumentException("Unrecognized option type: " + value);
    }

    private static Option convert(edu.cmu.cs.diamond.hyperfind.collaboration.api.ChoiceOption choice) {
        return ChoiceOption.of(
                choice.getDisplayName(),
                choice.getName(),
                choice.getChoicesList().stream().map(FromProto::convert).collect(Collectors.toList()),
                choice.getDisabledValue(),
                choice.hasInitiallyEnabled()
                        ? Optional.of(choice.getInitiallyEnabled().getValue())
                        : Optional.empty());
    }

    private static Option convert(edu.cmu.cs.diamond.hyperfind.collaboration.api.NumberOption number) {
        return NumberOption.of(
                number.getDisplayName(),
                number.getName(),
                number.getDefaultValue(),
                number.hasMin() ? OptionalDouble.of(number.getMin().getValue()) : OptionalDouble.empty(),
                number.hasMax() ? OptionalDouble.of(number.getMax().getValue()) : OptionalDouble.empty(),
                number.getStep(),
                number.getDisabledValue(),
                number.hasInitiallyEnabled()
                        ? Optional.of(number.getInitiallyEnabled().getValue())
                        : Optional.empty());
    }

    private static Option convert(edu.cmu.cs.diamond.hyperfind.collaboration.api.StringOption string) {
        return StringOption.of(
                string.getDisplayName(),
                string.getName(),
                string.getDefaultValue(),
                string.getHeight(),
                string.getWidth(),
                string.getMultiLine(),
                string.getDisabledValue(),
                string.hasInitiallyEnabled()
                        ? Optional.of(string.getInitiallyEnabled().getValue())
                        : Optional.empty());
    }

    public static Choice convert(edu.cmu.cs.diamond.hyperfind.collaboration.api.Choice value) {
        return Choice.of(value.getDisplayName(), value.getValue(), value.getIsDefault());
    }

    public static SearchStats convert(edu.cmu.cs.diamond.hyperfind.collaboration.api.SearchStats value) {
        return SearchStats.of(
                value.getTotalObjects(),
                value.getProcessedObjects(),
                value.getDroppedObjects(),
                value.getTruePositives(),
                value.getFalseNegatives());
    }

    public static FeedbackObject convert(edu.cmu.cs.diamond.hyperfind.collaboration.api.FeedbackObject value) {
        return FeedbackObject.of(
                convert(value.getId()),
                value.getLabel(),
                value.getFeatureVector().toByteArray());
    }
}
