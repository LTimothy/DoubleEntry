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

	public SurveyData(String[] headerData, String parsableData, String delimiter, int indexColumn) {
		this.surveyData = new TreeMap<>();
		this.rawData = parsableData.split(delimiter);
		this.internalLength = rawData.length;
		this.headerColumns = headerData;
		this.delimiter = delimiter;
		this.indexColumn = indexColumn;
		parseData(rawData);
	}

	private void parseData(String[] personalInformation) {
		for (int i = 0; i < headerColumns.length; i++) {
			if (i < personalInformation.length) {
				if (this.surveyData.containsKey(headerColumns[i])) {
					DoubleEntry.appendStatus("NOTICE: Duplicate variable names in header detected. Non-deterministic behavior may occur." + this.delim.getRowSeparator());
				}
				this.surveyData.put(headerColumns[i], personalInformation[i].toLowerCase().trim());
			} else {
				this.surveyData.put(headerColumns[i], "");
			}
		}
	}

	public String participantIdentifier() {
		String id = this.surveyData.get(headerColumns[this.indexColumn]);
		if (id.equals("")) {
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