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

// Resource: https://www.codejava.net/coding/reading-and-writing-configuration-for-java-application-using-properties-class

package edu.cmu.cs.diamond.hyperfind.connection.diamond;

import edu.cmu.cs.diamond.hyperfind.connection.api.Search;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchListenable;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiConsumer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class DiamondConfigFrame extends JFrame implements SearchListener {

    private final JCheckBox checkProxy = new JCheckBox("Use proxy IP: ");
    private final JTextField textProxy = new JTextField();

    private final JCheckBox checkDownload = new JCheckBox("Download results to directory: ");
    private final JTextField textDownload = new JTextField();

    private final JButton buttonRetrain = new JButton("Retrain Proxy");

    public DiamondConfigFrame(
            SearchListenable searchListenable,
            Properties configProps,
            BiConsumer<Optional<String>, Optional<String>> saveCallback) {
        searchListenable.addListener(this);
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0;
        constraints.insets = new Insets(10, 10, 5, 10);
        constraints.anchor = GridBagConstraints.WEST;
        add(checkProxy, constraints);

        constraints.gridx += 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(textProxy, constraints);

        constraints.gridy += 1;
        constraints.gridx = 0;
        constraints.weightx = 0;
        add(checkDownload, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(textDownload, constraints);

        constraints.gridy += 1;
        constraints.gridx = 0;
        constraints.weightx = 0;
        buttonRetrain.setEnabled(false);
        add(buttonRetrain, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        JButton buttonSave = new JButton("Save Config");
        add(buttonSave, constraints);

        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent _arg) {
                saveCallback.accept(
                        checkProxy.isSelected() ? Optional.of(textProxy.getText()) : Optional.empty(),
                        checkDownload.isSelected() ? Optional.of(textDownload.getText()) : Optional.empty());
            }
        });

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setSize(500, 130);
        setLocationRelativeTo(null);
        setVisible(true);

        checkProxy.setSelected(Boolean.parseBoolean(configProps.getProperty("useProxy")));
        textProxy.setEnabled(checkProxy.isSelected());
        textProxy.setText(configProps.getProperty("proxyIP", ""));
        checkDownload.setSelected(Boolean.parseBoolean(configProps.getProperty("downloadResults")));
        textDownload.setEnabled(checkDownload.isSelected());
        textDownload.setText(configProps.getProperty("downloadDirectory"));

        checkProxy.addItemListener(_ignore -> textProxy.setEnabled(checkProxy.isSelected()));
        checkDownload.addItemListener(_ignore -> textDownload.setEnabled(checkDownload.isSelected()));

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent _event) {
                searchListenable.removeListener(DiamondConfigFrame.this);
            }
        });
    }

    @Override
    public void searchStarted(Search _search, Runnable retrainCallback) {
        checkProxy.setEnabled(false);
        textProxy.setEnabled(false);
        checkDownload.setEnabled(false);
        textDownload.setEnabled(false);

        // Remove old listeners
        Arrays.stream(buttonRetrain.getActionListeners()).forEach(buttonRetrain::removeActionListener);
        buttonRetrain.addActionListener(_ignore -> retrainCallback.run());

        buttonRetrain.setEnabled(true);
    }

    @Override
    public void searchStopped() {
        checkProxy.setEnabled(true);
        textProxy.setEnabled(checkProxy.isSelected());
        checkDownload.setEnabled(true);
        textDownload.setEnabled(checkDownload.isSelected());
        buttonRetrain.setEnabled(false);
    }
}
