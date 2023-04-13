package index;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import parse.ParsedDocument;

import java.io.Reader;

/**
 * Represents a {@link Field} for containing the id/number of a document.
 *
 * @version 1.00
 * @since 1.00
 */
public class IdField extends Field
{
    /**
     *  The type of the document body field
      */
    private static final FieldType ID_TYPE = new FieldType();

    static {
        ID_TYPE.setStored(true);
    }

    /**
     * Create a new field for the id/number of a document.
     *
     * @param value the contents of the id/number of a document.
     */
    public IdField(final Reader value) {
        super(ParsedDocument.FIELDS.ID, value, ID_TYPE);
    }

    /**
     * Create a new field for the id/number of a document.
     *
     * @param value the contents of the id/number of a document.
     */
    public IdField(final String value) {
        super(ParsedDocument.FIELDS.ID, value, ID_TYPE);
    }
}
