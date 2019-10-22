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