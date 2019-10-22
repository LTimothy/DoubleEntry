/**
 * Delimiter provides information about how the file read/write should work.
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

class Delimiter {
	private String delimiter;
	private String saveDelimiter;
	private String rowDelimiter;

	public Delimiter(String delimiter, String saveDelimiter, String rowDelimiter) {
		this.delimiter = delimiter;
		this.saveDelimiter = saveDelimiter;
		this.rowDelimiter = rowDelimiter;
	}

	public String getDelimiter() {
		return this.delimiter;
	}

	public String getSaveDelimiter() {
		return this.saveDelimiter;
	}

	public String getRowDelimiter() {
		return this.rowDelimiter;
	}
}