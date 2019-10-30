# Double Entry Validation Tool

### Background
A tool which, given Qualtrics survey exports, can compare different survey entries (for the same participant data) and efficiently identify mismatched points. This is useful for "double entry" purposes where if the survey administrator decides to enter some data on Qualtrics twice for error checking purposes, the administrator can identify whether or not all points of data in one record correspond to all points of data in the other record. This is useful for identifying user input error.

### Features
* Takes in a (.tsv) Qualtrics export file, compares double-entry rows, and identifies mismatched data. 
* Exports mismatched data to a (.tsv) save file in a simplified format or a full-file format compatable with Qualtrics.
* Basic GUI (user interface).

### Instructions
* Download latest release for your platform. Instructions can be found in the app.
* Sample data is included in this distribution. The ID column is *31* and the prefix for double-entry data is *X_*.

### To-Do
* More Abstraction
* GUI update
* Documentation and comments
* Improved analysis algorithm
* Un-hardcoded values option (e.g. header row index, data rows start index, etc.)
* Advanced Options (e.g. prefix/postfix combination, delimiter choice, remove double-entry rows etc.)
* Logic selection (e.g. Qualtrics, REDCap, etc.)
* Additional file support (automatically save and open different file extensions)
* Allow on-the-fly data correction
* Improved instructions
* Parallelism and multithreading support

### Additional Notes
Work in progress. Created for the Family and Culture Lab at Berkeley, CA as a side-project.

Updated 10/20/19
