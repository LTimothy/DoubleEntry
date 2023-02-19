# Double Entry Validation Tool

### Background
A tool which, given Qualtrics survey exports, can compare different survey entries (for the same participant data) and 
efficiently identify mismatched points. This is useful for "double entry" purposes where if the survey administrator 
decides to enter some data on Qualtrics twice for error checking purposes, the administrator can identify whether or not 
all points of data in one record correspond to all points of data in the other record. This is useful for identifying 
user input error.

This tool takes in a *.tsv or *.csv export from Qualtrics, and compares rows of data based on a unique participant ID
column, a column identifier to begin comparisons at, and a "double entry" participant ID prefix. Through the GUI, users 
can efficiently analyze data reports from Qualtrics to identify discrepancies between the first entry of row of data, 
and the "double entry". This is useful for identifying and reconciling user input data.

### Features
* Takes in a *.tsv or *.csv Qualtrics export file, compares double-entry rows, and identifies mismatched data. 
* Exports mismatched data to a file compatible with Qualtrics for import.
* Basic user interface is provided for broad usability.

### Execution
```
./gradlew build
./gradlew shadowJar
```

### Demo
Sample data is included in this distribution. The ID column is `AF`, the first-relevant column is `C`, and the prefix 
for double-entry data is `X_`.

### Future Improvements
* Integration tests and unit tests, this was rushed to fulfill an immediate need within the lab.
* Refactoring of code into more classes, the responsibility of classes are somewhat meshed and can be improved.

### Additional Notes
Originally developed in April 2020. Refactored in February 2023. Created for the Family and Culture Lab at 
Berkeley, CA as a side-project.
