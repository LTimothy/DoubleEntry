/**
 * Double Entry Validation Tool (WIP).
 *
 * Copyright (C) 2019 Timothy Lee
 *
 * @source: <https://github.com/LTimothy/DoubleEntry>
 * @author: Timothy Lee <https://timothylee.us>
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class DoubleEntry extends JFrame
{
	// GUI Elements
	private static JTextArea status;
	private static JCheckBox saveFullExport;
	private JTextField columnText;
	private JTextField usefulText;
	private JTextField prefixText;
	private JFileChooser fileChooser;
	private JButton submitBtn;
	private Font defaultFont;

	// Data Fields
	private static String filename;
	private static int indexColumn;
	private static int relevantColumn;
	private static String doublePrefix;
	private static File file;
	private static DoubleEntryValidationLogic logic;
	private static Survey s;

	// Control Logic
	private String osName;

	public DoubleEntry() {
		Container contain = getContentPane();
		contain.setLayout(new BorderLayout());
		defaultFont = new Font("Serif", Font.PLAIN, 12);
		ButtonListener listener = new ButtonListener();

		// NORTH PANEL
		Panel northPanel = new Panel(new GridLayout(6, 1));
		Panel filenamePanel = new Panel(new GridLayout(1, 2));
		Panel columnPanel = new Panel(new GridLayout(1, 2));
		Panel usefulPanel = new Panel(new GridLayout(1, 2));
		Panel prefixPanel = new Panel(new GridLayout(1, 2));

		// Filename Loading
		JLabel filenameLabel = new JLabel("Select File");
		filenameLabel.setFont(defaultFont);
		filenamePanel.add(filenameLabel);
		fileChooser = new JFileChooser();
		JButton openButton = new JButton("Open File...");
		openButton.setFont(defaultFont);
		openButton.addActionListener(new FileListener());
		filenamePanel.add(openButton);

		// Column loading
		JLabel columnLabel = new JLabel("Enter Participant ID Column (e.g. AA)");
		columnLabel.setFont(defaultFont);
		columnPanel.add(columnLabel);
		columnText = new JTextField(20);
		columnText.setFont(defaultFont);
		columnText.setEditable(true);
		columnPanel.add(columnText);

		// Useful Columns
		JLabel usefulLabel = new JLabel("Enter First Relevant Column (e.g. F)");
		usefulLabel.setFont(defaultFont);
		usefulPanel.add(usefulLabel);
		usefulText = new JTextField(20);
		usefulText.setFont(defaultFont);
		usefulText.setEditable(true);
		usefulPanel.add(usefulText);

		// Prefix loading
		JLabel prefixLabel = new JLabel("Enter Double-Entry ID Prefix (e.g. X_)");
		prefixLabel.setFont(defaultFont);
		prefixPanel.add(prefixLabel);
		prefixText = new JTextField(20);
		prefixText.setFont(defaultFont);
		prefixText.setEditable(true);
		prefixPanel.add(prefixText);

		northPanel.add(filenamePanel);
		northPanel.add(columnPanel);
		northPanel.add(usefulPanel);
		northPanel.add(prefixPanel);

		// Checkbox Panel
		Panel checkboxPanel = new Panel(new GridLayout(1, 2));
		saveFullExport = new JCheckBox("Export as Full Data");
		saveFullExport.setFont(defaultFont);
		checkboxPanel.add(saveFullExport);
		northPanel.add(checkboxPanel);

		// Instructions/Analyze/Clear Buttons
		Panel buttonPanel = new Panel(new GridLayout(1, 4));
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
		status.setEditable(false);
		JScrollPane statusLog = new JScrollPane(status);
		southPanel.add(statusLog, BorderLayout.CENTER);

		// Project Maintenance
		JTextArea updates = new JTextArea();
		updates.setFont(defaultFont);
		updates.setLineWrap(true);
		updates.setWrapStyleWord(true);
		southPanel.add(updates, BorderLayout.SOUTH);
		updates.setText("Open Source: https://github.com/LTimothy/DoubleEntry");
		updates.setEditable(false);

		contain.add(southPanel, BorderLayout.CENTER);


		// Container Setup
		setTitle("Double Entry Validaton Tool");
		setBounds(100, 100, 500, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		// Native Look and Feel Setup (https://alvinalexander.com/apple/mac/java-mac-native-look/)
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			osName = System.getProperty("os.name").toLowerCase();
			boolean IS_MAC = osName.startsWith("mac os x");
			if (IS_MAC) {
				System.setProperty("apple.laf.useScreenMenuBar", "true");
				System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Double Entry Validation Tool");
			}
		} catch (Exception e) {
			System.out.println("UIManager.setLookAndFeel() failed.");
			osName = "";
		}
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
					status.setText("NOTICE: Load Succesful.\n");
				} else {
					status.setText("NOTICE: Load Failure.\n");
				}
			} else {
				int validity = fileChooser.showSaveDialog(DoubleEntry.this);
				if (validity == JFileChooser.APPROVE_OPTION) {
					boolean failed = false;
					try {
						File file = fileChooser.getSelectedFile();
						FileWriter fileW = new FileWriter(file, StandardCharsets.UTF_16);
						String surveyResult = "";
						if (saveFullExport.isSelected()) {
							surveyResult = s.getResult();
						}
						String results = logic.getResult();
						fileW.write(surveyResult + results, 0, results.length() + surveyResult.length());
						fileW.close();
					} catch (IOException e) {
						failed = true;
					}
					status.setText("NOTICE: Make sure to \"Analyze\" before saving!\n");
					if (saveFullExport.isSelected()) {
						status.append("NOTICE: For full export, be sure to run \"Analyze\" again after checking the box!\n");
					}
					if (!failed) {
						status.append("NOTICE: Save Successful.\n");
					} else {
						status.append("NOTICE: Save Failure.\n");
					}
				} else {
					status.setText("ERROR: Save Failure. Is your file in a supported filetype? (.tsv)\n");
				}
			}
		}
	}

	private int decipherColumn(String text) {
		text = text.trim().toLowerCase();
		StringBuilder temp = new StringBuilder(text);
		int columnNum = 0;

		for (int i = 0; i < text.length(); i++) {
			char identifier = temp.charAt(i);
			if (Character.isLetter(identifier)) {
				columnNum += (identifier - 97) + (i * 26);
			}
		}

		return columnNum;
	}

	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			String label = evt.getActionCommand();
			if (label.equals("Analyze")) {
				String idText;
				try {
					idText = columnText.getText();
					if (Character.isLetter(idText.charAt(0))) {
						indexColumn = decipherColumn(idText);
					} else {
						indexColumn = Integer.valueOf(idText);
					}
				} catch (NumberFormatException | StringIndexOutOfBoundsException e) {
					idText = "Invalid Column";
				}

				String firstRelevant;
				try {
					firstRelevant = usefulText.getText();
					if (Character.isLetter(firstRelevant.charAt(0))) {
						relevantColumn = decipherColumn(firstRelevant);
					} else {
						relevantColumn = Integer.valueOf(firstRelevant);
					}
				} catch (NumberFormatException | StringIndexOutOfBoundsException e) {
					firstRelevant = "Invalid First Relevant Column";
				}

				doublePrefix = prefixText.getText();
				status.setText("");
				status.append("Program Platform: " + osName + "\n");
				status.append("Filename: " + filename + "\n");
				status.append("Participant ID Column: " + idText + "\n");
				status.append("Participant ID Column #: " + indexColumn + "\n");
				status.append("First Relevant Data Column: " + firstRelevant + "\n");
				status.append("First Relevant Data Column #: " + relevantColumn + "\n");
				status.append("Double-Entry Participant ID Prefix: " + doublePrefix + "\n\n");
				runQualtricsDEVL();
			} else if (label.equals("Clear")) {
				status.setText("");
				filename = "";
				indexColumn = 0;
				doublePrefix = "";
				file = null;
			} else if (label.equals("Instructions")) {
				status.setText("----------Basic Instructions----------\n");
				status.append("1. Select File\n");
				status.append("2. Enter the column your participant identifier is in. This is 0-indexed, meaning that if your ID is in column \"A\" in Excel, you should enter: 0\n");
				status.append("3. Enter the prefix your double-entry IDs have. For example, if your ID's are usually 5 digits (#####) and the double entry records are prefixed with \"X_\" (so that we have X_#####), please enter: X_\n");
				status.append("4. Select \"Analyze\"\n5. Save (if desired)\n");
				status.append("\n----------Additional Notes----------\n");
				status.append("* The Qualtrics Export Format in .tsv should have variable names on row 1, variable descriptions on row 2-3, and records following those. Data must match this format.\n");
				status.append("* If some columns contain data you do not want compared over records, please remove these columns before processing the file.\n");
				status.append("* Do not alter rows 1-3 containing variable information from Qualtrics.\n");
				status.append("* There is no guarantee of ordering of data in the save file.\n");
				status.append("* Export as full file requires the user to press \"Analyze\" and then \"Save\" after checking the box.\n");
				status.append("* Export as full file will replace mismatched locations in the output with \"MISMATCH WITH [prefixed-ID]\"\n");
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

	private void runQualtricsDEVL() {
		try {
			int saveOption = 0;
			if (saveFullExport.isSelected()) {
				saveOption = 1;
			}
			s = new QualtricsSurvey(file, new Delimiter("[\\t]", "\t", "\n"), indexColumn, doublePrefix);
			logic = new QualtricsDEVL(s, saveOption);
		} catch (Exception e) {
			appendStatus("\nFailed to run QualtricsDEVL.\n");
			System.out.println("runQualtricsDEVL(): " + e);
		}
	}
}