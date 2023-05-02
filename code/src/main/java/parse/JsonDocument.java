package parse;

/**
 * A java POJO needed to convert JSON strings into Java objects.
 *
 * @version 1.00
 * @since 1.00
 */
public class JsonDocument
{
    private String id;
    private String contents;

    public String getId() {
        return id;
    }

    public String getContents() {
        return contents;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
}
