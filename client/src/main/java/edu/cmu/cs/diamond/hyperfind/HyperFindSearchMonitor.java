/*
 *  HyperFind, a search application for the OpenDiamond platform
 *
 *  Copyright (c) 2011 Carnegie Mellon University
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
 *  programs or libraries that are released under the GNU LGPL, the
 *  Eclipse Public License 1.0, or the Apache License 2.0. You may copy and
 *  distribute such a system following the terms of the GNU GPL for
 *  HyperFind and the licenses of the other code concerned, provided that
 *  you include the source code of that other code when and as the GNU GPL
 *  requires distribution of source code.
 *
 *  Note that people who make modified versions of HyperFind are not
 *  obligated to grant this special exception for their modified versions;
 *  it is their choice whether to do so. The GNU General Public License
 *  gives permission to release a modified version without this exception;
 *  this exception also makes it possible to release a modified version
 *  which carries forward this exception.
 */

package edu.cmu.cs.diamond.hyperfind;

import java.util.List;
import java.util.Set;

/*
 * Super-class for any class wishing to be kept abreast of the progress of a
 * HyperFind search.
 */
public abstract class HyperFindSearchMonitor {

    /*
     * Called each time a search result returns.
     */
    public abstract void notify(HyperFindResult hr) throws InterruptedException;

    /*
     * Called when the user has stopped the current search but results are still
     * available for browsing.
     */
    public void stopped() {
    }

    /*
     * Called when the current search results are no longer available for
     * browsing, such as when a new search is started.
     */
    public void terminated() {
    }

    /*
     * Called when the list of selected results in the ThumbnailBox has
     * changed.
     */
    public void selectionChanged(List<HyperFindResult> selected) {
    }

    /*
     * Returns a list of attributes that this SearchMonitor intends to access.
     */
    public abstract Set<String> getPushAttributes();

}
