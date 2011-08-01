/*
 *  HyperFind, an search application for the OpenDiamond platform
 *
 *  Copyright (c) 2009-2011 Carnegie Mellon University
 *  All rights reserved.
 *
 *  HyperFind is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, version 2.
 *
 *  HyperFind is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with HyperFind. If not, see <http://www.gnu.org/licenses/>.
 *
 *  Linking HyperFind statically or dynamically with other modules is
 *  making a combined work based on HyperFind. Thus, the terms and
 *  conditions of the GNU General Public License cover the whole
 *  combination.
 * 
 *  In addition, as a special exception, the copyright holders of
 *  HyperFind give you permission to combine HyperFind with free software
 *  programs or libraries that are released under the GNU LGPL or the
 *  Eclipse Public License 1.0. You may copy and distribute such a system
 *  following the terms of the GNU GPL for HyperFind and the licenses of
 *  the other code concerned, provided that you include the source code of
 *  that other code when and as the GNU GPL requires distribution of source
 *  code.
 *
 *  Note that people who make modified versions of HyperFind are not
 *  obligated to grant this special exception for their modified versions;
 *  it is their choice whether to do so. The GNU General Public License
 *  gives permission to release a modified version without this exception;
 *  this exception also makes it possible to release a modified version
 *  which carries forward this exception.
 */

package edu.cmu.cs.diamond.hyperfind;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.util.*;

import edu.cmu.cs.diamond.opendiamond.BundleFactory;
import edu.cmu.cs.diamond.opendiamond.Bundle;
import edu.cmu.cs.diamond.opendiamond.bundle.OptionGroup;
import edu.cmu.cs.diamond.opendiamond.bundle.Option;
import edu.cmu.cs.diamond.opendiamond.bundle.ExampleOption;

public class HyperFindSearchFactory {

    private final Bundle bundle;

    private final boolean needsExamples;

    private HyperFindSearchFactory(Bundle bundle) throws IOException {
        this.bundle = bundle;
        boolean needsExamples = false;
        for (OptionGroup group : bundle.getOptions()) {
            for (Option option : group.getOptions()) {
                if (option instanceof ExampleOption) {
                    needsExamples = true;
                }
            }
        }
        this.needsExamples = needsExamples;
    }

    public String getDisplayName() {
        return bundle.getDisplayName();
    }

    public boolean isCodec() {
        return bundle.isCodec();
    }

    public boolean needsExamples() {
        return needsExamples;
    }

    public HyperFindSearch createHyperFindSearch() throws IOException {
        return new BundledSearch(bundle);
    }

    public HyperFindSearch createHyperFindSearch(List<BufferedImage> examples)
            throws IOException {
        HyperFindSearch search = createHyperFindSearch();
        search.addExamples(examples);
        return search;
    }

    public static HyperFindSearch createHyperFindSearch(
            BundleFactory bundleFactory, URI uri) throws IOException {
        // System.out.println("trying " + uri);
        InputStream in = uri.toURL().openStream();
        Bundle bundle = bundleFactory.getBundle(in);
        if (bundle.isCodec()) {
            throw new IOException("Codecs cannot be imported at runtime.");
        }
        return new BundledSearch(bundle);
    }

    public static List<HyperFindSearchFactory>
            createHyperFindSearchFactories(BundleFactory bundleFactory)
            throws IOException {
        List<HyperFindSearchFactory> factories =
                new ArrayList<HyperFindSearchFactory>();

        for (Bundle b : bundleFactory.getBundles()) {
            factories.add(new HyperFindSearchFactory(b));
        }

        Collections.sort(factories, new Comparator<HyperFindSearchFactory>() {
            @Override
            public int compare(HyperFindSearchFactory o1,
                    HyperFindSearchFactory o2) {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        });
        return factories;
    }
}
