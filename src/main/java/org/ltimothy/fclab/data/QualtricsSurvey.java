package org.ltimothy.fclab.data;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.ltimothy.fclab.gui.DefaultGUI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class QualtricsSurvey {
    private static final int QUALTRICS_PRIMARY_HEADER_INDEX = 0;
    private static final int QUALTRICS_HEADERS_TOTAL_LENGTH = 3;
    private static final String NO_MISMATCH_TEXT = "OK";

    private final int participantIdColumn;
    private final int firstRelevantColumn;
    private final String doubleEntryIdPrefix;
    private final Map<String, String[]> participantIdToRawData;

    private List<String[]> rawDataHeaders;
    private List<String[]> processedData;

    public QualtricsSurvey(@NonNull final File file, int participantIdColumn, int firstRelevantColumn,
                           @NonNull final String doubleEntryIdPrefix) {
        this.participantIdToRawData = new HashMap<>();
        this.rawDataHeaders = new ArrayList<>();
        this.processedData = new ArrayList<>();
        this.participantIdColumn = participantIdColumn;
        this.firstRelevantColumn = firstRelevantColumn;
        this.doubleEntryIdPrefix = doubleEntryIdPrefix.toLowerCase();
        processFile(file);
    }

    public List<String[]> getExportData() {
        final List<String[]> exportData = new ArrayList<>();
        exportData.addAll(rawDataHeaders);
        exportData.addAll(processedData);
        return exportData;
    }

    private void processFile(@NonNull File file) throws IllegalStateException {
        loadFile(file);

        try {
            for (final String participantId : participantIdToRawData.keySet()) {
                final String doubleEntryParticipantId = doubleEntryIdPrefix.concat(participantId);
                final String originalEntryParticipantId = participantId.substring(doubleEntryIdPrefix.length());
                if (!participantId.startsWith(doubleEntryIdPrefix)) {
                    if (participantIdToRawData.containsKey(doubleEntryParticipantId)){
                        final String[] originalEntryRawData = participantIdToRawData.get(participantId);
                        final String[] doubleEntryData =
                                Arrays.copyOf(participantIdToRawData.get(doubleEntryParticipantId),
                                        originalEntryRawData.length);

                        printAnalysisHeader(participantId);
                        for (int i = firstRelevantColumn; i < originalEntryRawData.length; i++) {
                            final String originalValue = originalEntryRawData[i];
                            final String doubleEntryValue = doubleEntryData[i];
                            if (!originalValue.trim().equalsIgnoreCase(doubleEntryValue.trim())) {
                                printMismatch(rawDataHeaders.get(QUALTRICS_PRIMARY_HEADER_INDEX)[i], originalValue,
                                        doubleEntryValue);
                            } else {
                                doubleEntryData[i] = NO_MISMATCH_TEXT;
                            }
                        }

                        processedData.add(originalEntryRawData);
                        processedData.add(doubleEntryData);
                    } else {
                        processedData.add(participantIdToRawData.get(participantId));
                    }
                } else if (participantId.startsWith(doubleEntryIdPrefix) &&
                        !participantIdToRawData.containsKey(originalEntryParticipantId)){
                    printAnalysisHeader(participantId);
                    log.warn("There was no entry {} for double-entry {}", originalEntryParticipantId, participantId);
                    DefaultGUI.appendStatusTextArea("[Warning] There was no entry " +
                            originalEntryParticipantId + " for double-entry " + participantId);
                    processedData.add(participantIdToRawData.get(participantId));
                }
            }
        } catch (final IndexOutOfBoundsException e) {
            log.error("Column identifiers are invalid!");
            throw new IllegalStateException("Column identifiers are invalid!");
        }
    }

    private void printAnalysisHeader(@NonNull final String participantId) {
        int boxWidth = 50;
        int padding = (boxWidth - participantId.length()) / 2;
        boolean isOdd = participantId.length() % 2 == 1;

        DefaultGUI.appendStatusTextArea("+" + "-".repeat(boxWidth - 2) + "+");
        DefaultGUI.appendStatusTextArea("|" + " ".repeat(boxWidth - 2) + "|");
        DefaultGUI.appendStatusTextArea(String.format("|%" + padding + "s%s%" + (isOdd ? padding - 1 : padding) +
                "s|\n", "", participantId, ""));
        DefaultGUI.appendStatusTextArea("|" + " ".repeat(boxWidth - 2) + "|");
        DefaultGUI.appendStatusTextArea("+" + "-".repeat(boxWidth - 2) + "+");
    }

    private void printMismatch(@NonNull final String columnHeader, @NonNull final String originalValue,
                               @NonNull final String doubleEntryValue) {
        DefaultGUI.appendStatusTextArea("[Mismatch] " + columnHeader + " originally was \"" +
                originalValue.trim() + "\" but was \"" + doubleEntryValue.trim() + "\" in the double entry.");
    }

    private void loadFile(@NonNull File file) throws IllegalStateException {
        final String filePath = file.getPath();
        final String fileExtension = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();
        int headersRemaining = QUALTRICS_HEADERS_TOTAL_LENGTH;

        if (fileExtension.equals("tsv")) {
            try (final BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    final String[] fields = line.split("\t", -1);
                    if (headersRemaining > 0) {
                        rawDataHeaders.add(fields);
                        headersRemaining--;
                        continue;
                    }
                    processNonHeaderLine(fields);
                }
            } catch (final IOException | IndexOutOfBoundsException e) {
                log.error("Exception in processing the *.tsv file {}", file, e);
            }
        } else if (fileExtension.equals("csv")) {
            int i = 0;
            try (final CSVReader reader = new CSVReader(new FileReader(filePath))) {
                String[] nextLine;
                while ((nextLine = reader.readNext()) != null) {
                    i++;
                    if (headersRemaining > 0) {
                        rawDataHeaders.add(nextLine);
                        headersRemaining--;
                        continue;
                    }
                    processNonHeaderLine(nextLine);
                }
            } catch (final IOException | CsvValidationException | IndexOutOfBoundsException e) {
                log.error("Exception in processing the *.csv file {}", file, e);
            }
        } else {
            log.info("The file selected was of an unsupported file type {}", file);
        }
    }

    private void processNonHeaderLine(@NonNull String[] fields) throws IndexOutOfBoundsException,
            IllegalStateException {
        final String participantId = fields[participantIdColumn];
        final String participantIdLower = fields[participantIdColumn].toLowerCase();
        if (participantIdToRawData.containsKey(participantIdLower)) {
            log.error("Participant ID must be unique! At least one duplicate with ID {} was found.", participantId);
            throw new IllegalStateException("Participant ID must be unique! At least one duplicate with ID" +
                    participantId + " was found.");
        } else if (!participantIdLower.isBlank()){
            participantIdToRawData.put(participantIdLower, fields);
        } else {
            log.info("Blank participant ID was removed for row with fields {}", Arrays.toString(fields));
            DefaultGUI.appendStatusTextArea("Rows with blank participant ids were removed!");
        }
    }
}
