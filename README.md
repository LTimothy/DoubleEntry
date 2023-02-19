# Double Entry Validation Tool

## Background

### About
This tool takes in a `*.tsv` or `*.csv` export from Qualtrics, and compares rows of data based on a unique participant
ID column, a column identifier to begin comparisons at, and a "double entry" participant ID prefix. Through the GUI,
users can efficiently analyze data reports from Qualtrics to identify discrepancies between the first entry of row of
data, and the "double entry". This is useful for identifying and reconciling user input data.

### What is a "double entry"?
> Double-entry bookkeeping, also known as double-entry accounting, is a method of bookkeeping that relies on a two-sided 
> accounting entry to maintain financial information. Every entry to an account requires a corresponding and opposite 
> entry to a different account. The double-entry system has two equal and corresponding sides known as debit and credit. 
> A transaction in double-entry bookkeeping always affects at least two accounts, always includes at least one debit and 
> one credit, and always has total debits and total credits that are equal. The purpose of double-entry bookkeeping is 
> to allow the detection of financial errors and fraud. - Wikipedia (2023-02-19)

In the context of this tool, a double-entry is a re-entry of an existing record in a system like Qualtrics (or simply, 
an additional row of data in a `*.tsv` or `*.csv` file) that should be compared to an existing row. 

This tool facilitates this comparison process by reading in a supported file and several parameters specified by the 
user to automatically identify mismatches in the two entries in a manner consistent with the requirements of the Family 
and Culture Lab at UC Berkeley.

### Features
* Takes in a `*.tsv` or `*.csv` Qualtrics export file, compares double-entry rows, and identifies mismatched data. 
* Exports mismatched data to a file compatible with Qualtrics for import.
* Basic user interface is provided for broad usability.

### Execution
1. Download the latest release `*.jar` file from [GitHub](https://github.com/LTimothy/DoubleEntry/releases)
   if you have not already done so. Alternatively, you may opt to follow the instructions under "Build" to create the
   `*.jar` file yourself.
2. Download the JDK from [Oracle](https://www.oracle.com/java/technologies/downloads/) if you have not already done so.
3. Go to the directory where your `*.jar` file is located. You may opt to follow
   [this tutorial](https://tutorials.codebar.io/command-line/introduction/tutorial.html) if you have not done this before.
4. Start the program by typing `java -jar YOUR_JAR_NAME.jar`, replacing `YOUR_JAR_NAME` with the name of the file you
   downloaded or built manually.

### Demo
Sample data is included in this distribution, it can also be found in the `SampleData` folder on 
[GitHub](https://github.com/LTimothy/DoubleEntry/tree/master/SampleData). The ID column is `AF`, the first-relevant 
column is `C`, and the prefix for double-entry data is `X_`.

## Development

### Build
```
./gradlew build
./gradlew shadowJar
```

### Future Improvements
* Integration tests and unit tests, this was rushed to fulfill an immediate need within the lab.
* Refactoring of code into more classes, the responsibility of classes are somewhat meshed and can be improved.

### Additional Notes
Originally developed in April 2020. Refactored in February 2023. Created for the Family and Culture Lab at 
Berkeley, CA as a side-project.
