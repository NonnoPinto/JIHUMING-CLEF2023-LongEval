package analyze;

import org.apache.lucene.analysis.Analyzer;

import java.io.IOException;

/**
 * Lucene custom analyzer created for the English version of the documents.
 *
 * @version 1.0
 * @since 1.0
 */
public class EnglishAnalyzer extends Analyzer
{
    /**
     * Creates a new instance of the analyzer.
     */
    public EnglishAnalyzer() {
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
        //final String text = "I now live in Rome where I met my wife Alice back in 2010 during a beautiful afternoon
        // . " + "Occasionally, I fly to New York to visit the United Nations where I would like to work. The last " + "time I was there in March 2019, the flight was very inconvenient, leaving at 4:00 am, and expensive," + " over 1,500 dollars.";
        final String text = "For the first time, in its 800 academic year, University of Padua has a female " +
                "Rector.";

        // use the analyzer to process the text and print diagnostic information about each token
        //consumeTokenStream(new EnglishAnalyzer(), text);
    }
}