# Parsing

The parsing part of our system is implemented in the package named `parse`. This section
contains the LongEvalParser class used to parse every JSON corpus file inside the corpus directory
of the project. 
The document corpus used in this system was selected with respect to some queries using the Qwant 
click model. Apart from the documents selected using this model, the collection also contains randomly
selected documents from the Qwant index.

The folder content: 
* `DocumentParser`: abstract class that must implement every implemented document parser.
* `LongEvalParser`: implementation of a document parser for the documents of this project, given a file provides several
documents composed by an id and a body. Note that it provides the documents in an ``Iterator`` way.
* `ParsedDocument`: represents a document that has been parsed, and contains an id and a body.
* `JsonDocument`: Java POJO to support the conversion from JSON strings to Java objects.