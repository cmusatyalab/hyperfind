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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import edu.cmu.cs.delphi.api.ExamplesPerLabelConditionConfig;
import edu.cmu.cs.delphi.api.ModelConditionConfig;
import edu.cmu.cs.delphi.api.ModelConfig;
import edu.cmu.cs.delphi.api.NoReexaminationStrategyConfig;
import edu.cmu.cs.delphi.api.PercentageThresholdPolicyConfig;
import edu.cmu.cs.delphi.api.ReexaminationStrategyConfig;
import edu.cmu.cs.delphi.api.RetrainPolicyConfig;
import edu.cmu.cs.delphi.api.SVMConfig;
import edu.cmu.cs.delphi.api.SVMMode;
import edu.cmu.cs.delphi.api.SelectorConfig;
import edu.cmu.cs.delphi.api.TopKSelectorConfig;
import java.util.List;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableDelphiConfiguration.class)
@JsonDeserialize(as = ImmutableDelphiConfiguration.class)
@Value.Style(jdkOnly = true)
public interface DelphiConfiguration {

    @Value.Default
    default List<ModelConditionConfig> trainStrategy() {
        return ImmutableList.of(ModelConditionConfig.newBuilder()
                .setExamplesPerLabel(ExamplesPerLabelConditionConfig.newBuilder()
                        .setModel(ModelConfig.newBuilder()
                                .setSvm(SVMConfig.newBuilder()
                                        .setMode(SVMMode.DISTRIBUTED)
                                        .setFeatureExtractor("mpncov_resnet50")
                                        .setLinearOnly(true)
                                        .setProbability(false)
                                        .build())
                                .build())
                        .setCount(5)
                        .build())
                .build());
    }

    @Value.Default
    default RetrainPolicyConfig retrainPolicy() {
        return RetrainPolicyConfig.newBuilder()
                .setPercentage(PercentageThresholdPolicyConfig.newBuilder()
                        .setThreshold(0.1)
                        .build())
                .build();
    }

    @Value.Default
    default SelectorConfig selector() {
        return SelectorConfig.newBuilder()
                .setTopk(TopKSelectorConfig.newBuilder()
                        .setK(20)
                        .setBatchSize(10000)
                        .setReexaminationStrategy(ReexaminationStrategyConfig.newBuilder()
                                .setNone(NoReexaminationStrategyConfig.newBuilder())))
                .build();
    }

    @Value.Default
    default boolean shouldDownload() {
        return true;
    }

    @Value.Default
    default String downloadPathRoot() {
        return System.getProperty("user.home");
    }

    @Value.Default
    default int port() {
        return 6177;
    }

    @Value.Default
    default boolean useSsl() {
        return false;
    }

    Optional<String> truststorePath();

    @Value.Default
    default boolean onlyUseBetterModels() {
        return false;
    }

    @Value.Default
    default boolean colorByModelVersion() {
        return false;
    }
}
