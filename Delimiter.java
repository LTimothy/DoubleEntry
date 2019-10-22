/**
 * Delimiter provides information about how the file read/write should work.
 *
 * Copyright (C) 2019 Timothy Lee
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
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