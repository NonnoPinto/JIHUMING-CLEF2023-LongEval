package parse;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Implements a document parser for the LongEval corpus (JSON version).
 *
 * @version 1.00
 * @since 1.00
 */
public class LongEvalParser extends DocumentParser {

    /**
     * The size of the buffer for the body element.
     */
    private static final int BODY_SIZE = 1024 * 8;

    /**
     * The currently parsed document
     */
    private ParsedDocument document = null;

    /**
     * The JSON reader to be used to parse document(s).
     */
    private JsonReader in_json;

    /**
     * Creates a new Longeval Corpus (JSON) document parser.
     *
     * @param in the reader to the document(s) to be parsed.
     * @throws NullPointerException     if {@code in} is {@code null}.
     * @throws IllegalArgumentException if any error occurs while creating the parser.
     */
    public LongEvalParser(final Reader in) {
        super(new BufferedReader(in));

        in_json = new JsonReader(new BufferedReader(in));
        try {
            in_json.beginArray();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to parse the JSON file.", e);
        }
    }

    @Override
    public boolean hasNext() {

        // JSON stream reading taken from https://www.amitph.com/java-parse-large-json-files/
        try {
            if (in_json.hasNext()) {
                JsonDocument jdoc = new Gson().fromJson(in_json, JsonDocument.class);
                document = new ParsedDocument(jdoc.getId(), jdoc.getContents());

                next = true;
            } else {
                in_json.endArray();
                in_json.close();
                next = false;
            }
        }
        catch (IOException e) {
            throw new IllegalStateException("Unable to parse the document.", e);
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

        LongEvalParser p = new LongEvalParser(reader);

        for (ParsedDocument d : p) {
            System.out.printf("%n%n------------------------------------%n%s%n%n%n", d.toString());
        }
    }
}
