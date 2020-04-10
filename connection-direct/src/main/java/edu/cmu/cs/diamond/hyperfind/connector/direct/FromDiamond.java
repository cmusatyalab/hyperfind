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

package edu.cmu.cs.diamond.hyperfind.connector.direct;

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
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.NumberOption;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.Option;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.OptionGroup;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.StringOption;
import edu.cmu.cs.diamond.opendiamond.ObjectIdentifier;
import edu.cmu.cs.diamond.opendiamond.Result;
import edu.cmu.cs.diamond.opendiamond.ServerStatistics;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

public final class FromDiamond {

    private FromDiamond() {
    }

    public static ObjectId convert(ObjectIdentifier value) {
        return ObjectId.of(value.getObjectID(), value.getDeviceName(), value.getHostname());
    }

    public static SearchResult convert(Result value) {
        return new SearchResult(
                convert(value.getObjectIdentifier()),
                value.getKeys().stream().collect(Collectors.toMap(k -> k, value::getValue)));
    }

    public static SearchStats convert(ServerStatistics value) {
        return SearchStats.of(
                value.getServerStats().get(ServerStatistics.TOTAL_OBJECTS),
                value.getServerStats().get(ServerStatistics.PROCESSED_OBJECTS),
                value.getServerStats().get(ServerStatistics.DROPPED_OBJECTS),
                value.getServerStats().get(ServerStatistics.TP_OBJECTS),
                value.getServerStats().get(ServerStatistics.FN_OBJECTS));
    }

    public static Bundle convert(edu.cmu.cs.diamond.opendiamond.Bundle value) {
        try {
            edu.cmu.cs.diamond.opendiamond.Bundle.PreparedFileLoader fileLoader = value.export().fileLoader;
            return Bundle.of(
                    value.getDisplayName(),
                    BundleType.valueOf(value.getType().name()),
                    value.getOptions().stream().map(FromDiamond::convert).collect(Collectors.toList()),
                    BundleState.of(
                            fileLoader.bundleContents,
                            fileLoader.memberDirs.stream().map(File::toString).collect(Collectors.toList())),
                    (optionMap, examples) -> {
                        try {
                            final List<edu.cmu.cs.diamond.opendiamond.Filter> filters;
                            if (examples.isPresent()) {
                                filters = value.getFilters(optionMap, examples.get());
                            } else {
                                filters = value.getFilters(optionMap);
                            }

                            return filters.stream().map(FromDiamond::convert).collect(Collectors.toList());
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to get filters", e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert bundle from diamond", e);
        }
    }

    public static Filter convert(edu.cmu.cs.diamond.opendiamond.Filter value) {
        return Filter.of(
                value.getFilterCode().getBytes(),
                value.getDependencies(),
                value.getArguments(),
                value.getName(),
                value.getMinScore(),
                value.getMaxScore(),
                value.getBlob());
    }

    public static OptionGroup convert(edu.cmu.cs.diamond.opendiamond.bundle.OptionGroup value) {
        return OptionGroup.of(
                Optional.ofNullable(value.getDisplayName()),
                value.getOptions().stream().map(FromDiamond::convert).collect(Collectors.toList()));
    }

    public static Option convert(edu.cmu.cs.diamond.opendiamond.bundle.Option value) {
        String displayName = value.getDisplayName();
        String name = value.getName();

        if (value instanceof edu.cmu.cs.diamond.opendiamond.bundle.ExampleOption) {
            return ExampleOption.of(displayName, name);
        } else if (value instanceof edu.cmu.cs.diamond.opendiamond.bundle.StringOption) {
            edu.cmu.cs.diamond.opendiamond.bundle.StringOption stringOption =
                    (edu.cmu.cs.diamond.opendiamond.bundle.StringOption) value;
            return StringOption.of(
                    displayName,
                    name,
                    stringOption.getDefault(),
                    stringOption.getHeight(),
                    stringOption.getWidth(),
                    stringOption.isMultiLine(),
                    stringOption.getDisabledValue(),
                    Optional.ofNullable(stringOption.isInitiallyEnabled()));
        } else if (value instanceof edu.cmu.cs.diamond.opendiamond.bundle.NumberOption) {
            edu.cmu.cs.diamond.opendiamond.bundle.NumberOption numberOption =
                    (edu.cmu.cs.diamond.opendiamond.bundle.NumberOption) value;
            return NumberOption.of(
                    displayName,
                    name,
                    numberOption.getDefault(),
                    numberOption.getMin() != null ? OptionalDouble.of(numberOption.getMin()) : OptionalDouble.empty(),
                    numberOption.getMax() != null ? OptionalDouble.of(numberOption.getMax()) : OptionalDouble.empty(),
                    numberOption.getStep(),
                    numberOption.getDisabledValue(),
                    Optional.ofNullable(numberOption.isInitiallyEnabled()));
        } else if (value instanceof edu.cmu.cs.diamond.opendiamond.bundle.FileOption) {
            return FileOption.of(displayName, name);
        } else if (value instanceof edu.cmu.cs.diamond.opendiamond.bundle.BooleanOption) {
            edu.cmu.cs.diamond.opendiamond.bundle.BooleanOption booleanOption =
                    (edu.cmu.cs.diamond.opendiamond.bundle.BooleanOption) value;
            return BooleanOption.of(
                    displayName,
                    name,
                    booleanOption.isDefault());
        } else if (value instanceof edu.cmu.cs.diamond.opendiamond.bundle.ChoiceOption) {
            edu.cmu.cs.diamond.opendiamond.bundle.ChoiceOption choiceOption =
                    (edu.cmu.cs.diamond.opendiamond.bundle.ChoiceOption) value;
            return ChoiceOption.of(
                    displayName,
                    name,
                    choiceOption.getChoices().stream().map(FromDiamond::convert).collect(Collectors.toList()),
                    choiceOption.getDisabledValue(),
                    Optional.ofNullable(choiceOption.isInitiallyEnabled()));
        } else {
            throw new IllegalArgumentException("Unrecognized option type: " + value.getClass());
        }
    }

    public static Choice convert(edu.cmu.cs.diamond.opendiamond.bundle.Choice value) {
        return Choice.of(value.getDisplayName(), value.getValue(), value.isDefault());
    }
}
