package org.ltimothy.fclab.data;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;
import com.opencsv.exceptions.CsvValidationException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.ltimothy.fclab.gui.DefaultGUI;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
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
    private static final String MISMATCH_PREFIX = "!= ";

    private final int participantIdColumn;
    private final int firstRelevantColumn;
    private final String doubleEntryIdPrefix;
    private final Map<String, String[]> participantIdToRawData;
    private final Charset charset;

    private List<String[]> rawDataHeaders;
    private List<String[]> processedData;

    public QualtricsSurvey(@NonNull final File file, int participantIdColumn, int firstRelevantColumn,
                           @NonNull final String doubleEntryIdPrefix, @NonNull Charset charset) {
        this.participantIdToRawData = new HashMap<>();
        this.rawDataHeaders = new ArrayList<>();
        this.processedData = new ArrayList<>();
        this.participantIdColumn = participantIdColumn;
        this.firstRelevantColumn = firstRelevantColumn;
        this.doubleEntryIdPrefix = doubleEntryIdPrefix.toLowerCase();
        this.charset = charset;
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
                        final int rawDataLength = rawDataHeaders.get(QUALTRICS_PRIMARY_HEADER_INDEX).length;
                        final String[] originalEntryData =
                                Arrays.copyOf(participantIdToRawData.get(participantId), rawDataLength);
                        final String[] doubleEntryData =
                                Arrays.copyOf(participantIdToRawData.get(doubleEntryParticipantId), rawDataLength);

                        printAnalysisHeader(participantId);
                        for (int i = firstRelevantColumn; i < originalEntryData.length; i++) {
                            if (i == participantIdColumn) {
                                continue;
                            }
                            final String originalValue = originalEntryData[i];
                            final String doubleEntryValue = doubleEntryData[i];
                            if (!originalValue.trim().equalsIgnoreCase(doubleEntryValue.trim())) {
                                printMismatch(rawDataHeaders.get(QUALTRICS_PRIMARY_HEADER_INDEX)[i], originalValue,
                                        doubleEntryValue);
                                doubleEntryData[i] = MISMATCH_PREFIX.concat(doubleEntryData[i]);
                            } else {
                                doubleEntryData[i] = NO_MISMATCH_TEXT;
                            }
                        }

                        processedData.add(originalEntryData);
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

        final char delimiter;
        if (fileExtension.equals("tsv")) {
            delimiter = '\t';
        } else if (fileExtension.equals("csv")) {
            delimiter = ',';
        } else {
            log.info("The file selected was of an unsupported file type {}", file);
            return;
        }

        try (final CSVReader reader = new CSVReaderBuilder(new FileReader(filePath, charset))
                .withCSVParser(new CSVParserBuilder()
                        .withQuoteChar(ICSVParser.DEFAULT_QUOTE_CHARACTER)
                        .withSeparator(delimiter)
                        .build())
                .build()) {
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                if (headersRemaining > 0) {
                    rawDataHeaders.add(nextLine);
                    headersRemaining--;
                    continue;
                }
                processNonHeaderLine(nextLine);
            }
        } catch (final IOException | CsvValidationException | IndexOutOfBoundsException e) {
            log.error("Exception in processing the file {}", file, e);
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
            DefaultGUI.appendStatusTextArea("A row with a blank participant id was removed!");
        }
    }
}
