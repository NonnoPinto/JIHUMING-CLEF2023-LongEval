package analyze;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import java.io.IOException;
import java.io.Reader;

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
        final Tokenizer source = new StandardTokenizer();

        TokenStream tokens = new LowerCaseFilter(source);
        // TODO: decide how are we going to process our French texts

        return new TokenStreamComponents(source, tokens);
    }

    @Override
    protected Reader initReader(String fieldName, Reader reader) {
        return super.initReader(fieldName, reader);
    }

    @Override
    protected TokenStream normalize(String fieldName, TokenStream in) {
        return new LowerCaseFilter(in);
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