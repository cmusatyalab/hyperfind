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

package edu.cmu.cs.diamond.hyperfind;

import edu.cmu.cs.diamond.hyperfind.connector.api.Connection;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.Bundle;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.BundleType;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.ExampleOption;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.Option;
import edu.cmu.cs.diamond.hyperfind.connector.api.bundle.OptionGroup;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HyperFindPredicateFactory {

    private final Bundle bundle;

    private final boolean needsExamples;

    private HyperFindPredicateFactory(Bundle bundle) {
        this.bundle = bundle;
        boolean needsExamples = false;
        for (OptionGroup group : bundle.options()) {
            for (Option option : group.options()) {
                if (option instanceof ExampleOption) {
                    needsExamples = true;
                }
            }
        }
        this.needsExamples = needsExamples;
    }

    public String getDisplayName() {
        return bundle.displayName();
    }

    public BundleType getType() {
        return bundle.type();
    }

    public boolean needsExamples() {
        return needsExamples;
    }

    public HyperFindPredicate createHyperFindPredicate() throws IOException {
        return new HyperFindPredicate(bundle);
    }

    public HyperFindPredicate createHyperFindPredicate(List<BufferedImage> examples) throws IOException {
        HyperFindPredicate predicate = createHyperFindPredicate();
        predicate.addExamples(examples);
        return predicate;
    }

    public static HyperFindPredicate createHyperFindPredicate(Connection connection, URI uri) throws IOException {
        InputStream in = uri.toURL().openStream();
        Bundle bundle = connection.getBundle(in);

        if (bundle.type() != BundleType.PREDICATE) {
            throw new IllegalArgumentException("Codecs cannot be imported at runtime.");
        }

        return new HyperFindPredicate(bundle);
    }

    public static List<HyperFindPredicateFactory> createHyperFindPredicateFactories(List<Bundle> bundles) {
        List<HyperFindPredicateFactory> factories =
                new ArrayList<HyperFindPredicateFactory>();

        for (Bundle b : bundles) {
            factories.add(new HyperFindPredicateFactory(b));
        }

        Collections.sort(
                factories,
                new Comparator<HyperFindPredicateFactory>() {
                    @Override
                    public int compare(
                            HyperFindPredicateFactory o1,
                            HyperFindPredicateFactory o2) {
                        return o1.getDisplayName().compareTo(o2.getDisplayName());
                    }
                }
        );

        return factories;
    }
}

