package parse;

import java.util.List;

/**
 * Defines the structure of a .trec documents
 */
public class TrecDocument {

    private List<String> num;

    private List<String> title;


    public List<String> getNum() {
        return num;
    }

    public void setNum(List<String> num) {
        this.num = num;
    }

    public List<String> getTitle() {
        return title;
    }

    public void setTitle(List<String> title) {
        this.title = title;
    }
}
