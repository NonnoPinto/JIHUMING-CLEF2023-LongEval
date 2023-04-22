package analyze;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.*;

import java.io.IOException;
import java.io.StringReader;

/**
 * Helper class to load stop lists and <a href="http://opennlp.apache.org/" target="_blank">Apache OpenNLP</a> models
 * from the {@code resource} directory as well as consume {@link org.apache.lucene.analysis.TokenStream}s and print
 * diagnostic information about them.
 *
 * @author Nicola Ferro
 * @version 1.0
 * @since 1.0
 */
public class AnalyzerUtil {

    /**
     * The class loader of this class. Needed for reading files from the {@code resource} directory.
     */
    private static final ClassLoader CL = AnalyzerUtil.class.getClassLoader();

    /**
     * Consumes a {@link org.apache.lucene.analysis.TokenStream} for the given text by using the provided
     * {@link org.apache.lucene.analysis.Analyzer} and prints diagnostic information about all the generated tokens and
     * their {@link org.apache.lucene.util.Attribute}s.
     *
     * @param a the analyzer to use.
     * @param t the text to process.
     *
     * @throws IOException if something goes wrong while processing the text.
     */
    static void consumeTokenStream(final Analyzer a, final String t, boolean printTokenInfo) throws IOException {

        // the start time of the processing
        final long start = System.currentTimeMillis();

        // Create a new TokenStream for a dummy field
        final TokenStream stream = a.tokenStream("field", new StringReader(t));

        // Lucene tokens are decorated with different attributes whose values contain information about the token,
        // e.g. the term represented by the token, the offset of the token, etc.

        // The term represented by the token
        final CharTermAttribute tokenTerm = stream.addAttribute(CharTermAttribute.class);

        // The type the token
        final TypeAttribute tokenType = stream.addAttribute(TypeAttribute.class);

        // Whether the token is a keyword. Keyword-aware TokenStreams/-Filters skip modification of tokens that are keywords
        final KeywordAttribute tokenKeyword = stream.addAttribute(KeywordAttribute.class);

        // The position of the token wrt the previous token
        final PositionIncrementAttribute tokenPositionIncrement = stream.addAttribute(PositionIncrementAttribute.class);

        // The number of positions occupied by a token
        final PositionLengthAttribute tokenPositionLength = stream.addAttribute(PositionLengthAttribute.class);

        // The start and end offset of a token in characters
        final OffsetAttribute tokenOffset = stream.addAttribute(OffsetAttribute.class);

        // Optional flags a token can have
        final SentenceAttribute sentenceAttribute = stream.addAttribute(SentenceAttribute.class);


        System.out.printf("####################################################################################%n");
        System.out.printf("Text to be processed%n");
        System.out.printf("+ %s%n%n", t);

        System.out.printf("Tokens%n");
        try {
            // Reset the stream before starting
            stream.reset();

            // Print all tokens until the stream is exhausted
            while (stream.incrementToken()) {
                System.out.printf("+ token: %s%n", tokenTerm.toString());
                if (printTokenInfo)
                {
                    System.out.printf("  - type: %s%n", tokenType.type());
                    System.out.printf("  - keyword: %b%n", tokenKeyword.isKeyword());
                    System.out.printf("  - position increment: %d%n", tokenPositionIncrement.getPositionIncrement());
                    System.out.printf("  - position length: %d%n", tokenPositionLength.getPositionLength());
                    System.out.printf("  - offset: [%d, %d]%n", tokenOffset.startOffset(), tokenOffset.endOffset());
                    System.out.printf("  - sentence index: %d%n", sentenceAttribute.getSentenceIndex());
                }
            }

            // Perform any end-of-stream operations
            stream.end();
        } finally {

            // Close the stream and release all the resources
            stream.close();
        }

        System.out.printf("%nElapsed time%n");
        System.out.printf("+ %d milliseconds%n", System.currentTimeMillis() - start);
        System.out.printf("####################################################################################%n");
    }
}
