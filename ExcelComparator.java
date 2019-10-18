import java.io.*;
import java.util.*;
import java.lang.*;
import java.nio.charset.StandardCharsets;

public class ExcelComparator {
	private static String headerColumns[];
	private static BufferedReader TSVFile;
	private static List<PersonData> participantInformation;

    public static void main(String[] arg) throws Exception {
    	try {
	        TSVFile = new BufferedReader(new FileReader("data.tsv", StandardCharsets.UTF_16));
	    } catch (Exception e) {
	    	System.out.println("DEBUG: No file found. Does data.tsv exist?");
	    }

	    initializeHeader();
    	skipLines(2);
    	loadSurveys();

	    if (TSVFile != null) {
	    	TSVFile.close();
	    }
    }

    private static boolean loadSurveys() {
    	participantInformation = new ArrayList<>();
    	String loadData;

    	try {
	    	while ((loadData = TSVFile.readLine()) != null) {
	    		participantInformation.add(new PersonData(loadData));
	    	}
	    } catch (IOException e) {
	    	System.out.println("DEBUG: Unable to load surveys.");
	    	return false;
	    }
	    return true;
    }

    private static boolean initializeHeader() {
    	try {
    		headerColumns = TSVFile.readLine().split("[\\t]");
	    } catch (IOException e) {
	    	System.out.println("DEBUG: Attempted to initialize header but not possible.");
	    	return false;
	    }
	    return true;
    }

    private static boolean skipLines(int lines) {
    	try {
	    	while (lines > 0) {
	    		TSVFile.readLine();
	    		lines--;
	    	}
	    } catch (IOException e) {
	    	System.out.println("DEBUG: Attempted to skip more lines than file has remaining.");
	    	return false;
	    }
	    return true;
    }

    static class PersonData {
    	private Map<String, String> personData;

    	public PersonData(String info) {
    		personData = new TreeMap<>();
    		parseData(info.split("[\\t]"));
    	}

    	private void parseData(String [] personalInformation) {
    		for (int i = 0; i < headerColumns.length; i++) {
    			if (i < personalInformation.length) {
    				personData.put(headerColumns[i], personalInformation[i]);
    			} else {
    				personData.put(headerColumns[i], "");
    			}
    		}
    	}
    }
}