package topic;

import parse.LongEvalParser;
import parse.ParsedDocument;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

public class LongEvalTopicReader implements Iterator<LongEvalTopic>, Iterable<LongEvalTopic>{

    /**
     * XML reader from which topics will be retrieved.
     */
    private XMLStreamReader xmlIn;

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
        //Get XMLInputFactory instance.
        XMLInputFactory xmlInputFactory =
                XMLInputFactory.newInstance();

        //Create XMLStreamReader object.
        try {
            xmlIn = xmlInputFactory.createXMLStreamReader(in);
        } catch (XMLStreamException e) {
            throw new IOException("Unable to create XML reader for topics file.");
        }
    }

    @Override
    public Iterator<LongEvalTopic> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        LongEvalTopic actualTopic = null;
        int xmlEvent;
        boolean foundTopic = false;
        try {
            while (xmlIn.hasNext() && !foundTopic) {
                int eventType = xmlIn.next();

                if (eventType == XMLStreamReader.START_ELEMENT && xmlIn.getLocalName().equals("top"))
                    actualTopic = new LongEvalTopic();
                else if (eventType == XMLStreamReader.START_ELEMENT && xmlIn.getLocalName().equals("num"))
                    actualTopic.setNum(readCharacters(xmlIn));
                else if (eventType == XMLStreamReader.START_ELEMENT && xmlIn.getLocalName().equals("title"))
                    actualTopic.setTitle(readCharacters(xmlIn));
                else if (eventType == XMLStreamReader.END_ELEMENT && xmlIn.getLocalName().equals("top")) {
                    this.topic = actualTopic;
                    foundTopic = true;
                }
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException("Unable to process topics document: " + e.getMessage()); //TODO: RuntimeException?
        }
        // TODO: it's not working, is not processing XML documents with more than 1 root
        return foundTopic;
    }

    private String readCharacters(XMLStreamReader reader) throws XMLStreamException {
        StringBuilder result = new StringBuilder();
        while (reader.hasNext()) {
            int eventType = reader.next();
            switch (eventType) {
                case XMLStreamReader.CHARACTERS:
                case XMLStreamReader.CDATA:
                    result.append(reader.getText());
                    break;
                case XMLStreamReader.END_ELEMENT:
                    return result.toString();
            }
        }
        throw new XMLStreamException("Premature end of file");
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
