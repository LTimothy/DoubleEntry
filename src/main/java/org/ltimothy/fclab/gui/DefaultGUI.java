package org.ltimothy.fclab.gui;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.ltimothy.fclab.data.QualtricsSurvey;

import javax.inject.Named;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.ltimothy.fclab.dagger.GUIModule.APP_SYSTEM_PLATFORM_NAME;

@Slf4j
public class DefaultGUI extends JFrame {
    private static final String APP_TITLE = "Double Entry Validation Tool";
    private static final Font APP_UNSPACED_FONT = new Font("Serif", Font.PLAIN, 12);
    private static final Font APP_SPACED_FONT = new Font("Monospaced", Font.PLAIN, 12);

    private static final boolean APP_VISIBILITY = true;
    private static final boolean APP_WORD_WRAP_STYLE = true;
    private static final int APP_CLOSE_BEHAVIOR = JFrame.EXIT_ON_CLOSE;
    private static final int APP_X_COORDINATE = 100;
    private static final int APP_Y_COORDINATE = 100;
    private static final int APP_WIDTH = 500;
    private static final int APP_HEIGHT = 500;
    private static JTextArea statusTextArea;

    private Container container;
    private JTextField participantIdColumnInputTextField;
    private JTextField firstRelevantColumnInputTextField;
    private JTextField doubleEntryIdPrefixInputTextField;
    private Optional<QualtricsSurvey> surveyOptional;

    @Getter
    private Optional<File> fileOptional;

    public DefaultGUI(@Named(APP_SYSTEM_PLATFORM_NAME) final Optional<String> systemPlatform) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                statusTextArea = getNonEditableJTextArea();

                this.participantIdColumnInputTextField = getEditableJTextField();
                this.firstRelevantColumnInputTextField = getEditableJTextField();
                this.doubleEntryIdPrefixInputTextField = getEditableJTextField();
                this.fileOptional = Optional.empty();
                this.surveyOptional = Optional.empty();

