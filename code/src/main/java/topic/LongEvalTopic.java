package topic;

import org.apache.lucene.benchmark.quality.trec.TrecTopicsReader;

import java.util.Objects;

/**
 * Java POJO representing each topic provided by LongEval. Each topic has a number (<num></num>) and a title
 * (<title></title>).
 *
 * We had to create this class and {@link LongEvalTopicReader} because {@link TrecTopicsReader} is not processing
 * properly the topics provided by LongEval.
 *
 * @version 1.00
 * @since 1.00
 */
public class LongEvalTopic {

    private String num;
    private String title;

    public LongEvalTopic()
    {
        this.num = null;
        this.title = null;
    }

    public LongEvalTopic(String num, String title)
    {
        this.num = num;
        this.title = title;
    }

    public String getNum() {
        return num;
    }

    public String getTitle() {
        return title;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LongEvalTopic that = (LongEvalTopic) o;
        return num.equals(that.num);
    }

    @Override
    public int hashCode() {
        return Objects.hash(num);
    }

    @Override
    public String toString() {
        return "LongEvalTopic(" +
                "num='" + num + '\'' +
                ", title='" + title + '\'' +
                ')';
    }
}
