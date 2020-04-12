/*
 *  HyperFind, a search application for the OpenDiamond platform
 *
 *  Copyright (c) 2008-2011 Carnegie Mellon University
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

package edu.cmu.cs.diamond.hyperfind.collaboration;

import edu.cmu.cs.diamond.hyperfind.connection.api.Connection;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchInfo;
import edu.cmu.cs.diamond.hyperfind.persistence.HyperFindPersistence;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;

public class SearchSelector extends JFrame {

    private final Connection connection;
    private final Consumer<Optional<SearchInfo>> selectionCallback;

    public SearchSelector(
            Connection connection,
            Consumer<Optional<SearchInfo>> selectionCallback) {
        super("Running Searches");
        this.connection = connection;
        this.selectionCallback = selectionCallback;
        initUI();
    }

    private void initUI() {
        List<SessionState> sessions = loadSessions();

        String[] items = Stream.concat(Stream.of("Start new search"),
                sessions.stream().map(SearchSelector::toListItem))
                .toArray(String[]::new);

        JList<String> list = new JList<>(items);
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    dispose();
                    int index = list.locationToIndex(evt.getPoint());
                    if (index == 0) {
                        selectionCallback.accept(Optional.empty());
                    } else {
                        selectionCallback.accept(Optional.of(sessions.get(index - 1)));
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(list);
        add(scrollPane);

        setSize(new Dimension(480, 100));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private static List<SessionState> loadSessions() {
        if (!SESSIONS_DIR.toFile().exists()) {
            return Collections.emptyList();
        }

        try (Stream<Path> files = Files.list(SESSIONS_DIR)) {
            return files.map(SessionSelector::getSessionState)
                    .sorted(Comparator.comparingLong(SessionState::getLastModified).reversed())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to list files in session directory " + SESSIONS_DIR, e);
        }
    }

    private static String toListItem(SessionState state) {
        return String.format("Running search (started %s)", Instant.ofEpochMilli(state.getLastModified()));
    }

    private static SessionState getSessionState(Path path) {
        File file = path.toFile();
        return new SessionState(file.getName(), file.lastModified(), HyperFindPersistence.loadPredicates(file));
    }
}
