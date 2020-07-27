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

package edu.cmu.cs.diamond.hyperfind.connection.delphi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import edu.cmu.cs.diamond.hyperfind.connection.api.Search;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchListenable;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchListener;
import edu.cmu.cs.diamond.hyperfind.connection.delphi.ImmutableDelphiConfiguration.Builder;
import edu.cmu.cs.diamond.hyperfind.connection.delphi.jackson.MessageListSerializer;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class DelphiConfigFrame extends JFrame implements SearchListener {

    private final JTextArea trainArea = createTextField();
    private final JTextArea retrainArea = createTextField();
    private final JTextArea selectorArea = createTextField();

    private final JCheckBox checkDownload = new JCheckBox("Download results to directory: ");
    private final JTextField textDownload = new JTextField();

    private final JTextField textPort = new JTextField();

    private final JCheckBox checkSsl = new JCheckBox("Use SSL");
    private final JTextField textTruststore = new JTextField();

    private final JCheckBox checkOnlyUseBetterModels = new JCheckBox("Only Deploy Better Models");
    private final JCheckBox checkColorByModelVersion = new JCheckBox("Color by Model Version");

    private final JButton buttonDownload = new JButton("Download Model");

    public DelphiConfigFrame(
            SearchListenable searchListenable,
            DelphiConfiguration config,
            Consumer<DelphiConfiguration> saveCallback) {
        searchListenable.addListener(this);

        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0;
        constraints.insets = new Insets(10, 10, 5, 10);
        constraints.anchor = GridBagConstraints.WEST;

        add(new JLabel("Train Strategy"), constraints);
        constraints.gridx += 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(trainArea, constraints);

        constraints.gridy += 1;
        constraints.gridx = 0;
        constraints.weightx = 0;
        add(new JLabel("Retrain Policy"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(retrainArea, constraints);

        constraints.gridy += 1;
        constraints.gridx = 0;
        constraints.weightx = 0;
        add(new JLabel("Result Selector"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(selectorArea, constraints);

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
        add(new JLabel("Delphi Port"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(textPort, constraints);

        constraints.gridy += 1;
        constraints.gridx = 0;
        constraints.weightx = 0;
        add(checkSsl, constraints);

        constraints.gridy += 1;
        constraints.gridx = 0;
        constraints.weightx = 0;
        add(new JLabel("Truststore Path"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(textTruststore, constraints);

        constraints.gridy += 1;
        constraints.gridx = 0;
        constraints.weightx = 0;
        add(checkOnlyUseBetterModels, constraints);

        constraints.gridy += 1;
        constraints.gridx = 0;
        constraints.weightx = 0;
        add(checkColorByModelVersion, constraints);

        constraints.gridy += 1;
        constraints.gridx = 0;
        constraints.weightx = 0;
        buttonDownload.setEnabled(false);
        add(buttonDownload, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        JButton buttonSave = new JButton("Save Config");
        add(buttonSave, constraints);

        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent _arg) {
                Builder builder = ImmutableDelphiConfiguration.builder();
                saveCallback.accept(builder.build());
            }
        });

        SimpleModule module = new SimpleModule();
        module.addSerializer(new MessageListSerializer());

        trainArea.setText(print(config.trainStrategy()));

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setSize(500, 600);
        setLocationRelativeTo(null);
        setVisible(true);

        checkProxy.setSelected(Boolean.parseBoolean(config.getProperty("useProxy")));
        textProxy.setEnabled(checkProxy.isSelected());
        textProxy.setText(config.getProperty("proxyIP", ""));
        checkDownload.setSelected(Boolean.parseBoolean(config.getProperty("downloadResults")));
        textDownload.setEnabled(checkDownload.isSelected());
        textDownload.setText(config.getProperty("downloadDirectory"));

        checkProxy.addItemListener(_ignore -> textProxy.setEnabled(checkProxy.isSelected()));
        checkDownload.addItemListener(_ignore -> textDownload.setEnabled(checkDownload.isSelected()));

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent _event) {
                searchListenable.removeListener(DelphiConfigFrame.this);
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
        Arrays.stream(buttonDownload.getActionListeners()).forEach(buttonDownload::removeActionListener);
        buttonDownload.addActionListener(_ignore -> retrainCallback.run());

        buttonDownload.setEnabled(true);
    }

    @Override
    public void searchStopped() {
        checkProxy.setEnabled(true);
        textProxy.setEnabled(true);
        checkDownload.setEnabled(true);
        textDownload.setEnabled(true);
        buttonDownload.setEnabled(false);
    }

    private static JTextArea createTextField() {
        JTextArea textArea = new JTextArea();
        textArea.setColumns(20);
        textArea.setLineWrap(true);
        textArea.setRows(5);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        return textArea;
    }

    private static String print(MessageOrBuilder message) {
        try {
            return JsonFormat.printer().print(message);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Failed to print message", e);
        }
    }
}
