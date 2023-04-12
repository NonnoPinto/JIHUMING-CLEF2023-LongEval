package parse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class LongevalParser extends DocumentParser {

    /**
     * The size of the buffer for the body element.
     */
    private static final int BODY_SIZE = 1024 * 8;

    /**
     * The currently parsed document
     */
    private ParsedDocument document = null;


    /**
     * Creates a new Longeval Corpus document parser.
     *
     * @param in the reader to the document(s) to be parsed.
     * @throws NullPointerException     if {@code in} is {@code null}.
     * @throws IllegalArgumentException if any error occurs while creating the parser.
     */
    public LongevalParser(final Reader in) {
        super(new BufferedReader(in));
    }


    @Override
    public boolean hasNext() {

        String id = null;
        StringBuilder body = new StringBuilder(BODY_SIZE);


        try {
            //TODO: here the parsing of a document must be implemented.
            // It must follow the rules specified by LongEval Train Collection Readme
            id = null; //TODO
            body = null; //TODO
        } catch (Exception e)
        {
            throw new IllegalStateException("Unable to parse the document.", e);
        }
        /*catch (IOException e) {
            throw new IllegalStateException("Unable to parse the document.", e);
        }*/

        if (id != null) {
            document = new ParsedDocument(id, body.length() > 0 ?
                    body.toString().replaceAll("<[^>]*>", " ") : "#");
        }

        return next;
    }

    @Override
    protected final ParsedDocument parse() {
        return document;
    }


    /**
     * Main method of the class. Just for testing purposes.
     *
     * @param args command line arguments.
     * @throws Exception if something goes wrong while indexing.
     */
    public static void main(String[] args) throws Exception {

        Reader reader = new FileReader(
                "file.txt"); //TODO

        LongevalParser p = new LongevalParser(reader);

        for (ParsedDocument d : p) {
            System.out.printf("%n%n------------------------------------%n%s%n%n%n", d.toString());
        }
    }
}