                this.container = createDefaultContainer(systemPlatform);
                this.container.add(getDefaultConfigurationPanel(), BorderLayout.NORTH);
                this.container.add(getDefaultInformationPanel(), BorderLayout.CENTER);
            });
        } catch (final InterruptedException | InvocationTargetException e) {
            log.error("Exception in initializing the default GUI for {}", systemPlatform, e);
        }
    }

    public static void appendStatusTextArea(@NonNull final String statusUpdate) {
        SwingUtilities.invokeLater(() -> {
            String statusUpdateWithNewline = statusUpdate;
            if (!statusUpdate.endsWith("\n")) {
                statusUpdateWithNewline = statusUpdateWithNewline.concat("\n");
            }
            statusTextArea.append(statusUpdateWithNewline);
            log.info("Appended to status panel {}", statusUpdateWithNewline);
        });
    }

    public static void setStatusTextArea(@NonNull final String statusUpdate) {
        SwingUtilities.invokeLater(() -> {
            statusTextArea.setText(statusUpdate);
            log.info("Rewrote status panel as {}", statusUpdate);
        });
    }

    private Container createDefaultContainer(@NonNull final Optional<String> systemPlatform) {
        log.info("Creating default container");
        final Container container = getContentPane();
        container.setLayout(new BorderLayout());

        setTitle(APP_TITLE);
        setBounds(APP_X_COORDINATE, APP_Y_COORDINATE, APP_WIDTH, APP_HEIGHT);
        setDefaultCloseOperation(APP_CLOSE_BEHAVIOR);
        setVisible(APP_VISIBILITY);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            if (systemPlatform.isPresent() && systemPlatform.get().toLowerCase().startsWith("mac")) {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Double Entry Validation Tool");
            }
        } catch (final Exception e) {
            log.error("Exception in configuring native look and feel for {}", systemPlatform, e);
        }

        return container;
    }

    private Panel getDefaultConfigurationPanel() {
        final Panel configurationPanel = new Panel(new GridLayout(5, 1));
        configurationPanel.setVisible(APP_VISIBILITY);
        configurationPanel.add(getFilePanel());
        configurationPanel.add(createConfigurationInputPanel("Enter Participant ID Column (e.g. AA)",
                participantIdColumnInputTextField));
        configurationPanel.add(createConfigurationInputPanel("Enter First Relevant Column (e.g. F)",
                firstRelevantColumnInputTextField));
        configurationPanel.add(createConfigurationInputPanel("Enter Double-Entry ID Prefix (e.g. X_)",
                doubleEntryIdPrefixInputTextField));
        configurationPanel.add(getDefaultConfigurationButtonPanel());
        return configurationPanel;
    }

    private Panel getDefaultConfigurationEntryPanel() {
        final Panel defaultGridPanel = new Panel(new GridLayout(1, 2));
        defaultGridPanel.setVisible(APP_VISIBILITY);
        return defaultGridPanel;
    }

    private Panel getFilePanel() {
        final Panel filePanel = getDefaultConfigurationEntryPanel();
        filePanel.add(createJLabel("Select File"));
        filePanel.add(createJButton("Open File...", new OpenFileListener()));
        return filePanel;
    }

    private Panel createConfigurationInputPanel(@NonNull final String inputLabel,
                                                @NonNull final JTextField textField) {
        final Panel configurationInputPanel = getDefaultConfigurationEntryPanel();
        configurationInputPanel.add(createJLabel(inputLabel));
        configurationInputPanel.add(textField);
        return configurationInputPanel;
    }

    private Panel getDefaultConfigurationButtonPanel() {
        final Panel defaultConfigurationButtonPanel = new Panel(new GridLayout(1, 3));
        defaultConfigurationButtonPanel.setVisible(APP_VISIBILITY);
        defaultConfigurationButtonPanel.add(getClearStatusButton());
        defaultConfigurationButtonPanel.add(getAnalyzeButton());
        defaultConfigurationButtonPanel.add(getSaveButton());
        return defaultConfigurationButtonPanel;
    }

    private JButton getClearStatusButton() {
        return createJButton("Clear", action -> setStatusTextArea(""));
    }

    private JButton getAnalyzeButton() {
        return createJButton("Analyze", new AnalyzeFileListener());
    }

    private JButton getSaveButton() {
        return createJButton("Save", new SaveFileListener());
    }

    private Optional<Integer> decipherColumn(final String columnText) {
        final StringBuilder temp = new StringBuilder(columnText.trim().toLowerCase());
        int columnNum = 0;

        for (int i = 0; i < columnText.length(); i++) {
            char identifier = temp.charAt(i);
            if (Character.isLetter(identifier)) {
                columnNum += (identifier - 97) + (i * 26);
            } else {
                log.error("Invalid column input {}", columnText);
                appendStatusTextArea("Invalid column input " + columnText);
                return Optional.empty();
            }
        }

        return Optional.of(columnNum);
    }

    private Panel getDefaultInformationPanel() {
        final Panel informationPanel = new Panel(new BorderLayout());
        informationPanel.setVisible(APP_VISIBILITY);
        informationPanel.add(getStatusScrollPane(), BorderLayout.CENTER);
        informationPanel.add(getProjectMaintenanceTextArea(), BorderLayout.SOUTH);
        return informationPanel;
    }

    private JScrollPane getStatusScrollPane() {
        final JScrollPane statusScrollPane = new JScrollPane(statusTextArea);
        statusScrollPane.setVisible(APP_VISIBILITY);
        return statusScrollPane;
    }

    private JTextArea getProjectMaintenanceTextArea() {
        final JTextArea projectMaintenanceTextArea = getNonEditableJTextArea();
        projectMaintenanceTextArea.setText("Open Source: https://github.com/LTimothy/DoubleEntry");
        return projectMaintenanceTextArea;
    }

    private JButton createJButton(@NonNull final String buttonText, @NonNull ActionListener actionListener) {
        final JButton defaultJButton = new JButton(buttonText);
        defaultJButton.setFont(APP_UNSPACED_FONT);
        defaultJButton.addActionListener(actionListener);
        defaultJButton.setVisible(APP_VISIBILITY);
        return defaultJButton;
    }

    private JLabel createJLabel(@NonNull final String labelText) {
        final JLabel defaultJLabel = new JLabel(labelText);
        defaultJLabel.setFont(APP_UNSPACED_FONT);
        defaultJLabel.setVisible(APP_VISIBILITY);
        return defaultJLabel;
    }

    private JTextField getEditableJTextField() {
        final JTextField editableJTextField = new JTextField();
        editableJTextField.setFont(APP_SPACED_FONT);
        editableJTextField.setVisible(APP_VISIBILITY);
        editableJTextField.setEditable(true);
        return editableJTextField;
    }

    private JTextArea getNonEditableJTextArea() {
        final JTextArea nonEditableJTextArea = new JTextArea();
        nonEditableJTextArea.setVisible(APP_VISIBILITY);
        nonEditableJTextArea.setFont(APP_SPACED_FONT);
        nonEditableJTextArea.setWrapStyleWord(APP_WORD_WRAP_STYLE);
        nonEditableJTextArea.setEditable(false);
        return nonEditableJTextArea;
    }

    private class OpenFileListener implements ActionListener {
        @Override
        public void actionPerformed(final ActionEvent ae) {
            final JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File("."));
            fileChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(final File f) {
                    return f.getName().toLowerCase().endsWith(".tsv")
                            || f.getName().toLowerCase().endsWith(".csv")
                            || f.isDirectory();
                }

                @Override
                public String getDescription() {
                    return "Tab-separated values (*.tsv) or Comma-separated values (*.csv)";
                }
            });

            int result = fileChooser.showOpenDialog(DefaultGUI.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                fileOptional = Optional.ofNullable(fileChooser.getSelectedFile());
                if (fileOptional.isPresent()) {
                    final File file = fileOptional.get();
                    log.info("Selected file {}", file);
                    appendStatusTextArea("Selected file " + file.getName());
                }
            }
        }
    }

    private class SaveFileListener implements ActionListener {
        @Override
        public void actionPerformed(final ActionEvent ae) {
            if (surveyOptional.isPresent()) {
                final JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("."));
                fileChooser.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(final File f) {
                        if (f.isDirectory()) {
                            return true;
                        }
                        final String name = f.getName();
                        return name.toLowerCase().endsWith(".csv") || name.toLowerCase().endsWith(".tsv");
                    }

                    @Override
                    public String getDescription() {
                        return "CSV and TSV Files (*.csv, *.tsv)";
                    }
                });

                int result = fileChooser.showSaveDialog(DefaultGUI.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    final File fileToSave = fileChooser.getSelectedFile();
                    final String fileToSaveName = fileToSave.getName();
                    final String fileExtension;

                    final int lastDot = fileToSaveName.lastIndexOf(".");
                    if (lastDot == -1) {
                        log.error("Missing file extension (e.g. *.tsv or *.csv), was provided {}", fileToSaveName);
                        DefaultGUI.appendStatusTextArea("Missing file extension (e.g. *.tsv or *.csv)!");
                        return;
                    } else {
                        fileExtension = fileToSaveName.substring(lastDot + 1);
                    }

                    final char delimiter;
                    if (fileExtension.equalsIgnoreCase("tsv")) {
                        delimiter = '\t';
                    } else if (fileExtension.equalsIgnoreCase("csv")) {
                        delimiter = ',';
                    } else {
                        log.error("Invalid file extension (e.g. *.tsv or *.csv), was provided {}", fileToSaveName);
                        DefaultGUI.appendStatusTextArea("Invalid file extension (e.g. *.tsv or *.csv)!");
                        return;
                    }

                    try {
                        CSVWriter writer = new CSVWriter(new FileWriter(fileToSave), delimiter,
                                ICSVWriter.DEFAULT_QUOTE_CHARACTER, ICSVWriter.DEFAULT_ESCAPE_CHARACTER,
                                ICSVWriter.DEFAULT_LINE_END);
                        surveyOptional.get().getExportData().forEach(writer::writeNext);
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                log.info("Must select and analyze a file before saving!");
                DefaultGUI.appendStatusTextArea("Must select and analyze a file before saving!");
            }
        }
    }

    private class AnalyzeFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent ee) {
            final Optional<String> participantIdColumnOptional =
                    Optional.ofNullable(participantIdColumnInputTextField.getText());
            final Optional<String> firstRelevantColumnOptional =
                    Optional.ofNullable(firstRelevantColumnInputTextField.getText());
            final Optional<String> doubleEntryIdPrefixOptional =
                    Optional.ofNullable(doubleEntryIdPrefixInputTextField.getText());

            final boolean fileSelected = fileOptional.isPresent();
            final boolean participantIdPresentAndNotBlank = participantIdColumnOptional.isPresent() &&
                    !participantIdColumnOptional.get().isBlank();
            final boolean firstRelevantColumnPresentAndNotBlank = firstRelevantColumnOptional.isPresent() &&
                    !firstRelevantColumnOptional.get().isBlank();
            final boolean doubleEntryIdPrefixPresentAndNotBlank = doubleEntryIdPrefixOptional.isPresent() &&
                    !doubleEntryIdPrefixOptional.get().isBlank();

            if (fileSelected && participantIdPresentAndNotBlank && firstRelevantColumnPresentAndNotBlank &&
                    doubleEntryIdPrefixPresentAndNotBlank) {
                final Optional<Integer> participantIdColumnNum = decipherColumn(participantIdColumnOptional.get());
                final Optional<Integer> firstRelevantColumnNum = decipherColumn(firstRelevantColumnOptional.get());

                if (participantIdColumnNum.isPresent() && firstRelevantColumnNum.isPresent()) {
                    try {
                        surveyOptional = Optional.of(new QualtricsSurvey(fileOptional.get(), participantIdColumnNum.get(),
                                firstRelevantColumnNum.get(), doubleEntryIdPrefixOptional.get()));
                    } catch (final IllegalStateException e) {
                        appendStatusTextArea(e.getMessage());
                    }
                }
            } else {
                log.info("One or more of the required inputs were blank: {}, {}, {}, {}", fileOptional,
                        participantIdColumnOptional, firstRelevantColumnOptional, doubleEntryIdPrefixOptional);
                if (!fileSelected) {
                    appendStatusTextArea("Missing File Selection!");
                }
                if (!participantIdPresentAndNotBlank) {
                    appendStatusTextArea("Missing Participant ID!");
                }
                if (!firstRelevantColumnPresentAndNotBlank) {
                    appendStatusTextArea("Missing First Relevant Column!");
                }
                if (!doubleEntryIdPrefixPresentAndNotBlank) {
                    appendStatusTextArea("Missing Double-Entry ID Prefix!");
                }
            }
        }
    }
}
