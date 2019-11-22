/*
 *  HyperFind, a search application for the OpenDiamond platform
 *
 *  Copyright (c) 2009-2012 Carnegie Mellon University
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

// Resource: https://www.codejava.net/coding/reading-and-writing-configuration-for-java-application-using-properties-class

package edu.cmu.cs.diamond.hyperfind;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.net.URL;

public class HyperFindProperty extends JFrame {

	private File configFile = new File(System.getProperty("user.dir")+"/src/resources/config.properties");
	private Properties configProps;
	private JLabel labelProxy = new JLabel("Proxy IP: ");
	private JLabel labelDownload = new JLabel("Download Results: ");
	private JLabel labelDirectory = new JLabel("Download directory: ");
	private JLabel labelSessionName = new JLabel("Session Name (for history window): ");

	private JTextField textProxy = new JTextField();
	private JTextField textDownload = new JTextField(); //TODO: Boolean Option
	private JTextField textDirectory = new JTextField();
    private JTextField textSessionName = new JTextField();

    private JButton buttonSave = new JButton("Save");

	public HyperFindProperty() {

		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 0;
		constraints.insets = new Insets(10, 10, 5, 10);
		constraints.anchor = GridBagConstraints.WEST;
		add(labelProxy, constraints);

		constraints.gridx += 1;
		constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
		add(textProxy, constraints);

		constraints.gridy += 1;
		constraints.gridx = 0;
		constraints.weightx = 0;
		add(labelDownload, constraints);

		constraints.gridx = 1;
		constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
		add(textDownload, constraints);

		constraints.gridy += 1;
		constraints.gridx = 0;
		constraints.weightx = 0;
		add(labelDirectory, constraints);

		constraints.gridx = 1;
		constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
		add(textDirectory, constraints);

		constraints.gridy += 1;
		constraints.gridx = 0;
		constraints.weightx = 0;
		add(labelSessionName, constraints);

		constraints.gridx = 1;
		constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
		add(textSessionName, constraints);

		constraints.gridy += 1;
		constraints.gridx = 0;
		constraints.weightx = 0;
		constraints.gridwidth = 2;
		constraints.anchor = GridBagConstraints.CENTER;
		add(buttonSave, constraints);

		buttonSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					saveProperties();
                    dispose();
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(HyperFindProperty.this,
							"Error saving properties file: " + ex.getMessage());
                    dispose();
				}
			}

		});

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pack();
        setSize(500, 200);
		setLocationRelativeTo(null);
		setVisible(true);

		try {
			loadProperties();
		} catch (IOException ex) {
            ex.printStackTrace();
		}
		textProxy.setText(configProps.getProperty("proxyIP"));
		textDownload.setText(configProps.getProperty("downloadResults"));
		textDirectory.setText(configProps.getProperty("downloadDirectory"));
        textSessionName.setText(configProps.getProperty("sessionName"));
	}

	private void loadProperties() throws IOException {
		Properties defaultProps = new Properties();
		// sets default properties
		defaultProps.setProperty("proxyIP", "");
		defaultProps.setProperty("downloadResults", "false");
		defaultProps.setProperty("downloadDirectory", System.getProperty("user.home"));
        defaultProps.setProperty("sessionName", "search_history");
		configProps = new Properties(defaultProps);

		// loads properties from file
		InputStream inputStream = new FileInputStream(configFile);
		configProps.load(inputStream);
		inputStream.close();
	}

	private void saveProperties() throws IOException {
		configProps.setProperty("proxyIP", textProxy.getText());
		configProps.setProperty("downloadResults", textDownload.getText());
		configProps.setProperty("downloadDirectory", textDirectory.getText());
        configProps.setProperty("sessionName", textSessionName.getText());
		OutputStream outputStream = new FileOutputStream(configFile);
		configProps.store(outputStream, "hyperfind settings");
		outputStream.close();
	}

    public String getProxyIP() {
        return textProxy.getText();
    }

    public Boolean checkDownload() {
       return Boolean.parseBoolean(textDownload.getText().toLowerCase());
    }

    public String getDownloadDirectory() {
        return textDirectory.getText();
    }

    public String getSessionName() {
        return textSessionName.getText();
    }
}
