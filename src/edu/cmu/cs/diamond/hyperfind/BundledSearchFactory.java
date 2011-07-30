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
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import edu.cmu.cs.diamond.opendiamond.BundleFactory;
import edu.cmu.cs.diamond.opendiamond.Bundle;
import edu.cmu.cs.diamond.opendiamond.bundle.OptionGroup;
import edu.cmu.cs.diamond.opendiamond.bundle.Option;
import edu.cmu.cs.diamond.opendiamond.bundle.ExampleOption;

public class BundledSearchFactory extends HyperFindSearchFactory {

    private final Bundle bundle;

    private final boolean needsExamples;

    private BundledSearchFactory(Bundle bundle) throws IOException {
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

    @Override
    public String getDisplayName() {
        return bundle.getDisplayName();
    }

    @Override
    public boolean isCodec() {
        return bundle.isCodec();
    }

    @Override
    public HyperFindSearch createHyperFindSearch() throws IOException,
            InterruptedException {
        return new BundledSearch(bundle);
    }

    @Override
    public boolean needsPatches() {
        return needsExamples;
    }

    @Override
    public HyperFindSearch createHyperFindSearch(List<BufferedImage> patches)
            throws IOException, InterruptedException {
        HyperFindSearch search = createHyperFindSearch();
        search.addPatches(patches);
        return search;
    }

    public static HyperFindSearch createHyperFindSearch(
            BundleFactory bundleFactory, InputStream in) throws IOException {
        Bundle bundle = bundleFactory.getBundle(in);
        if (bundle.isCodec()) {
            throw new IOException("Codecs cannot be imported at runtime.");
        }
        return new BundledSearch(bundle);
    }

    public static List<HyperFindSearchFactory> createHyperFindSearchFactories(
            BundleFactory bundleFactory) throws IOException {
        List<HyperFindSearchFactory> factories =
                new ArrayList<HyperFindSearchFactory>();
        for (Bundle b : bundleFactory.getBundles()) {
            factories.add(new BundledSearchFactory(b));
        }
        return factories;
    }
}
