/**
 * Survey data represents data of individual participants.
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
	private int indexColumn;
	private String[] reserved;

	public SurveyData(String[] headerData, String parsableData, String delimiter, int indexColumn) {
		this.surveyData = new TreeMap<>();
		this.rawData = parsableData.split(delimiter);
		this.internalLength = rawData.length;
		this.headerColumns = headerData;
		this.delimiter = delimiter;
		this.indexColumn = indexColumn;
		this.reserved = new String[2];
		this.reserved[0] = "N/A";
		this.reserved[1] = "NULL";
		parseData();
	}

	private void parseData() {
		boolean reservedKeywordFound = false;
		for (int i = 0; i < headerColumns.length; i++) {
			if (i < this.rawData.length) {
				if (this.surveyData.containsKey(headerColumns[i])) {
					DoubleEntry.appendStatus("NOTICE: Duplicate variable names in header detected. Non-deterministic behavior may occur. \n");
				}
				String toAdd = this.rawData[i].trim().toLowerCase();
				if (reservedKeywordFound == false) {
					for (int j = 0; j < reserved.length; j++) {
						if (reserved[j].equals(toAdd)) {
							reservedKeywordFound = true;
							break;
						}
					}
				}
				this.surveyData.put(headerColumns[i], toAdd);
			} else {
				this.surveyData.put(headerColumns[i], "");
			}
		}

		if (reservedKeywordFound) {
			DoubleEntry.appendStatus("NOTICE: Reserved keyword detected. Non-deterministic behavior may occur. \n");
			DoubleEntry.appendStatus("RESERVED KEYWORDS: ");
			for (int k = 0; k < reserved.length; k++) {
				DoubleEntry.appendStatus(reserved[k] + " ");
			}
			DoubleEntry.appendStatus("\n");
		}
	}

	public String participantIdentifier() {
		String id = this.surveyData.get(headerColumns[this.indexColumn]);
		if (id == null) {
			return "NULL";
		} else if (id.equals("")) {
			return "N/A";
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