/**
 * Delimiter provides information about how the file read/write should work.
 *
 * Copyright (C) 2019 Timothy Lee
 *
 * @source: <https://github.com/LTimothy/DoubleEntry>
 * @author: Timothy Lee <https://timothylee.us>
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

	public String getSeparator() {
		return this.delimiter;
	}

	public String getSaveSeparator() {
		return this.saveDelimiter;
	}

	public String getRowSeparator() {
		return this.rowDelimiter;
	}
}