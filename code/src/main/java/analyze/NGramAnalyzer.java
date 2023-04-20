package analyze;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.pattern.PatternReplaceFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Pattern;

import static analyze.AnalyzerUtil.consumeTokenStream;

/**
 * Lucene custom analyzer generating N-Grams of the English and French versions of the documents
 *
 * @version 1.0
 * @since 1.0
 */
public class NGramAnalyzer extends Analyzer
{
    /**
     * Creates a new instance of the analyzer.
     */
    public NGramAnalyzer() {
        super();
    }

    // TODO: test the function and include more filters (if necessary)
    @Override
    protected TokenStreamComponents createComponents(String s) {
        final Tokenizer source = new WhitespaceTokenizer();

        TokenStream tokens = new LowerCaseFilter(source);
        // Remove all real numbers
        tokens = new PatternReplaceFilter(source, Pattern.compile("^(?:-(?:[1-9](?:\\d{0,2}(?:,\\d{3})+|\\d*))|(?:0|(?:[1-9](?:\\d{0,2}(?:,\\d{3})+|\\d*))))(?:.\\d+|)$"),
                "", true);
        tokens = new NGramTokenFilter(source,3);

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
        // TODO: find a text in English and French
        final String text = "";

        // use the analyzer to process the text and print diagnostic information about each token
        consumeTokenStream(new NGramAnalyzer(), text);
    }
}