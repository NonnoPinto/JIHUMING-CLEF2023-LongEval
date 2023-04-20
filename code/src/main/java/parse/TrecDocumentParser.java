package parse;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


import org.apache.lucene.benchmark.quality.trec.TrecTopicsReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class TrecDocumentParser{

    protected String in;
    protected boolean next = true;

    TrecDocument document = new TrecDocument();
    /**
     * Creates a new document parser.
     *
     * @param in the reader to the document(s) to be parsed.
     * @throws NullPointerException if {@code in} is {@code null}.
     */
    protected TrecDocumentParser(String in) throws IOException {

        if (in == null) {
            throw new NullPointerException("Reader cannot be null.");
        }
        this.in = in;


        //Code ideas taken from: https://howtodoinjava.com/java/xml/parse-string-to-xml-dom/

        Path filepath = Path.of(this.in);
        String input = Files.readString(filepath);
        //Adding keys at the top and at the end to parse file as xml
        String xmlDoc = "<in>"+input+"</in>";

        //Parser that produces DOM object trees from XML content
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        //API to obtain DOM Document instance
        DocumentBuilder builder = null;
        try {
            //Create DocumentBuilder with default configuration
            builder = factory.newDocumentBuilder();

            //Parse the content to Document object
            Document doc = builder.parse(new InputSource(new StringReader(xmlDoc)));

            //Gets the list of num elements
            NodeList nodeList = doc.getElementsByTagName("top");

            System.out.println("Number of queries in trec file: "+nodeList.getLength());

            //Lists of num and titles of trec file
            List<String> num = new ArrayList<>();
            List<String> title = new ArrayList<>();

            //Foreach node of xml file it tales and add to lists values
            for(int i = 0; i < nodeList.getLength(); i++){
                Node node = nodeList.item(i);
                System.out.println(node);
                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) node;

                    num.add(eElement.getElementsByTagName("num").item(0).getTextContent());
                    title.add(eElement.getElementsByTagName("title").item(0).getTextContent());
                }
            }

            this.document.setNum(num);
            this.document.setTitle(title);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    protected TrecDocument parse(){

        return document;
    }

    public static void main(String[] args) throws Exception {

        final String FILE_NAME = "C:\\Users\\Lenovo\\Desktop\\seupd2223-jihuming\\heldout.trec";
        Reader reader = new FileReader(
                FILE_NAME);

        TrecDocumentParser p = new TrecDocumentParser(FILE_NAME);
        var document = p.parse();


        System.out.printf("Starting document parsing at %s %n", FILE_NAME);

        //Test to pars all queries in trec file
        int i = 0;
        for(i = 0; i  < document.getNum().size(); i++){
            System.out.println("NUM: "+document.getNum().get(i).toString());
            System.out.println("TITLE: "+document.getTitle().get(i).toString());
            System.out.println("****");
        }
        System.out.printf("Number of parsed queries: %d", i);
    }
}
