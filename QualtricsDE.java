/**
 * Qualtrics Double Entry Validation Logic (WIP).
 *
 * @source: https://github.com/LTimothy/DoubleEntry
 * @author: Timothy Lee (https://timothylee.us)
 */

import java.io.*;
import java.util.*;
import java.lang.*;
import java.nio.charset.StandardCharsets;

public class QualtricsDE {
    private static String delimiter;
    private static String headerColumns[];
    private static BufferedReader TSVFile;
    private static List<SurveyData> participantInformation;
    private static List<SurveyData> excludedFromMap;
    private static Map<String, SurveyData> idParticipantMap;
    private static int idKey;
    private static String idPrefix;
    private static int numSaved;
    private static StringBuilder results;
    private static String saveDelimiter;
    private static int saveOption;
    private static File inputFile;

    // Constructor
    public QualtricsDE(int idColumn, String idPrefix, File file, String delimiter, String saveDelimiter, int saveOption) throws Exception {
        this.delimiter = delimiter;
        this.saveDelimiter = saveDelimiter;
        this.saveOption = saveOption;
        numSaved = 0;
        results = new StringBuilder();
        excludedFromMap = new ArrayList<>();
        this.inputFile = file;

        try {
            TSVFile = new BufferedReader(new FileReader(file, StandardCharsets.UTF_16));
        } catch (Exception e) {
            DoubleEntry.appendStatus("ERROR: No file found. Does " + file.getName() + " exist?\n");
        }

        initializeHeader();

        idKey = idColumn;
        if (idKey > headerColumns.length) {
            DoubleEntry.appendStatus("ERROR: The specified ID column is invalid.\n");
        }
        this.idPrefix = idPrefix.toLowerCase().trim();

        skipLines(2);
        loadSurveys();
        analyzeSurveys();
        completeStatement();

        if (TSVFile != null) {
            TSVFile.close();
        }
    }

    public static String saveResult() {
        if (numSaved == 0 || results == null || results.equals("")) {
            DoubleEntry.appendStatus("ERROR: Saved Empty Data. Did you run the program already?\n");
            return "";
        }

        if (saveOption == 1) {
            for (int i = 0; i < excludedFromMap.size(); i++) {
                addParticipantInformation(null, excludedFromMap.get(i));
            }
        }
        
        return results.toString();
    }

    private static void analyzeSurveys() {
        idParticipantMap = new HashMap<>();
        boolean printDuplicate = false;
        for (int i = 0; i < participantInformation.size(); i++) {
            SurveyData data = participantInformation.get(i);
            String id = data.participantIdentifier();
            if (!printDuplicate && idParticipantMap.containsKey(id) && id != "MISSING ID") {
                printDuplicate = true;
            }
            if (saveOption == 1 && idParticipantMap.containsKey(id)) {
                excludedFromMap.add(idParticipantMap.get(id));
            }
            idParticipantMap.put(id, data);
        }

        if (printDuplicate) {
            DoubleEntry.appendStatus("NOTICE: Duplicate IDs detected. Non-deterministic behavior may occur.\n");
        }

        Set<String> participantPool = idParticipantMap.keySet();
        if (participantPool.contains("MISSING ID")) {
        	DoubleEntry.appendStatus("NOTICE: Some records may have missing ids! Non-deterministic behavior may occur on entries with missing id.\n");
        }

        DoubleEntry.appendStatus("\n------------------------------\n");
        DoubleEntry.appendStatus("Starting Analysis.");
        DoubleEntry.appendStatus("\n------------------------------\n");

        Iterator<String> participants = participantPool.iterator();
        while (participants.hasNext()) {
            String checking = participants.next();
            String lookingFor = idPrefix + checking;
            if (participantPool.contains(lookingFor)) {
            	printOffending(checking, lookingFor);
            } else {
                addParticipantInformation(checking, null);
            }
        }
    }

    private static void addParticipantInformation(String surveyId, SurveyData data) {
        SurveyData userId;
        if (data != null) {
            userId = data;
        } else {
            userId = idParticipantMap.get(surveyId);
        }
        for (int i = 0; i < headerColumns.length; i++) {
            results.append(userId.columnData(i) + saveDelimiter);
        }
        results.append("\n");
    }

