/**
 * Qualtrics Survey is a type of survey. The data starts 2 rows after the header.
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

class QualtricsSurvey extends Survey {
	public QualtricsSurvey(File file, Delimiter delim, int indexColumn, String doublePrefix) {
		super(file, delim, indexColumn, doublePrefix, 2);
	}
}