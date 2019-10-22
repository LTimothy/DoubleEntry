/**
 * QualtricsSurvey is a type of survey. The data starts 2 rows after the header.
 *
 * Copyright (C) 2019 Timothy Lee
 *
 * @source: <https://github.com/LTimothy/DoubleEntry>
 * @author: Timothy Lee <https://timothylee.us>
 */

import java.io.*;

class QualtricsSurvey extends Survey {
	public QualtricsSurvey(File file, Delimiter delim, int indexColumn, String doublePrefix) {
		super(file, delim, indexColumn, doublePrefix, 2);
	}
}