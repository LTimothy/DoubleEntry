/**
 * Qualtrics Double Entry Validation Logic (WIP).
 *
 * Copyright (C) 2019 Timothy Lee - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the GNU General Public License v3.0.
 *
 * You should have received a copy of the GNU General
 * Public License v3.0 with this file. If not, please
 * contact: timothyl@berkeley.edu, or visit:
 * https://github.com/LTimothy/DoubleEntry
 *
 * @author: Timothy Lee (https://timothylee.us)
 */

import java.io.*;
import java.util.*;
import java.lang.*;
import java.nio.charset.StandardCharsets;

public class QualtricsDEVL extends DoubleEntryValidationLogic {
    private String headerColumns[];
    private BufferedReader TSVFile;
    private List<SurveyData> participantInformation;
    private List<SurveyData> excludedFromMap;
    private Map<String, SurveyData> idParticipantMap;
    private String idPrefix;
    private int numSaved;
    private StringBuilder results;
    private int saveOption;
    private Delimiter delim;

    public QualtricsDEVL(int idColumn, String idPrefix, File file, Delimiter delim, int saveOption) throws Exception {
        this.saveOption = saveOption;
        this.delim = delim;
        this.numSaved = 0;
        this.results = new StringBuilder();
        this.excludedFromMap = new ArrayList<>();
        this.inputFile = file;

        try {
            TSVFile = new BufferedReader(new FileReader(file, StandardCharsets.UTF_16));
        } catch (Exception e) {
            DoubleEntry.appendStatus("ERROR: No file found. Does " + file.getName() + " exist?" + delim.getRowDelimiter());
        }

        initializeHeader();

        this.idKey = idColumn;
        if (idKey > headerColumns.length) {
            DoubleEntry.appendStatus("ERROR: The specified ID column is invalid." + delim.getRowDelimiter());
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

    public String getResult() {
        if (numSaved == 0 || results == null || results.equals("")) {
            DoubleEntry.appendStatus("ERROR: Saved Empty Data. Did you run the program already?" + delim.getRowDelimiter());
            return "";
        }

        if (saveOption == 1) {
            for (int i = 0; i < excludedFromMap.size(); i++) {
                addParticipantInformation(null, excludedFromMap.get(i));
            }
        }

        return results.toString();
    }

    private void analyzeSurveys() {
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
            DoubleEntry.appendStatus("NOTICE: Duplicate IDs detected. Non-deterministic behavior may occur." + delim.getRowDelimiter());
        }

        Set<String> participantPool = idParticipantMap.keySet();
        if (participantPool.contains("MISSING ID")) {
        	DoubleEntry.appendStatus("NOTICE: Some records may have missing ids! Non-deterministic behavior may occur on entries with missing id." + delim.getRowDelimiter());
        }

        DoubleEntry.appendStatus("" + delim.getRowDelimiter() + "-----------------------------" + delim.getRowDelimiter());
        DoubleEntry.appendStatus("Starting Analysis.");
        DoubleEntry.appendStatus("" + delim.getRowDelimiter() + "-----------------------------" + delim.getRowDelimiter());

        Iterator<String> participants = participantPool.iterator();
        while (participants.hasNext()) {
            String checking = participants.next();
            String lookingFor = idPrefix + checking;
            if (participantPool.contains(lookingFor)) {
            	printOffending(checking, lookingFor);
            } else if (saveOption == 1) {
                addParticipantInformation(checking, null);
            }
        }
    }

    private void addParticipantInformation(String surveyId, SurveyData data) {
        SurveyData userId;
        if (data != null) {
            userId = data;
        } else {
            userId = idParticipantMap.get(surveyId);
        }
        for (int i = 0; i < headerColumns.length; i++) {
            results.append(userId.columnData(i) + delim.getSaveDelimiter());
        }
        results.append("" + delim.getRowDelimiter());
    }

    private void printOffending(String originalEntry, String doubleEntry) {
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
                                results.append("Original ID" + delim.getSaveDelimiter() + "Double Entry ID" + delim.getSaveDelimiter() + "Mismatched Column Name" + delim.getSaveDelimiter() + "Mismatched Column Index" + delim.getSaveDelimiter() + "Original Data" + delim.getSaveDelimiter() + "Double Entry Data" + delim.getRowDelimiter());
                            }
                        }
                        DoubleEntry.appendStatus("" + delim.getRowDelimiter() + "-----------------------------------------------------------" + delim.getRowDelimiter());
                        DoubleEntry.appendStatus("ID: " + originalEntry + " MISMATCH WITH " + doubleEntry);
                        DoubleEntry.appendStatus("" + delim.getRowDelimiter() + "-----------------------------------------------------------" + delim.getRowDelimiter());
                        savedSomething = true;
                    } else {
                        if (saveOption == 0) {
                            results.append("" + delim.getRowDelimiter());
                        }
                    }
                    DoubleEntry.appendStatus(headerColumns[i] + " (Col. #: " + i + ") MISMATCH - FOUND:" + delim.getRowDelimiter());
                    DoubleEntry.appendStatus(origPrint + firstValue + "" + delim.getRowDelimiter());
                    DoubleEntry.appendStatus(doubPrint + secondValue + "" + delim.getRowDelimiter());
                    if (saveOption == 0) {
                        results.append(originalEntry + delim.getSaveDelimiter() + doubleEntry + delim.getSaveDelimiter() + headerColumns[i] + delim.getSaveDelimiter() + i + delim.getSaveDelimiter() + firstValue + delim.getSaveDelimiter() + secondValue);
                    } else if (saveOption == 1) {
                        results.append("MISMATCH WITH " + doubleEntry + delim.getSaveDelimiter());
                    }
                }
            }
            if (saveOption == 1 && !foundMismatch) {
                results.append(firstValue + delim.getSaveDelimiter());
            }
        }

        if (savedSomething || saveOption == 1) {
            results.append("" + delim.getRowDelimiter());
            numSaved++;
        }
    }

    private void completeStatement() {
        DoubleEntry.appendStatus("" + delim.getRowDelimiter() + "-----------------------------" + delim.getRowDelimiter());
        DoubleEntry.appendStatus("Analysis Complete. No other records found.");
        DoubleEntry.appendStatus("" + delim.getRowDelimiter() + "-----------------------------" + delim.getRowDelimiter());
    }

    private void loadSurveys() {
    	participantInformation = new ArrayList<>();
    	String loadData;

    	try {
	    	while ((loadData = TSVFile.readLine()) != null) {
	    		participantInformation.add(new SurveyData(headerColumns, loadData, delim.getDelimiter(), idKey));
	    	}
	    } catch (IOException e) {
	    	DoubleEntry.appendStatus("DEBUG: Unable to load surveys." + delim.getRowDelimiter());
	    }
    }

    private String newlineTerminator(String line) {
        if (!line.endsWith("" + delim.getRowDelimiter())) {
            line += "" + delim.getRowDelimiter();
        }
        return line;
    }

    private void initializeHeader() {
    	try {
            String  line = TSVFile.readLine();
    		headerColumns = line.split(delim.getDelimiter());
            if (saveOption == 1) {
                results.delete(0, results.length());
                results.append(newlineTerminator(line));
            }
	    } catch (IOException e) {
	    	DoubleEntry.appendStatus("DEBUG: Attempted to initialize header but not possible." + delim.getRowDelimiter());
	    }
    }

    private void skipLines(int lines) {
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
	    	DoubleEntry.appendStatus("DEBUG: Attempted to skip more lines than file has remaining." + delim.getRowDelimiter());
	    }
    }
}