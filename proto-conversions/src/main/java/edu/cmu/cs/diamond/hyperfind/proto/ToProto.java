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

import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.StringValue;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.BundleType;
import edu.cmu.cs.diamond.hyperfind.collaboration.api.FilterBuilderReference;
import edu.cmu.cs.diamond.hyperfind.connection.api.FeedbackObject;
import edu.cmu.cs.diamond.hyperfind.connection.api.Filter;
import edu.cmu.cs.diamond.hyperfind.connection.api.HyperFindPredicateState;
import edu.cmu.cs.diamond.hyperfind.connection.api.ObjectId;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchResult;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchStats;
import edu.cmu.cs.diamond.hyperfind.connection.api.bundle.BooleanOption;
import edu.cmu.cs.diamond.hyperfind.connection.api.bundle.Bundle;
import edu.cmu.cs.diamond.hyperfind.connection.api.bundle.BundleState;
import edu.cmu.cs.diamond.hyperfind.connection.api.bundle.Choice;
import edu.cmu.cs.diamond.hyperfind.connection.api.bundle.ChoiceOption;
import edu.cmu.cs.diamond.hyperfind.connection.api.bundle.ExampleOption;
import edu.cmu.cs.diamond.hyperfind.connection.api.bundle.FileOption;
import edu.cmu.cs.diamond.hyperfind.connection.api.bundle.NumberOption;
import edu.cmu.cs.diamond.hyperfind.connection.api.bundle.Option;
import edu.cmu.cs.diamond.hyperfind.connection.api.bundle.OptionGroup;
import edu.cmu.cs.diamond.hyperfind.connection.api.bundle.OptionVisitor;
import edu.cmu.cs.diamond.hyperfind.connection.api.bundle.StringOption;
import java.util.stream.Collectors;
import one.util.streamex.EntryStream;

public final class ToProto {

