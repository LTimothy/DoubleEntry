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

import java.util.*;

public class QualtricsDEVL extends DoubleEntryValidationLogic {
    private String idPrefix;
    private StringBuilder results;
    private int idKey;
    private int numSaved;
    private int saveOption;
    private Delimiter delim;
    private Survey s;

    public QualtricsDEVL(Survey s, int saveOption) {
        this.s = s;
        this.saveOption = saveOption;
        this.delim = this.s.getDelimiter();
        this.numSaved = 0;
        this.results = new StringBuilder();
        this.idKey = this.s.getIndex();
        if (idKey > this.s.getHeaderColumnLength()) {
            DoubleEntry.appendStatus("ERROR: The specified ID column is invalid." + delim.getRowSeparator());
        }
        this.idPrefix = this.s.getPrefix().toLowerCase().trim(); // idPrefix.toLowerCase().trim();

        analyzeSurveys();
        completeStatement();
    }

    public String getResult() {
        if (numSaved == 0 || results == null || results.equals("")) {
            DoubleEntry.appendStatus("ERROR: Saved Empty Data. Did you run the program already?" + delim.getRowSeparator());
            return "";
        }

        if (this.saveOption == 1) {
            for (int i = 0; i < this.s.getMapExcludedData().size(); i++) {
                addParticipantInformation(null, this.s.getMapExcludedData().get(i));
            }
        }

        return results.toString();
    }

    private void analyzeSurveys() {
        DoubleEntry.appendStatus("" + delim.getRowSeparator() + "-----------------------------" + delim.getRowSeparator());
        DoubleEntry.appendStatus("Starting Analysis.");
        DoubleEntry.appendStatus("" + delim.getRowSeparator() + "-----------------------------" + delim.getRowSeparator());

        Set<String> uniqueNames = this.s.getUniqueParticipantNameSet();
        Iterator<String> participants = uniqueNames.iterator();
        while (participants.hasNext()) {
            String checking = participants.next();
            String lookingFor = idPrefix + checking;
            if (uniqueNames.contains(lookingFor)) {
            	printOffending(checking, lookingFor);
            } else if (this.saveOption == 1) {
                addParticipantInformation(checking, null);
            }
        }
    }

    private void addParticipantInformation(String surveyId, SurveyData data) {
        SurveyData userId;
        if (data != null) {
            userId = data;
        } else {
            userId = this.s.getParticipantData(surveyId);
        }
        for (int i = 0; i < this.s.getHeaderColumnLength(); i++) {
            results.append(userId.columnData(i) + delim.getSaveSeparator());
        }
        results.append(delim.getRowSeparator());
    }

    private void printOffending(String originalEntry, String doubleEntry) {
        SurveyData first = this.s.getParticipantData(originalEntry);
        SurveyData second = this.s.getParticipantData(doubleEntry);

        int maxReach = Math.max(first.internalLength(), second.internalLength());

        String origPrint = "ID- " + originalEntry + ": ";
        String doubPrint = "ID- " + doubleEntry + ": ";

        boolean savedSomething = false;

        for (int i = 0; i < maxReach || (this.saveOption == 1 && i < this.s.getHeaderColumnLength()); i++) {
            String firstValue = first.columnData(i);
            String secondValue = second.columnData(i);
            boolean foundMismatch = false;
            if (i != idKey && i < maxReach) {
                if (!firstValue.equals(secondValue)) {
                    foundMismatch = true;
                    if (savedSomething == false) {
                        if (numSaved == 0) {
                            if (this.saveOption == 0) {
                                results.append("Original ID" + delim.getSaveSeparator() + "Double Entry ID" + delim.getSaveSeparator() + "Mismatched Column Name" + delim.getSaveSeparator() + "Mismatched Column Index" + delim.getSaveSeparator() + "Original Data" + delim.getSaveSeparator() + "Double Entry Data" + delim.getRowSeparator());
                            }
                        }
                        DoubleEntry.appendStatus("" + delim.getRowSeparator() + "-----------------------------------------------------------" + delim.getRowSeparator());
                        DoubleEntry.appendStatus("ID: " + originalEntry + " MISMATCH WITH " + doubleEntry);
                        DoubleEntry.appendStatus("" + delim.getRowSeparator() + "-----------------------------------------------------------" + delim.getRowSeparator());
                        savedSomething = true;
                    } else {
                        if (this.saveOption == 0) {
                            results.append("" + delim.getRowSeparator());
                        }
                    }
                    DoubleEntry.appendStatus(this.s.getHeaderColumn(i) + " (Col. #: " + i + ") MISMATCH - FOUND:" + delim.getRowSeparator());
                    DoubleEntry.appendStatus(origPrint + firstValue + "" + delim.getRowSeparator());
                    DoubleEntry.appendStatus(doubPrint + secondValue + "" + delim.getRowSeparator());
                    if (this.saveOption == 0) {
                        results.append(originalEntry + delim.getSaveSeparator() + doubleEntry + delim.getSaveSeparator() + this.s.getHeaderColumn(i) + delim.getSaveSeparator() + i + delim.getSaveSeparator() + firstValue + delim.getSaveSeparator() + secondValue);
                    } else if (this.saveOption == 1) {
                        results.append("MISMATCH WITH " + doubleEntry + delim.getSaveSeparator());
                    }
                }
            }
            if (this.saveOption == 1 && !foundMismatch) {
                results.append(firstValue + delim.getSaveSeparator());
            }
        }

        if (savedSomething || this.saveOption == 1) {
            results.append("" + delim.getRowSeparator());
            numSaved++;
        }
    }

    private void completeStatement() {
        DoubleEntry.appendStatus("" + delim.getRowSeparator() + "-----------------------------" + delim.getRowSeparator());
        DoubleEntry.appendStatus("Analysis Complete. No other records found.");
        DoubleEntry.appendStatus("" + delim.getRowSeparator() + "-----------------------------" + delim.getRowSeparator());
    }
}