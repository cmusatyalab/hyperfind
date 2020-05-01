/*
 *  HyperFind, a search application for the OpenDiamond platform
 *
 *  Copyright (c) 2007-2010 Carnegie Mellon University
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

import edu.cmu.cs.diamond.hyperfind.connection.api.SearchStats;
import java.util.Map;
import javax.swing.JProgressBar;

public class StatisticsBar extends JProgressBar {
    public StatisticsBar() {
        setStringPainted(true);
        clear();
    }

    public void clear() {
        setNumbers(0, 0, 0);
    }

    private void setNumbers(long total, long searched, long dropped) {
        setIndeterminate(false);
        long passed = searched - dropped;
        String str = String.format("Total %d, Searched %d, Dropped %d (%.2f%%), Passed %d (%.2f%%)",
                total, searched, dropped, 100f * dropped / searched, passed, 100f * passed / searched);
        setString(str);
        setMaximum(total > Integer.MAX_VALUE ?
                Integer.MAX_VALUE : (int) total);
        setValue(searched > Integer.MAX_VALUE ?
                Integer.MAX_VALUE : (int) searched);
    }

    public void update(Map<String, SearchStats> serverStats, long discardedPositives) {
        long total = 0;
        long processed = 0;
        long dropped = discardedPositives;
        for (SearchStats ss : serverStats.values()) {
            total += ss.totalObjects();
            processed += ss.processedObjects();
            dropped += ss.droppedObjects();
        }
        setNumbers(total, processed, dropped);
    }

    public void setIndeterminateMessage(String message) {
        setIndeterminate(true);
        setString(message);
    }

    public void showException(Throwable e) {
        setIndeterminate(false);
        Throwable c = e.getCause();
        String msg = e.getLocalizedMessage();
        if (c != null) {
            msg += ": " + c;
        }
        setString(msg);
    }

    public void setDone() {
        if (isIndeterminate()) {
            clear();
            setIndeterminate(false);
        }
    }
}
