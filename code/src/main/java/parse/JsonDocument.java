package parse;

/**
 * A java POJO needed to convert JSON strings into Java objects.
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
