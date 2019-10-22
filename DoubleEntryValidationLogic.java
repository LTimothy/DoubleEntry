/**
 * Double Entry Validation Logic Abstraction.
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

public abstract class DoubleEntryValidationLogic {
	String delimiter;
	String saveDelimiter;
	int idKey;
	File inputFile;

	abstract String getResult();
}