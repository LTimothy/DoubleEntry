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
    private static String idString;
    private static int idKey;
    private static String idPrefix;
    private static Scanner systemInput;

    // Constructor
    public QualtricsDE() throws Exception {
        systemInput = new Scanner(System.in);
        delimiter = "[\\t]";
        idKey = -1;
        String filename = promptUser("What is the name of the file (case-sensitive): ");

        try {
            TSVFile = new BufferedReader(new FileReader(filename, StandardCharsets.UTF_16));
        } catch (Exception e) {
            System.out.println("DEBUG: No file found. Does " + filename + " exist?");
            System.exit(1);
        }

        initializeHeader();

        idKey = Integer.valueOf(promptUser("Where column A is 0, what column is the participant identifier located on: "));
        while (idKey == -1 || idKey > headerColumns.length) {
            System.out.println("NOTICE: Invalid ID Column. Try again.");
            idKey = Integer.valueOf(promptUser("Where column A is 0, what column is the participant identifier located on: "));
        }
        idPrefix = promptUser("What are IDs prefixed with for double entry: ").toLowerCase().trim();

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
        for (int i = 0; i < participantInformation.size(); i++) {
            SurveyData data = participantInformation.get(i);
            String id = data.participantIdentifier();
            idParticipantMap.put(id, data);
        }

        Set<String> participantPool = idParticipantMap.keySet();
        if (participantPool.contains("MISSING ID")) {
        	System.out.println("NOTICE: Some records may have missing ids! Non-deterministic behavior may occur on entries with missing id.\n");
        }

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
        System.out.println("\n------------------------------");
        System.out.println("ID: " + originalEntry + " MISMATCH WITH " + doubleEntry);
        System.out.println("------------------------------\n");

        SurveyData first = idParticipantMap.get(originalEntry);
        SurveyData second = idParticipantMap.get(doubleEntry);

        int maxReach = Math.max(first.internalLength(), second.internalLength());

        for (int i = 0; i < maxReach; i++) {
            if (i != idKey) {
                String firstValue = first.columnData(i);
                String secondValue = second.columnData(i);
                if (!firstValue.equals(secondValue)) {
                    System.out.println("COLUMN " + i + " MISMATCH - FOUND:");
                    System.out.println("originalEntry: " + firstValue);
                    System.out.println("doubleEntry: " + secondValue);
                    System.out.println();
                }
            }
        }

    }

    private static void completeStatement() {
        System.out.println("\n------------------------------");
        System.out.println("Analysis Complete. No other records found.");
        System.out.println("------------------------------\n");
    }

    private static String promptUser(String prompt) {
        System.out.print(prompt);
        String data = systemInput.nextLine();
        System.out.println();
        return data;
    }

    private static void loadSurveys() {
    	participantInformation = new ArrayList<>();
    	String loadData;

    	try {
	    	while ((loadData = TSVFile.readLine()) != null) {
	    		participantInformation.add(new SurveyData(headerColumns, loadData, delimiter, idKey));
	    	}
	    } catch (IOException e) {
	    	System.out.println("DEBUG: Unable to load surveys.");
	    	System.exit(1);
	    }
    }

    private static void initializeHeader() {
    	try {
    		headerColumns = TSVFile.readLine().split(delimiter);
	    } catch (IOException e) {
	    	System.out.println("DEBUG: Attempted to initialize header but not possible.");
	    	System.exit(1);
	    }
    }

    private static void skipLines(int lines) {
    	try {
	    	while (lines > 0) {
	    		TSVFile.readLine();
	    		lines--;
	    	}
	    } catch (IOException e) {
	    	System.out.println("DEBUG: Attempted to skip more lines than file has remaining.");
	    	System.exit(1);
	    }
    }
}