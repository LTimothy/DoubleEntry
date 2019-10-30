/**
 * Qualtrics Double Entry Validation Logic (WIP).
 *
 * Copyright (C) 2019 Timothy Lee
 *
 * @source: <https://github.com/LTimothy/DoubleEntry>
 * @author: Timothy Lee <https://timothylee.us>
 */

import java.util.*;

public class QualtricsDEVL extends DoubleEntryValidationLogic {
    private String idPrefix;
    private StringBuilder results;
    private int idKey;
    private int numSaved;
    private int saveOption;
    private int firstRelevant;
    private Delimiter delim;
    private Survey s;

    public QualtricsDEVL(Survey s, int saveOption, int firstRelevant) {
        this.s = s;
        this.saveOption = saveOption;
        this.delim = this.s.getDelimiter();
        this.numSaved = 0;
        this.firstRelevant = firstRelevant;
        this.results = new StringBuilder();
        this.idKey = this.s.getIndex();
        if (this.idKey > this.s.getHeaderColumnLength()) {
            DoubleEntry.appendStatus("ERROR: The specified ID column is invalid.\n");
        }
        this.idPrefix = this.s.getPrefix();

        analyzeSurveys();
        completeStatement();
    }

    public String getResult() {
        if (this.numSaved == 0 || this.results == null || this.results.equals("")) {
            DoubleEntry.appendStatus("ERROR: Saved Empty Data. Did you run the program already?\n");
            return "";
        }

        if (this.saveOption == 1) {
            for (int i = 0; i < this.s.getMapExcludedData().size(); i++) {
                addParticipantInformation(null, this.s.getMapExcludedData().get(i));
            }
        }

        return this.results.toString();
    }

    private void analyzeSurveys() {
        DoubleEntry.appendStatus("\n-----------------------------\n");
        DoubleEntry.appendStatus("Starting Analysis.");
        DoubleEntry.appendStatus("\n-----------------------------\n");

        Set<String> uniqueNames = new TreeSet<String>(this.s.getUniqueParticipantNameSet());
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
            this.results.append(userId.columnData(i) + this.delim.getSaveSeparator());
        }
        this.results.append(this.delim.getRowSeparator());
        this.numSaved++;
    }

    private void printOffending(String originalEntry, String doubleEntry) {
        SurveyData first = this.s.getParticipantData(originalEntry);
        SurveyData second = this.s.getParticipantData(doubleEntry);

        int maxReach = Math.max(first.internalLength(), second.internalLength());

        String origPrint = "ID- " + originalEntry + ": ";
        String doubPrint = "ID- " + doubleEntry + ": ";

        boolean savedSomething = false;
        StringBuilder comparisonData = new StringBuilder();

        for (int i = 0; i < maxReach || (this.saveOption == 1 && i < this.s.getHeaderColumnLength()); i++) {
            String firstValue = first.columnData(i);
            String secondValue = second.columnData(i);
            boolean foundMismatch = false;

            if (i != this.idKey && i < maxReach && i >= this.firstRelevant) {
                if (!firstValue.equals(secondValue)) {
                    foundMismatch = true;
                    if (savedSomething == false) {
                        if (this.numSaved == 0 && this.saveOption == 0) {
                            this.results.append("Original ID" + this.delim.getSaveSeparator() + "Double Entry ID" + this.delim.getSaveSeparator() + "Mismatched Column Name" + this.delim.getSaveSeparator() + "Mismatched Column Index" + this.delim.getSaveSeparator() + "Original Data" + this.delim.getSaveSeparator() + "Double Entry Data" + this.delim.getRowSeparator());
                        }
                        DoubleEntry.appendStatus("\n-----------------------------------------------------------\n");
                        DoubleEntry.appendStatus("ID: " + originalEntry + " MISMATCH WITH " + doubleEntry);
                        DoubleEntry.appendStatus("\n-----------------------------------------------------------\n");
                        savedSomething = true;
                    } else if (this.saveOption == 0) {
                        this.results.append(this.delim.getRowSeparator());
                    }
                    DoubleEntry.appendStatus(this.s.getHeaderColumn(i) + " (Col. #: " + i + ") MISMATCH - FOUND:\n");
                    DoubleEntry.appendStatus(origPrint + firstValue + "\n");
                    DoubleEntry.appendStatus(doubPrint + secondValue + "\n");
                    if (this.saveOption == 0) {
                        this.results.append(originalEntry + this.delim.getSaveSeparator() + doubleEntry + this.delim.getSaveSeparator() + this.s.getHeaderColumn(i) + this.delim.getSaveSeparator() + i + this.delim.getSaveSeparator() + firstValue + this.delim.getSaveSeparator() + secondValue);
                    } else if (this.saveOption == 1) {
                        comparisonData.append("MISMATCH" + this.delim.getSaveSeparator());
                    }
                }
            }
            if (this.saveOption == 1) {
                if (!foundMismatch && i != this.idKey) {
                    comparisonData.append("SAME" + this.delim.getSaveSeparator());
                } else if (i == this.idKey) {
                    comparisonData.append("COMPARISON_" + firstValue + this.delim.getSaveSeparator());
                }
                this.results.append(firstValue + this.delim.getSaveSeparator());
            }
        }

        if (savedSomething || this.saveOption == 1) {
            this.results.append(this.delim.getRowSeparator());
            String comparisonRow = comparisonData.toString();
            if (!comparisonRow.equals("")) {
                this.results.append(comparisonData.toString() + this.delim.getRowSeparator());
            }
            this.numSaved++;
        }
    }

    private void completeStatement() {
        DoubleEntry.appendStatus("\n-----------------------------\n");
        DoubleEntry.appendStatus("Analysis Complete. No other records found.");
        DoubleEntry.appendStatus("\n-----------------------------\n");
    }
}