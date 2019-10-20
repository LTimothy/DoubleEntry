/**
 * Survey Data Object (WIP). Represents Individual Participants.
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

class SurveyData {
	private Map<String, String> surveyData;
	private String[] rawData;
	private int internalLength;
	private String[] headerColumns;
	private String delimiter;
	private int columnKey;

	public SurveyData(String[] headerData, String parsableData, String delimiter, int columnId) {
		surveyData = new TreeMap<>();
		rawData = parsableData.split(delimiter);
		internalLength = rawData.length;
		headerColumns = headerData;
		this.delimiter = delimiter;
		columnKey = columnId;
		parseData(rawData);
	}

	private void parseData(String[] personalInformation) {
		for (int i = 0; i < headerColumns.length; i++) {
			if (i < personalInformation.length) {
				surveyData.put(headerColumns[i], personalInformation[i].toLowerCase().trim());
			} else {
				surveyData.put(headerColumns[i], "");
			}
		}
	}

	public String participantIdentifier() {
		String id = surveyData.get(headerColumns[columnKey]);
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