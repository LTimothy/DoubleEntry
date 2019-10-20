/**
 * Double Entry Validation Tool (WIP).
 *
 * @source: https://github.com/LTimothy/DoubleEntry
 * @author: Timothy Lee (https://timothylee.us)
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public class DoubleEntry extends JFrame
{
	// GUI Elements
	private static JTextArea status;
	private JTextField columnText;
	private JTextField prefixText;
	private JFileChooser fileChooser;
	private JButton submitBtn;
	private Font defaultFont;

	// Data Fields
	private static String filename;
	private static int idColumn;
	private static String idPrefix;
	private static File file;

	public DoubleEntry() {
		Container contain = getContentPane();
		contain.setLayout(new BorderLayout());
		defaultFont = new Font("Serif", Font.PLAIN, 12);
		ButtonListener listener = new ButtonListener();

		// NORTH PANEL
		Panel northPanel = new Panel(new GridLayout(4, 1));
		Panel filenamePanel = new Panel(new GridLayout(1, 2));
		Panel columnPanel = new Panel(new GridLayout(1, 2));
		Panel prefixPanel = new Panel(new GridLayout(1, 2));
		Panel buttonPanel = new Panel(new GridLayout(1, 4));

		// Filename Loading
		JLabel filenameLabel = new JLabel("Enter Filename");
		filenameLabel.setFont(defaultFont);
		filenamePanel.add(filenameLabel);
		fileChooser = new JFileChooser();
		JButton openButton = new JButton("Open File...");
		openButton.setFont(defaultFont);
		openButton.addActionListener(new FileListener());
		filenamePanel.add(openButton);

		// Column loading
		JLabel columnLabel = new JLabel("Enter ID Column # (0-indexed)");
		columnLabel.setFont(defaultFont);
		columnPanel.add(columnLabel);
		columnText = new JTextField(20);
		columnText.setFont(defaultFont);
		columnText.setEditable(true);
		columnPanel.add(columnText);

		// Prefix loading
		JLabel prefixLabel = new JLabel("Enter ID Prefix");
		prefixLabel.setFont(defaultFont);
		prefixPanel.add(prefixLabel);
		prefixText = new JTextField(20);
		prefixText.setFont(defaultFont);
		prefixText.setEditable(true);
		prefixPanel.add(prefixText);

		northPanel.add(filenamePanel);
		northPanel.add(columnPanel);
		northPanel.add(prefixPanel);

		// Instructions/Analyze/Clear Buttons
		JButton instructionsBtn = new JButton("Instructions");
		instructionsBtn.setFont(defaultFont);
		instructionsBtn.addActionListener(listener);
		buttonPanel.add(instructionsBtn);
		northPanel.add(buttonPanel);
		JButton clearBtn = new JButton("Clear");
		clearBtn.setFont(defaultFont);
		clearBtn.addActionListener(listener);
		buttonPanel.add(clearBtn);
		JButton analyzeBtn = new JButton("Analyze");
		analyzeBtn.setFont(defaultFont);
		analyzeBtn.addActionListener(listener);
		buttonPanel.add(analyzeBtn);
		JButton saveButton = new JButton("Save");
		saveButton.setFont(defaultFont);
		saveButton.addActionListener(new FileListener());
		buttonPanel.add(saveButton);
		northPanel.add(buttonPanel);

		contain.add(northPanel, BorderLayout.NORTH);

		// CENTER PANEL
		Panel southPanel = new Panel(new BorderLayout());

		// Status Panel
		status = new JTextArea();
		status.setFont(defaultFont);
		status.setLineWrap(true);
		status.setWrapStyleWord(true);
		JScrollPane statusLog = new JScrollPane(status);
		southPanel.add(statusLog, BorderLayout.CENTER);

		// Project Maintenance
		JTextArea updates = new JTextArea();
		updates.setFont(defaultFont);
		updates.setLineWrap(true);
		updates.setWrapStyleWord(true);
		southPanel.add(updates, BorderLayout.SOUTH);
		updates.setText("Open Source: https://github.com/LTimothy/DoubleEntry");

		contain.add(southPanel, BorderLayout.CENTER);


		// Container Setup
		setTitle("Double Entry");
		//setSize(250, 100);
		setBounds(100, 100, 500, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	public static void appendStatus(String text) {
		status.append(text);
	}

	private boolean supportedExtension(boolean read, String filename) {
		if (read) {
			if (filename.endsWith(".tsv")) {
				return true;
			} else {
				return false;
			}
		} else {
			if (filename.endsWith(".tsv")) {
				return true;
			} else {
				return false;
			}
		}
	}

	private class FileListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			String label = evt.getActionCommand();
			if (label.equals("Open File...")) {
				int validity = fileChooser.showOpenDialog(DoubleEntry.this);
				if (validity == JFileChooser.APPROVE_OPTION && supportedExtension(true, fileChooser.getSelectedFile().getName())) {
					file = fileChooser.getSelectedFile();
					filename = file.getName();
					status.setText("Load Succesful.\n");
				} else {
					status.setText("Load Failure.\n");
				}
			} else {
				int validity = fileChooser.showSaveDialog(DoubleEntry.this);
				if (validity == JFileChooser.APPROVE_OPTION) {
					boolean failed = false;
					try {
						File file = fileChooser.getSelectedFile();
						FileWriter fileW = new FileWriter(file);
						String results = QualtricsDE.saveResult();
						fileW.write(results, 0, results.length());
						fileW.close();
					} catch (IOException e) {
						failed = true;
					}
					if (!failed) {
						status.setText("Save Successful.\n");
					} else {
						status.setText("Save Failure.\n");
					}
				} else {
					status.setText("Save Failure. Is your file in a supported filetype? (.tsv)\n");
				}
			}
		}
	}

	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			String label = evt.getActionCommand();
			if (label.equals("Analyze")) {
				String idText;
				try {
					idText = columnText.getText();
					idColumn = Integer.valueOf(idText);
				} catch (NumberFormatException e) {
					idText = "Invalid Number";
				}

				idPrefix = prefixText.getText();
				status.setText("");
				status.append("Filename: " + filename + "\n");
				status.append("ID Column #: " + idText + "\n");
				status.append("Prefix: " + idPrefix + "\n\n");
				runQualtricsDE();
			} else if (label.equals("Clear")) {
				status.setText("");
				filename = "";
				idColumn = 0;
				idPrefix = "";
				file = null;
			} else if (label.equals("Instructions")) {
				status.setText("----------Basic Instructions----------\n");
				status.append("1. Select File\n");
				status.append("2. Enter the column your participant identifier is in. This is 0-indexed, meaning that if your ID is in column \"A\" in Excel, you should enter: 0\n");
				status.append("3. Enter the prefix your double-entry IDs have. For example, if your ID's are usually 5 digits (#####) and the double entry records are prefixed with \"X_\" (so that we have X_######), please enter: X_\n");
				status.append("4. Select \"Analyze\"\n5. Save (if desired)\n");
				status.append("\n----------Additional Notes----------\n");
				status.append("* The Qualtrics Export Format in .tsv should have variable names on row 1, variable descriptions on row 2-3, and records following those. Data must match this format.\n");
				status.append("* If some columns contain data you do not want compared over records, please remove these columns before processing the file.\n");
				status.append("* Do not alter rows 1-3 containing variable information from Qualtrics.\n");
			}
		}
	}

	public static void main (String args[]) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new DoubleEntry();
			}
		});
	}

	private static void runQualtricsDE() {
		try {
			new QualtricsDE(idColumn, idPrefix, file, "[\\t]", "\t");
		} catch (Exception e) {
			appendStatus("\nFailed to run QualtricsDE.\n");
		}
	}
}