package analyze;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import java.io.IOException;
import java.io.Reader;

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
        final Tokenizer source = new StandardTokenizer();

        TokenStream tokens = new LowerCaseFilter(source);
        // TODO: decide how are we going to process our English texts

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
        // text to analyze, taken from: https://www.opensourceshakespeare.org/views/plays/play_view.php?WorkID=hamlet&Act=5&Scene=2&Scope=scene

        final String text = "An earnest conjuration from the King,\n" +
                "As England was his faithful tributary,\n" +
                "As love between them like the palm might flourish,\n" +
                "As peace should still her wheaten garland wear\n" +
                "And stand a comma 'tween their amities,\n" +
                "And many such-like as's of great charge,\n" +
                "That, on the view and knowing of these contents,\n" +
                "Without debatement further, more or less,\n" +
                "He should the bearers put to sudden death,\n" +
                "Not shriving time allow'd. ";

        // use the analyzer to process the text and print diagnostic information about each token
        //consumeTokenStream(new EnglishAnalyzer(), text);
    }
}