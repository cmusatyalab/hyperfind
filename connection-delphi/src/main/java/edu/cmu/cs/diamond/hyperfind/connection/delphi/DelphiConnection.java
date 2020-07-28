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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.collect.ImmutableList;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import edu.cmu.cs.diamond.hyperfind.connection.api.Connection;
import edu.cmu.cs.diamond.hyperfind.connection.api.Filter;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchFactory;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchInfo;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchListenable;
import edu.cmu.cs.diamond.hyperfind.connection.api.bundle.Bundle;
import edu.cmu.cs.diamond.hyperfind.connection.api.bundle.BundleState;
import edu.cmu.cs.diamond.hyperfind.connection.diamond.FromDiamond;
import edu.cmu.cs.diamond.opendiamond.BundleFactory;
import edu.cmu.cs.diamond.opendiamond.CookieMap;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public final class DelphiConnection implements Connection {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory())
            .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE)
            .registerModule(new Jdk8Module())
            .registerModule(new ProtobufModule());

    private static final File CONFIG_FILE = Paths.get(System.getProperty("user.home"))
            .resolve(".diamond")
            .resolve("hyperfind-delphi.yml")
            .toFile();

    private final BundleFactory bundleFactory;
    private final ExecutorService resultExecutor;

    private DelphiConfiguration config = loadConfig();

    public DelphiConnection(String bundleDirs, String filterDirs) {
        this.bundleFactory = new BundleFactory(splitDirs(bundleDirs), splitDirs(filterDirs));
        this.resultExecutor = Executors.newCachedThreadPool();
    }

    @Override
    public SearchFactory getSearchFactory(List<Filter> filters) {
        try {
            CookieMap cookieMap = CookieMap.createDefaultCookieMap();
            return new DelphiSearchFactory(config, filters, cookieMap, resultExecutor);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create cookie map", e);
        }
    }

    @Override
    public List<SearchInfo> getRunningSearches() {
        return ImmutableList.of();
    }

    @Override
    public List<Bundle> getBundles() {
        return bundleFactory.getBundles().stream().map(FromDiamond::convert).collect(Collectors.toList());
    }

    @Override
    public Bundle getBundle(InputStream inputStream) {
        try {
            return FromDiamond.convert(bundleFactory.getBundle(inputStream));
        } catch (IOException e) {
            throw new RuntimeException("Failed to get bundle", e);
        }
    }

    @Override
    public Bundle restoreBundle(BundleState state) {
        try {
            return FromDiamond.convert(new edu.cmu.cs.diamond.opendiamond.Bundle(
                    new edu.cmu.cs.diamond.opendiamond.Bundle.PreparedFileLoader(
                            state.bundleContents(),
                            state.memberDirs().stream().map(File::new).collect(Collectors.toList()))));
        } catch (IOException e) {
            throw new RuntimeException("Failed to restore bundle", e);
        }
    }

    @Override
    public void openConfigPanel(SearchListenable searchListenable) {
        new DelphiConfigFrame(searchListenable, config, this::saveConfig);
    }

    private DelphiConfiguration loadConfig() {
        if (CONFIG_FILE.exists()) {
            try {
                return OBJECT_MAPPER.readValue(CONFIG_FILE, DelphiConfiguration.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load config file", e);
            }
        } else {
            return ImmutableDelphiConfiguration.builder().build();
        }
    }

    private void saveConfig(DelphiConfiguration newConfig) {
        try {
            OBJECT_MAPPER.writeValue(CONFIG_FILE, newConfig);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config file", e);
        }

        config = newConfig;
    }

    private static List<File> splitDirs(String paths) {
        return Arrays.stream(paths.split(":")).map(File::new).filter(File::isDirectory).collect(Collectors.toList());
    }
}
