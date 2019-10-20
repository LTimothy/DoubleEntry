import java.io.*;
import java.util.*;
import java.lang.*;
import java.nio.charset.StandardCharsets;

public class QualtricsDE {
    private static String delimiter;
    private static String headerColumns[];
    private static BufferedReader TSVFile;
    private static List<SurveyData> participantInformation;
    private static Map<String, SurveyData> idParticipantMap;
    private static int idKey;
    private static String idPrefix;
    private static Scanner systemInput;

    // Constructor
    public QualtricsDE(int idColumn, String idPrefix, File file) throws Exception {
        systemInput = new Scanner(System.in);
        delimiter = "[\\t]";
        idKey = -1;

        try {
            TSVFile = new BufferedReader(new FileReader(file, StandardCharsets.UTF_16));
        } catch (Exception e) {
            DoubleEntry.appendStatus("ERROR: No File Found. Does " + file.getName() + " exist?\n");
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

    private static void analyzeSurveys() {
        idParticipantMap = new HashMap<>();
        boolean printDuplicate = false;
        for (int i = 0; i < participantInformation.size(); i++) {
            SurveyData data = participantInformation.get(i);
            String id = data.participantIdentifier();
            if (!printDuplicate && idParticipantMap.containsKey(id) && id != "MISSING ID") {
                printDuplicate = true;
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
            }
        }
    }

    private static void printOffending(String originalEntry, String doubleEntry) {
        DoubleEntry.appendStatus("\n------------------------------------------------------------\n");
        DoubleEntry.appendStatus("ID: " + originalEntry + " MISMATCH WITH " + doubleEntry);
        DoubleEntry.appendStatus("\n------------------------------------------------------------\n");

        SurveyData first = idParticipantMap.get(originalEntry);
        SurveyData second = idParticipantMap.get(doubleEntry);

        int maxReach = Math.max(first.internalLength(), second.internalLength());

        String origPrint = "ID- " + originalEntry + ": ";
        String doubPrint = "ID- " + doubleEntry + ": ";

        for (int i = 0; i < maxReach; i++) {
            if (i != idKey) {
                String firstValue = first.columnData(i);
                String secondValue = second.columnData(i);
                if (!firstValue.equals(secondValue)) {
                    DoubleEntry.appendStatus(headerColumns[i] + " (Col. #: " + i + " MISMATCH - FOUND:\n");
                    DoubleEntry.appendStatus(origPrint + firstValue + "\n");
                    DoubleEntry.appendStatus(doubPrint + secondValue + "\n");
                }
            }
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

    private static void initializeHeader() {
    	try {
    		headerColumns = TSVFile.readLine().split(delimiter);
	    } catch (IOException e) {
	    	DoubleEntry.appendStatus("DEBUG: Attempted to initialize header but not possible.\n");
	    }
    }

    private static void skipLines(int lines) {
    	try {
	    	while (lines > 0) {
	    		TSVFile.readLine();
	    		lines--;
	    	}
	    } catch (IOException e) {
	    	DoubleEntry.appendStatus("DEBUG: Attempted to skip more lines than file has remaining.\n");
	    }
    }
}