/**
* Qualtrics Double Entry Tool. Work in progress.
* Takes in a .tsv Qualtrics export file, compares double-entry rows, and identifies mismatched data.
* Project maintained at https://github.com/LTimothy/QualtricsDoubleEntryValidation
*
* Updated at 10/18/19
*
* @author Timothy Lee
*/


import java.io.*;
import java.util.*;
import java.lang.*;
import java.nio.charset.StandardCharsets;

public class QualtricsDoubleEntryValidation {
	private static String delimiter = "[\\t]";
	private static String headerColumns[];
	private static BufferedReader TSVFile;
	private static List<PersonData> participantInformation;
	private static Map<String, PersonData> idParticipantMap;
	private static String idString;
	private static int idKey;
	private static String idPrefix;
	private static Scanner systemInput;

    public static void main(String[] arg) throws Exception {
    	systemInput = new Scanner(System.in);

    	String filename = promptUser(2);

    	try {
	        TSVFile = new BufferedReader(new FileReader(filename, StandardCharsets.UTF_16));
	    } catch (Exception e) {
	    	System.out.println("DEBUG: No file found. Does data.tsv exist?");
	    	System.exit(1);
	    }

	    initializeHeader();
	    promptUser(0);
	    promptUser(1);
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
    		PersonData data = participantInformation.get(i);
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

    	PersonData first = idParticipantMap.get(originalEntry);
    	PersonData second = idParticipantMap.get(doubleEntry);

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

    private static String promptUser(int prompt) {
    	try {
	    	switch (prompt) {
	    		case 0: System.out.print("Where column A is 0, what column is the participant identifier located on: ");
	    				idKey = systemInput.nextInt();
	    				idString = headerColumns[idKey];
	    				systemInput.nextLine();
	    				break;
	    		case 1: System.out.print("What are IDs prefixed with for double entry: ");
	    				idPrefix = systemInput.nextLine().toLowerCase().trim();
	    				break;
	    		case 2: System.out.print("What is the name of the file (case-sensitive): ");
	    				return systemInput.nextLine();
	    		default: break;
	    	}
	    } catch (IndexOutOfBoundsException e) {
	    	System.out.println("DEBUG: Out of bounds error.");
	    	System.exit(1);
	    }
	    System.out.println();
	    return "N/A";
    }

    private static void loadSurveys() {
    	participantInformation = new ArrayList<>();
    	String loadData;

    	try {
	    	while ((loadData = TSVFile.readLine()) != null) {
	    		participantInformation.add(new PersonData(loadData));
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

    static class PersonData {
    	private Map<String, String> personData;
    	private String[] rawData;
    	private int internalLength;

    	public PersonData(String info) {
    		personData = new TreeMap<>();
    		rawData = info.split(delimiter);
    		internalLength = rawData.length;
    		parseData(rawData);
    	}

    	private void parseData(String[] personalInformation) {
    		for (int i = 0; i < headerColumns.length; i++) {
    			if (i < personalInformation.length) {
    				personData.put(headerColumns[i], personalInformation[i].toLowerCase().trim());
    			} else {
    				personData.put(headerColumns[i], "");
    			}
    		}
    	}

    	public String participantIdentifier() {
    		String id = personData.get(idString);
    		if (id.equals("")) {
    			return "MISSING ID";
    		} else {
    			return id;
    		}
    	}

    	public int internalLength() {
    		return internalLength;
    	}

    	public String columnData(int column) {
    		if (column < internalLength) {
    			return rawData[column];
    		}
    		return "";
    	}
    }
}