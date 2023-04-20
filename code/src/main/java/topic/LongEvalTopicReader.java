package topic;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

public class LongEvalTopicReader implements Iterator<LongEvalTopic>, Iterable<LongEvalTopic>{

    /**
     * XML reader from which topics will be retrieved.
     */
    private Reader in;

    /**
     * Indicates whether there is another {@code LongEvalTopic} to return.
     */
    private boolean next = true;

    /**
     * The current topic.
     */
    private LongEvalTopic topic = null;

    /**
     * TODO
     * @param in
     * @throws IOException
     */
    public LongEvalTopicReader (Reader in) throws IOException {
        this.in = in;
    }

    @Override
    public Iterator<LongEvalTopic> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        // TODO: set next variable to true (if there are more topics to read), false (if there aren't more topics to read)
        // TODO: save into variable 'topic' the document to be returned by the Iterator
        return false;
    }

    @Override
    public LongEvalTopic next() {
        return topic;
    }

    /**
     * Main method of the class. Just for testing purposes.
     *
     * @param args command line arguments.
     * @throws Exception if something goes wrong while indexing.
     */
    public static void main(String[] args) throws Exception {

        final String FILE_NAME = "C:\\longeval_train\\publish\\English\\Queries\\heldout.trec";
        Reader reader = new FileReader(FILE_NAME);

        LongEvalTopicReader topicReader = new LongEvalTopicReader(reader);

        System.out.printf("Starting topic reading at %s %n", FILE_NAME);
        int count = 0;
        for (LongEvalTopic t : topicReader) {
            //System.out.printf("%n%n------------------------------------%n%s%n%n%n", d.toString());
            System.out.printf("Topic read %s %n", t);
            count++;
        }
        System.out.printf("Number of read topics: %d", count);
    }
}
