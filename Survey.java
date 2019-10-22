/**
 * Survey represents the entire survey's data.
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

class Survey {
	// Participant Information
	private List<SurveyData> participantInformation;
    private Map<String, SurveyData> idParticipantMap;
	private List<SurveyData> excludedFromMap;

	// Read From
    private BufferedReader readFile;
    private String headerColumns[];
    private Delimiter delim;
    private int indexColumn;
    private String doublePrefix;

    // Output (if desired) of reading items
    private StringBuilder results;

	public Survey(File file, Delimiter delim, int indexColumn, String doublePrefix, int dataStart) {
    	this.participantInformation = new ArrayList<>();
    	this.excludedFromMap = new ArrayList<>();
    	this.idParticipantMap = new HashMap<>();
    	this.delim = delim;
    	this.indexColumn = indexColumn;
    	this.doublePrefix = doublePrefix;
    	this.results = new StringBuilder();

    	try {
            this.readFile = new BufferedReader(new FileReader(file, StandardCharsets.UTF_16));
        } catch (Exception e) {
        	System.out.println("Survey Constructor (1): " + e);
        	try {
	            DoubleEntry.appendStatus("ERROR: No file found. Does " + file.getName() + " exist?" + this.delim.getRowSeparator());
	        } catch (NullPointerException f) {
	        	System.out.println("Survey Constructor (2): " + f);
	        	DoubleEntry.appendStatus("ERROR: No valid file selected." + this.delim.getRowSeparator());
	        	return;
	        }
	        return;
        }

        loadHeader();
        skipLines(dataStart);
        loadSurveys();
        makeMap();

        if (this.readFile != null) {
        	try {
            	this.readFile.close();
            } catch (IOException e) {
            	System.out.println("Survey Constructor (3): " + e);
            	DoubleEntry.appendStatus("ERROR: Failed to close file." + this.delim.getRowSeparator());
            	return;
            }
        }
	}

	public SurveyData getParticipantData(String name) {
		SurveyData temp = this.idParticipantMap.get(name);
		if (temp != null) {
			return temp;
		} else {
			for (int i = 0; i < participantInformation.size(); i++) {
				if (participantInformation.get(i).participantIdentifier().equals(name)) {
					return participantInformation.get(i);
				}
			}
		}
		return null;
	}

	public Set<String> getUniqueParticipantNameSet() {
		return this.idParticipantMap.keySet();
	}

	public List<SurveyData> getMapExcludedData() {
		return this.excludedFromMap;
	}

	public String getPrefix() {
		return this.doublePrefix;
	}

	public int getHeaderColumnLength() {
		return this.headerColumns.length;
	}

	public String getHeaderColumn(int i) {
		return this.headerColumns[i];
	}

	public String getResult() {
		return this.results.toString();
	}

	public int getIndex() {
		return this.indexColumn;
	}

	public Delimiter getDelimiter() {
		return this.delim;
	}

    private void loadHeader() {
    	try {
            String  line = this.readFile.readLine();
    		headerColumns = line.split(this.delim.getSeparator());
            results.delete(0, results.length());
            results.append(rowTerminator(line));
	    } catch (IOException e) {
	    	System.out.println("loadHeader(): " + e);
	    	DoubleEntry.appendStatus("DEBUG: Attempted to load header but not possible." + this.delim.getRowSeparator());
	    	return;
	    }
    }

    private void skipLines(int lines) {
    	try {
	    	while (lines > 0) {
                results.append(rowTerminator(this.readFile.readLine()));
	    		lines--;
	    	}
	    } catch (IOException e) {
	    	System.out.println("skipLines(): " + e);
	    	DoubleEntry.appendStatus("DEBUG: Attempted to skip more lines than file has remaining." + this.delim.getRowSeparator());
	    	return;
	    }
    }

	private void loadSurveys() {
    	String loadData;
    	try {
	    	while ((loadData = this.readFile.readLine()) != null) {
	    		participantInformation.add(new SurveyData(headerColumns, loadData, this.delim.getSeparator(), this.indexColumn));
	    	}
	    } catch (IOException e) {
	    	System.out.println("loadSurveys(): " + e);
	    	DoubleEntry.appendStatus("DEBUG: Unable to load surveys." + this.delim.getRowSeparator());
	    	return;
	    }
    }

    private void makeMap() {
        boolean printDuplicate = false;
        for (int i = 0; i < participantInformation.size(); i++) {
            SurveyData data = participantInformation.get(i);
            String id = data.participantIdentifier();
            if (!printDuplicate && idParticipantMap.containsKey(id) && id != "N/A") {
                printDuplicate = true;
            }
            if (idParticipantMap.containsKey(id)) {
                this.excludedFromMap.add(idParticipantMap.get(id));
            }
            idParticipantMap.put(id, data);
        }

        if (printDuplicate) {
            DoubleEntry.appendStatus("NOTICE: Duplicate IDs detected. Non-deterministic behavior may occur." + this.delim.getRowSeparator());
        }

        Set<String> participantPool = idParticipantMap.keySet();
        if (participantPool.contains("N/A")) {
        	DoubleEntry.appendStatus("NOTICE: Some records may have missing ids! Non-deterministic behavior may occur on entries with missing id." + this.delim.getRowSeparator());
        }
    }

    private String rowTerminator(String line) {
        if (!line.endsWith("" + this.delim.getRowSeparator())) {
            line += "" + this.delim.getRowSeparator();
        }
        return line;
    }
}