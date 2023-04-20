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
 * Lucene custom analyzer generating N-Grams for the English and French versions of the documents.
 *
 * @version 1.0
 * @since 1.0
 */
public class NGramAnalyzer extends Analyzer
{
    /**
     * N parameter of the N-Gram.
     */
    private Integer n;

    /**
     * Creates a new instance of the analyzer.
     */
    public NGramAnalyzer(Integer n) {
        super();
        this.n = n;
    }

    @Override
    protected TokenStreamComponents createComponents(String s) {
        // Delete whitespaces
        final Tokenizer source = new WhitespaceTokenizer();
        // Lowercase all
        TokenStream tokens = new LowerCaseFilter(source);
        // Delete real numbers
        tokens = new PatternReplaceFilter(tokens, Pattern.compile("^(?:-(?:[1-9](?:\\d{0,2}(?:,\\d{3})+|\\d*))|(?:0|(?:[1-9](?:\\d{0,2}(?:,\\d{3})+|\\d*))))(?:.\\d+|)$"),
                "", true);
        // Delete punctuation marks
        tokens = new PatternReplaceFilter(tokens, Pattern.compile("\\p{Punct}"),
                "", true);
        // Create N-Gram
        tokens = new NGramTokenFilter(tokens, n);

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

        // Text to analyze
        final String enText = "A web search engine is a software system designed to carry out web searches. They search " +
                "the World Wide Web in a systematic way for particular information specified in a textual web search " +
                "query.";
        final String frText = "Un moteur de recherche web est un système logiciel conçu pour effectuer des recherches " +
                "sur le web. Ils recherchent systématiquement sur le World Wide Web des informations particulières " +
                "spécifiées dans une requête textuelle de recherche sur le Web.";
        final String text = enText + " " + frText;

        // Use the analyzer to process the text and print diagnostic information about each token
        consumeTokenStream(new NGramAnalyzer(3), text);
    }
}