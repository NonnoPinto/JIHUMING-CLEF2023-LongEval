package analyze;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import org.apache.lucene.analysis.pattern.PatternReplaceFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import parse.LongEvalParser;
import parse.ParsedDocument;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Pattern;

import static analyze.AnalyzerUtil.consumeTokenStream;
import static analyze.AnalyzerUtil.loadStopList;

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
        // TODO: decide how are we going to process our English texts

        // Whitespace tokenizer
        final Tokenizer source = new WhitespaceTokenizer();

        // Lowercase
        TokenStream tokens = new LowerCaseFilter(source);

        // Delete some estrange symbols found in documents
        tokens = new PatternReplaceFilter(tokens, Pattern.compile("[·–“”…]+"), "", true);

        // Delete punctuation marks at the beginning of words (text)
        tokens = new PatternReplaceFilter(tokens, Pattern.compile("^[\\p{Punct}]+"), "", true);

        // Delete punctuation marks at the end of words (text)
        tokens = new PatternReplaceFilter(tokens, Pattern.compile("[\\p{Punct}]+$"), "", true);

        // Apply WordDelimiterGraphFilter with the following options
        tokens = new WordDelimiterGraphFilter(tokens,
                WordDelimiterGraphFilter.GENERATE_WORD_PARTS // Ex: "PowerShot" => "Power" "Shot"
                        //| WordDelimiterGraphFilter.GENERATE_NUMBER_PARTS // Ex: "500-42" => "500" "42"
                        | WordDelimiterGraphFilter.PRESERVE_ORIGINAL // Ex: "500-42" => "500" "42" "500-42"
                        | WordDelimiterGraphFilter.SPLIT_ON_CASE_CHANGE // Causes lowercase -> uppercase transition to start a new subword.
                        //| WordDelimiterGraphFilter.SPLIT_ON_NUMERICS // If not set, causes numeric changes to be ignored (subwords will only be generated given SUBWORD_DELIM tokens).
                        | WordDelimiterGraphFilter.STEM_ENGLISH_POSSESSIVE, // "O'Neil's" => "O", "Neil"
                null);

        // Apply TERRIER stopword list
        tokens = new StopFilter(tokens, loadStopList("terrier.txt"));

        // Remove tokens with empty text
        tokens = new EmptyTokenFilter(tokens);

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
        // Take one example (parsed) (English) document from the training set (pdExample)
        final String FILE_NAME = "C:\\longeval_train\\publish\\English\\Documents\\Json\\collector_kodicare_1.txt.json";
        Reader reader = new FileReader(
                FILE_NAME);
        LongEvalParser parser = new LongEvalParser(reader);
        ParsedDocument pdExample = null;
        if (parser.hasNext())
            pdExample = parser.next();

        if (pdExample != null) {
            System.out.println("--------- PARSED DOCUMENT ---------");
            System.out.println(pdExample.getBody());
            System.out.println("--------- ANALYZER RESULT ---------");
            // use the analyzer to process the text and print diagnostic information about each token
            consumeTokenStream(new EnglishAnalyzer(), pdExample.getBody(), false);
        } else {
            System.out.println("Error, could not retrieve document to apply analyzer");
        }
    }
}