    private static void printOffending(String originalEntry, String doubleEntry) {
        SurveyData first = idParticipantMap.get(originalEntry);
        SurveyData second = idParticipantMap.get(doubleEntry);

        int maxReach = Math.max(first.internalLength(), second.internalLength());

        String origPrint = "ID- " + originalEntry + ": ";
        String doubPrint = "ID- " + doubleEntry + ": ";

        boolean savedSomething = false;

        for (int i = 0; i < maxReach || (saveOption == 1 && i < headerColumns.length); i++) {
            String firstValue = first.columnData(i);
            String secondValue = second.columnData(i);
            boolean foundMismatch = false;
            if (i != idKey && i < maxReach) {
                if (!firstValue.equals(secondValue)) {
                    foundMismatch = true;
                    if (savedSomething == false) {
                        if (numSaved == 0) {
                            if (saveOption == 0) {
                                results.append("Original ID" + saveDelimiter + "Double Entry ID" + saveDelimiter + "Mismatched Column Name" + saveDelimiter + "Mismatched Column Index" + saveDelimiter + "Original Data" + saveDelimiter + "Double Entry Data\n");
                            }
                        }
                        DoubleEntry.appendStatus("\n------------------------------------------------------------\n");
                        DoubleEntry.appendStatus("ID: " + originalEntry + " MISMATCH WITH " + doubleEntry);
                        DoubleEntry.appendStatus("\n------------------------------------------------------------\n");
                        savedSomething = true;
                    } else {
                        if (saveOption == 0) {
                            results.append("\n");
                        }
                    }
                    DoubleEntry.appendStatus(headerColumns[i] + " (Col. #: " + i + ") MISMATCH - FOUND:\n");
                    DoubleEntry.appendStatus(origPrint + firstValue + "\n");
                    DoubleEntry.appendStatus(doubPrint + secondValue + "\n");
                    if (saveOption == 0) {
                        results.append(originalEntry + saveDelimiter + doubleEntry + saveDelimiter + headerColumns[i] + saveDelimiter + i + saveDelimiter + firstValue + saveDelimiter + secondValue);
                    } else if (saveOption == 1) {
                        results.append("MISMATCH WITH " + doubleEntry + saveDelimiter);
                    }
                }
            }
            if (saveOption == 1 && !foundMismatch) {
                results.append(firstValue + saveDelimiter);
            }
        }

        if (savedSomething || saveOption == 1) {
            results.append("\n");
            numSaved++;
        }
    }

    private static void completeStatement() {
        DoubleEntry.appendStatus("\n------------------------------\n");
        DoubleEntry.appendStatus("Analysis Complete. No other records found.");
        DoubleEntry.appendStatus("\n------------------------------\n");
    }

    private static void loadSurveys() {
    	participantInformation = new ArrayList<>();
    	String loadData;

    	try {
	    	while ((loadData = TSVFile.readLine()) != null) {
	    		participantInformation.add(new SurveyData(headerColumns, loadData, delimiter, idKey));
	    	}
	    } catch (IOException e) {
	    	DoubleEntry.appendStatus("DEBUG: Unable to load surveys.\n");
	    }
    }

    private static String newlineTerminator(String line) {
        if (!line.endsWith("\n")) {
            line += "\n";
        }
        return line;
    }

    private static void initializeHeader() {
    	try {
            String  line = TSVFile.readLine();
    		headerColumns = line.split(delimiter);
            if (saveOption == 1) {
                results.append(newlineTerminator(line));
            }
	    } catch (IOException e) {
	    	DoubleEntry.appendStatus("DEBUG: Attempted to initialize header but not possible.\n");
	    }
    }

    private static void skipLines(int lines) {
    	try {
	    	while (lines > 0) {
                if (saveOption == 1) {
                    results.append(newlineTerminator(TSVFile.readLine()));
                } else {
                    TSVFile.readLine();
                }
	    		lines--;
	    	}
	    } catch (IOException e) {
	    	DoubleEntry.appendStatus("DEBUG: Attempted to skip more lines than file has remaining.\n");
	    }
    }
}