package analyze;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;

/**
 * Lucene custom toke filter to remove tokens with empty text.
 *
 * @version 1.0
 * @since 1.0
 */
public class EmptyTokenFilter extends FilteringTokenFilter {

    private final CharTermAttribute termAtt =
            addAttribute(CharTermAttribute.class);

    protected EmptyTokenFilter(TokenStream input) {
        super(input);
    }

    @Override
    protected boolean accept() throws IOException {
        final int length = termAtt.length();
        return length != 0;
    }
}