    private ToProto() {
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

    public static edu.cmu.cs.diamond.hyperfind.collaboration.api.HyperFindPredicateState convert(
            HyperFindPredicateState value) {
        edu.cmu.cs.diamond.hyperfind.collaboration.api.HyperFindPredicateState.Builder builder =
                edu.cmu.cs.diamond.hyperfind.collaboration.api.HyperFindPredicateState.newBuilder()
                        .setBundleState(convert(value.bundleState()))
                        .putAllOptionMap(value.optionMap())
                        .setInstanceName(value.instanceName());

        value.examples().ifPresent(e -> e.forEach(b -> builder.addExamples(ByteString.copyFrom(b))));

        return builder.build();
    }

    public static edu.cmu.cs.diamond.hyperfind.collaboration.api.FeedbackObject convert(FeedbackObject value) {
        return edu.cmu.cs.diamond.hyperfind.collaboration.api.FeedbackObject.newBuilder()
                .setId(convert(value.id()))
                .setLabel(value.label())
                .setFeatureVector(ByteString.copyFrom(value.featureVector()))
                .build();
    }

    public static edu.cmu.cs.diamond.hyperfind.collaboration.api.SearchResult convert(SearchResult value) {
        return edu.cmu.cs.diamond.hyperfind.collaboration.api.SearchResult.newBuilder()
                .setId(convert(value.getId()))
                .putAllAttributes(value.getKeys().stream()
                        .collect(Collectors.toMap(k -> k, k -> ByteString.copyFrom(value.getBytes(k).get()))))
                .build();
    }

    public static edu.cmu.cs.diamond.hyperfind.collaboration.api.SearchStats convert(String server, SearchStats value) {
        return edu.cmu.cs.diamond.hyperfind.collaboration.api.SearchStats.newBuilder()
                .setServerName(server)
                .setTotalObjects(value.totalObjects())
                .setProcessedObjects(value.processedObjects())
                .setDroppedObjects(value.droppedObjects())
                .setTruePositives(value.truePositives())
                .setFalseNegatives(value.falseNegatives())
                .build();
    }

    public static edu.cmu.cs.diamond.hyperfind.collaboration.api.Bundle convert(
            Bundle value, FilterBuilderReference filterBuilder) {
        return edu.cmu.cs.diamond.hyperfind.collaboration.api.Bundle.newBuilder()
                .setDisplayName(value.displayName())
                .setType(BundleType.valueOf(value.type().name()))
                .addAllOptions(value.options().stream().map(ToProto::convert).collect(Collectors.toList()))
                .setState(ToProto.convert(value.state()))
                .setFilterBuilder(filterBuilder)
                .build();
    }

    public static edu.cmu.cs.diamond.hyperfind.collaboration.api.OptionGroup convert(OptionGroup value) {
        edu.cmu.cs.diamond.hyperfind.collaboration.api.OptionGroup.Builder builder =
                edu.cmu.cs.diamond.hyperfind.collaboration.api.OptionGroup.newBuilder();

        value.displayName().ifPresent(d -> builder.setDisplayName(StringValue.newBuilder().setValue(d).build()));
        value.options().forEach(o -> builder.addOptions(convert(o)));

        return builder.build();
    }

    public static edu.cmu.cs.diamond.hyperfind.collaboration.api.Option convert(Option value) {
        edu.cmu.cs.diamond.hyperfind.collaboration.api.Option.Builder builder =
                edu.cmu.cs.diamond.hyperfind.collaboration.api.Option.newBuilder();

        value.accept(new OptionVisitor<Void>() {
            @Override
            public Void accept(StringOption option) {
                edu.cmu.cs.diamond.hyperfind.collaboration.api.StringOption.Builder strBuilder =
                        edu.cmu.cs.diamond.hyperfind.collaboration.api.StringOption.newBuilder()
                                .setDisplayName(option.displayName())
                                .setName(option.name())
                                .setDefaultValue(option.defaultValue())
                                .setHeight(option.height())
                                .setWidth(option.width())
                                .setMultiLine(option.multiLine())
                                .setDisabledValue(option.disabledValue());

                option.initiallyEnabled().ifPresent(o ->
                        strBuilder.setInitiallyEnabled(BoolValue.newBuilder().setValue(o)));

                builder.setString(strBuilder);
                return null;
            }

            @Override
            public Void accept(BooleanOption option) {
                builder.setBoolean(edu.cmu.cs.diamond.hyperfind.collaboration.api.BooleanOption.newBuilder()
                        .setDisplayName(option.displayName())
                        .setName(option.name())
                        .setDefaultValue(option.defaultValue()));

                return null;
            }

            @Override
            public Void accept(NumberOption option) {
                edu.cmu.cs.diamond.hyperfind.collaboration.api.NumberOption.Builder numBuilder =
                        edu.cmu.cs.diamond.hyperfind.collaboration.api.NumberOption.newBuilder()
                                .setDisplayName(option.displayName())
                                .setName(option.name())
                                .setDefaultValue(option.defaultValue())
                                .setStep(option.step())
                                .setDisabledValue(option.disabledValue());

                option.min().ifPresent(o -> numBuilder.setMin(DoubleValue.newBuilder().setValue(o)));
                option.max().ifPresent(o -> numBuilder.setMax(DoubleValue.newBuilder().setValue(o)));
                option.initiallyEnabled().ifPresent(o ->
                        numBuilder.setInitiallyEnabled(BoolValue.newBuilder().setValue(o)));

                builder.setNumber(numBuilder);
                return null;
            }

            @Override
            public Void accept(ChoiceOption option) {
                edu.cmu.cs.diamond.hyperfind.collaboration.api.ChoiceOption.Builder choiceBuilder =
                        edu.cmu.cs.diamond.hyperfind.collaboration.api.ChoiceOption.newBuilder()
                                .setDisplayName(option.displayName())
                                .setName(option.name())
                                .setDisabledValue(option.disabledValue());

                option.choices().forEach(c -> choiceBuilder.addChoices(convert(c)));

                option.initiallyEnabled().ifPresent(o ->
                        choiceBuilder.setInitiallyEnabled(BoolValue.newBuilder().setValue(o)));

                builder.setChoice(choiceBuilder);
                return null;
            }

            @Override
            public Void accept(ExampleOption option) {
                builder.setExample(edu.cmu.cs.diamond.hyperfind.collaboration.api.ExampleOption.newBuilder()
                        .setDisplayName(option.displayName())
                        .setName(option.name()));

                return null;
            }

            @Override
            public Void accept(FileOption option) {
                builder.setFile(edu.cmu.cs.diamond.hyperfind.collaboration.api.FileOption.newBuilder()
                        .setDisplayName(option.displayName())
                        .setName(option.name()));

                return null;
            }
        });

        return builder.build();
    }

    public static edu.cmu.cs.diamond.hyperfind.collaboration.api.Choice convert(Choice value) {
        return edu.cmu.cs.diamond.hyperfind.collaboration.api.Choice.newBuilder()
                .setDisplayName(value.displayName())
                .setValue(value.value())
                .setIsDefault(value.isDefault())
                .build();
    }

    public static edu.cmu.cs.diamond.hyperfind.collaboration.api.BundleState convert(BundleState value) {
        return edu.cmu.cs.diamond.hyperfind.collaboration.api.BundleState.newBuilder()
                .putAllBundleContents(EntryStream.of(value.bundleContents()).mapValues(ByteString::copyFrom).toMap())
                .addAllMemberDirs(value.memberDirs())
                .build();
    }
}
