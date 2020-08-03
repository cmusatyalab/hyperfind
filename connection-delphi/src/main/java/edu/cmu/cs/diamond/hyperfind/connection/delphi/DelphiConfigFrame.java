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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import edu.cmu.cs.diamond.hyperfind.connection.api.Search;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchListenable;
import edu.cmu.cs.diamond.hyperfind.connection.api.SearchListener;
import java.awt.BorderLayout;
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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class DelphiConfigFrame extends JFrame implements SearchListener {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE)
            .registerModule(new Jdk8Module())
            .registerModule(new ProtobufModule());

    private final JTextArea areaTrain = createTextField();
    private final JTextArea areaRetrain = createTextField();
    private final JTextArea areaSelector = createTextField();

    private final JCheckBox checkExamples = new JCheckBox("Starting examples directory:");
    private final JTextField textExamples = new JTextField();
    private final JButton buttonExamples = new JButton("Browse");

    private final JCheckBox checkDownloadPath = new JCheckBox("Download results to directory: ");
    private final JTextField textDownloadPath = new JTextField();
    private final JButton buttonDownloadPath = new JButton("Browse");

    private final JTextField textPort = new JTextField();

    private final JCheckBox checkSsl = new JCheckBox("Use SSL");
    private final JTextField textTruststore = new JTextField();
    private final JButton buttonTruststore = new JButton("Browse");

    private final JCheckBox checkOnlyUseBetterModels = new JCheckBox("Only Deploy Better Models");
    private final JCheckBox checkColorByModelVersion = new JCheckBox("Color by Model Version");

    private final JButton buttonDownloadModel = new JButton("Download Model");

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

        constraints.weighty = 20;
        add(new JLabel("Train Strategy"), constraints);
        constraints.gridx += 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.BOTH;
        add(new JScrollPane(areaTrain), constraints);

        constraints.weighty = 8;
        constraints.gridy += 1;
        constraints.gridx = 0;
        constraints.weightx = 0;
        add(new JLabel("Retrain Policy"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.BOTH;
        add(new JScrollPane(areaRetrain), constraints);

        constraints.weighty = 8;
        constraints.gridy += 1;
        constraints.gridx = 0;
        constraints.weightx = 0;
        add(new JLabel("Result Selector"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.BOTH;
        add(new JScrollPane(areaSelector), constraints);

        constraints.weighty = 1;
        constraints.gridy += 1;
        constraints.gridx = 0;
        constraints.weightx = 0;
        add(checkExamples, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        JPanel examplePanel = new JPanel(new BorderLayout(5, 0));
        examplePanel.add(textExamples, BorderLayout.CENTER);
        examplePanel.add(buttonExamples, BorderLayout.LINE_END);
        add(examplePanel, constraints);

        constraints.weighty = 1;
        constraints.gridy += 1;
        constraints.gridx = 0;
        constraints.weightx = 0;
        add(checkDownloadPath, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        JPanel downloadPanel = new JPanel(new BorderLayout(5, 0));
        downloadPanel.add(textDownloadPath, BorderLayout.CENTER);
        downloadPanel.add(buttonDownloadPath, BorderLayout.LINE_END);
        add(downloadPanel, constraints);

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
        JPanel truststorePanel = new JPanel(new BorderLayout(5, 0));
        truststorePanel.add(textTruststore, BorderLayout.CENTER);
        truststorePanel.add(buttonTruststore, BorderLayout.LINE_END);
        add(truststorePanel, constraints);

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
        buttonDownloadModel.setEnabled(false);
        add(buttonDownloadModel, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        JButton buttonSave = new JButton("Save Config");
        add(buttonSave, constraints);

        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent _e) {
                ImmutableDelphiConfiguration.Builder builder = ImmutableDelphiConfiguration.builder();
                builder.addAllTrainStrategy(fromString(areaTrain.getText(), new TypeReference<>() {}));
                builder.retrainPolicy(fromString(areaRetrain.getText(), new TypeReference<>() {}));
                builder.selector(fromString(areaSelector.getText(), new TypeReference<>() {}));
                builder.shouldIncludeExamples(checkExamples.isSelected());
                builder.examplePath(textExamples.getText());
                builder.shouldDownload(checkDownloadPath.isSelected());
                builder.downloadPathRoot(textDownloadPath.getText());
                builder.port(fromString(textPort.getText(), new TypeReference<>() {}));
                builder.useSsl(checkSsl.isSelected());
                builder.truststorePath(textTruststore.getText());
                builder.onlyUseBetterModels(checkOnlyUseBetterModels.isSelected());
                builder.colorByModelVersion(checkColorByModelVersion.isSelected());

                saveCallback.accept(builder.build());
            }
        });

        buttonExamples.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent _e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int option = fileChooser.showOpenDialog(DelphiConfigFrame.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    textExamples.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        buttonDownloadPath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent _e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int option = fileChooser.showOpenDialog(DelphiConfigFrame.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    textDownloadPath.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        buttonTruststore.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent _e) {
                JFileChooser fileChooser = new JFileChooser();
                int option = fileChooser.showOpenDialog(DelphiConfigFrame.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    textTruststore.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        areaTrain.setText(toString(config.trainStrategy()));
        areaRetrain.setText(toString(config.retrainPolicy()));
        areaSelector.setText(toString(config.selector()));

        checkExamples.setSelected(config.shouldIncludeExamples());
        textExamples.setEnabled(config.shouldIncludeExamples());
        textExamples.setText(config.examplePath());
        buttonExamples.setEnabled(config.shouldIncludeExamples());
        checkExamples.addItemListener(_ignore -> {
            textExamples.setEnabled(checkExamples.isSelected());
            buttonExamples.setEnabled(checkExamples.isSelected());
        });

        checkDownloadPath.setSelected(config.shouldDownload());
        textDownloadPath.setEnabled(config.shouldDownload());
        textDownloadPath.setText(config.downloadPathRoot());
        buttonDownloadPath.setEnabled(config.shouldDownload());
        checkDownloadPath.addItemListener(_ignore -> {
            textDownloadPath.setEnabled(checkDownloadPath.isSelected());
            buttonDownloadPath.setEnabled(checkDownloadPath.isSelected());
        });

        textPort.setText(toString(config.port()));

        checkSsl.setSelected(config.useSsl());
        textTruststore.setEnabled(config.useSsl());
        textTruststore.setText(config.truststorePath());
        buttonTruststore.setEnabled(config.useSsl());
        checkSsl.addItemListener(_ignore -> {
            textTruststore.setEnabled(checkSsl.isSelected());
            buttonTruststore.setEnabled(checkSsl.isSelected());
        });

        checkOnlyUseBetterModels.setSelected(config.onlyUseBetterModels());
        checkColorByModelVersion.setSelected(config.colorByModelVersion());

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setSize(500, 650);
        setLocationRelativeTo(null);
        setVisible(true);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent _event) {
                searchListenable.removeListener(DelphiConfigFrame.this);
            }
        });
    }

    @Override
    public void searchStarted(Search search, Runnable _retrainCallback) {
        setConfigEnabled(false);

        // Remove old listeners
        Arrays.stream(buttonDownloadModel.getActionListeners()).forEach(buttonDownloadModel::removeActionListener);
        buttonDownloadModel.addActionListener(_ignore -> ((DelphiSearch) search).downloadModel());

        buttonDownloadModel.setEnabled(true);
    }

    @Override
    public void searchStopped() {
        setConfigEnabled(true);
        buttonDownloadModel.setEnabled(false);
    }

    private void setConfigEnabled(boolean enabled) {
        areaTrain.setEnabled(enabled);
        areaRetrain.setEnabled(enabled);
        areaSelector.setEnabled(enabled);
        checkExamples.setEnabled(enabled);
        textExamples.setEnabled(enabled && checkExamples.isSelected());
        buttonExamples.setEnabled(enabled && checkExamples.isSelected());
        checkDownloadPath.setEnabled(enabled);
        textDownloadPath.setEnabled(enabled && checkDownloadPath.isSelected());
        buttonDownloadPath.setEnabled(enabled && checkDownloadPath.isSelected());
        textPort.setEnabled(enabled);
        checkSsl.setEnabled(enabled);
        textTruststore.setEnabled(enabled && checkSsl.isSelected());
        buttonTruststore.setEnabled(enabled && checkSsl.isSelected());
        checkOnlyUseBetterModels.setEnabled(enabled);
        checkColorByModelVersion.setEnabled(enabled);
    }

    private <T> T fromString(String message, TypeReference<T> reference) {
        try {
            return OBJECT_MAPPER.readValue(message, reference);
        } catch (JsonProcessingException e) {
            JOptionPane.showMessageDialog(this, "Failed to parse message: " + message);
            throw new RuntimeException("Failed to parse message: " + message, e);
        }
    }

    private static JTextArea createTextField() {
        JTextArea textArea = new JTextArea();
        textArea.setColumns(20);
        textArea.setLineWrap(true);
        textArea.setRows(5);
        textArea.setWrapStyleWord(true);
        return textArea;
    }

    private static String toString(Object message) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to print message", e);
        }
    }
}