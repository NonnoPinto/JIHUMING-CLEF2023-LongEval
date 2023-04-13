# Parsing

The parsing part of our system is implemented in the package named "parse". This section
contains the LongEvalParser class used to parse every JSON corpus file inside the corpus directory
of the project. 
The documents corpus used in this system were selected with respect to these queries using the Qwant 
click model. Apart from the documents selected using this model, the collection also contains randomly
selected documents from the Qwant index.

The folder content: 
* `DocumentParser`: DocumentParser parse the document .
* `JsonDocument`: this folder contains the runs produced by the developed system.
* `LongEvalParser`: this folder contains the performance scores of the runs.
* `ParsedDocument`:  document parsed has FIELDS including ID, ENGLISH_BODY, and FRENCH_BODY .