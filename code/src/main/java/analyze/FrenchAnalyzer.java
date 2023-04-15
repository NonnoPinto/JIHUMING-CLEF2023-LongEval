package analyze;

import org.apache.lucene.analysis.Analyzer;

import java.io.IOException;

/**
 * Lucene custom analyzer created for the French version of the documents.
 *
 * @version 1.0
 * @since 1.0
 */
public class FrenchAnalyzer extends Analyzer
{
    /**
     * Creates a new instance of the analyzer.
     */
    public FrenchAnalyzer() {
        super();
    }

    @Override
    protected TokenStreamComponents createComponents(String s) {
        return null;
    }

    /**
     * Main method of the class.
     *
     * @param args command line arguments.
     *
     * @throws IOException if something goes wrong while processing the text.
     */
    public static void main(String[] args) throws IOException {

        // TODO: complete test main function
        // text to analyze
        // TODO: find a text in French
        final String text = "";

        // use the analyzer to process the text and print diagnostic information about each token
        //consumeTokenStream(new FrenchAnalyzer(), text);
    }
}