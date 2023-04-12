package index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import parse.ParsedDocument;

import javax.swing.text.html.parser.DocumentParser;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Indexes documents processing a whole directory tree.
 * Some elements of the code are taken by the project hello-tipster by Nicola Ferro.
 *
 * @author Jesús Moncada Ramírez
 * @version 1.00
 * @since 1.00
 */
public class DirectoryIndexer {

    // One megabyte
    private static final int MBYTE = 1024 * 1024;

    // The index writer
    private final IndexWriter writer;

    // The class of the {@code DocumentParser} to be used.
    private final Class<? extends DocumentParser> dpCls;

    // The directory where the index is stored.
    private final Path indexDir;

    // The directory (and sub-directories) where documents are stored.
    private final Path docsDir;

    // The extension of the files to be indexed.
    private final String extension;

    // The charset used for encoding documents.
    private final Charset cs;

    // The total number of documents expected to be indexed.
    private final long expectedDocs;

    // The start instant of the indexing.
    private final long start;

    // The total number of indexed files.
    private long filesCount;

    // The total number of indexed documents.
    private long docsCount;

    // The total number of indexed bytes
    private long bytesCount;

    /**
     * Creates a new indexer.
     *
     * @param analyzer        the {@code Analyzer} to be used.
     * @param similarity      the {@code Similarity} to be used.
     * @param ramBufferSizeMB the size in megabytes of the RAM buffer for indexing documents.
     * @param indexPath       the directory where to store the index.
     * @param docsPath        the directory from which documents have to be read.
     * @param extension       the extension of the files to be indexed.
     * @param charsetName     the name of the charset used for encoding documents.
     * @param expectedDocs    the total number of documents expected to be indexed
     * @param dpCls           the class of the {@code DocumentParser} to be used.
     * @throws NullPointerException     if any of the parameters is {@code null}.
     * @throws IllegalArgumentException if any of the parameters assumes invalid values.
     */
    public DirectoryIndexer(final Analyzer analyzer, final Similarity similarity, final int ramBufferSizeMB,
                            final String indexPath, final String docsPath, final String extension,
                            final String charsetName, final long expectedDocs,
                            final Class<? extends DocumentParser> dpCls) {
        // dpCls
        if (dpCls == null) {
            throw new NullPointerException("Document parser class cannot be null.");
        }
        this.dpCls = dpCls;

        // analyzer
        if (analyzer == null) {
            throw new NullPointerException("Analyzer cannot be null.");
        }

        // similarity
        if (similarity == null) {
            throw new NullPointerException("Similarity cannot be null.");
        }

        // ramBufferSizeMB
        if (ramBufferSizeMB <= 0) {
            throw new IllegalArgumentException("RAM buffer size cannot be less than or equal to zero.");
        }

        final IndexWriterConfig indexConfig = new IndexWriterConfig(analyzer);
        indexConfig.setSimilarity(similarity);
        indexConfig.setRAMBufferSizeMB(ramBufferSizeMB);
        indexConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        indexConfig.setCommitOnClose(true);
        indexConfig.setUseCompoundFile(true);

        // indexPath
        if (indexPath == null) {
            throw new NullPointerException("Index path cannot be null.");
        }
        if (indexPath.isEmpty()) {
            throw new IllegalArgumentException("Index path cannot be empty.");
        }

        final Path indexDir = Paths.get(indexPath);
        // if the directory does not already exist, create it
        if (Files.notExists(indexDir)) {
            try {
                Files.createDirectory(indexDir);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        String.format("Unable to create directory %s: %s.", indexDir.toAbsolutePath(),
                                e.getMessage()), e);
            }
        }
        if (!Files.isWritable(indexDir)) {
            throw new IllegalArgumentException(
                    String.format("Index directory %s cannot be written.", indexDir.toAbsolutePath()));
        }
        if (!Files.isDirectory(indexDir)) {
            throw new IllegalArgumentException(String.format("%s expected to be a directory where to write the index.",
                    indexDir.toAbsolutePath()));
        }
        this.indexDir = indexDir;

        // docsPath
        if (docsPath == null) {
            throw new NullPointerException("Documents path cannot be null.");
        }
        if (docsPath.isEmpty()) {
            throw new IllegalArgumentException("Documents path cannot be empty.");
        }

        final Path docsDir = Paths.get(docsPath);
        if (!Files.isReadable(docsDir)) {
            throw new IllegalArgumentException(
                    String.format("Documents directory %s cannot be read.", docsDir.toAbsolutePath()));
        }
        if (!Files.isDirectory(docsDir)) {
            throw new IllegalArgumentException(
                    String.format("%s expected to be a directory of documents.", docsDir.toAbsolutePath()));
        }
        this.docsDir = docsDir;

        // extension
        if (extension == null) {
            throw new NullPointerException("File extension cannot be null.");
        }
        if (extension.isEmpty()) {
            throw new IllegalArgumentException("File extension cannot be empty.");
        }
        this.extension = extension;

        // charsetName
        if (charsetName == null) {
            throw new NullPointerException("Charset name cannot be null.");
        }
        if (charsetName.isEmpty()) {
            throw new IllegalArgumentException("Charset name cannot be empty.");
        }

        try {
            cs = Charset.forName(charsetName);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("Unable to create the charset %s: %s.", charsetName, e.getMessage()), e);
        }

        // expectedDocs
        if (expectedDocs <= 0) {
            throw new IllegalArgumentException(
                    "The expected number of documents to be indexed cannot be less than or equal to zero.");
        }
        this.expectedDocs = expectedDocs;

        // Set all the counts to 0
        this.docsCount = 0;
        this.bytesCount = 0;
        this.filesCount = 0;

        // Create the IndexWritter object
        try {
            writer = new IndexWriter(FSDirectory.open(indexDir), indexConfig);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Unable to create the index writer in directory %s: %s.",
                    indexDir.toAbsolutePath(), e.getMessage()), e);
        }

        this.start = System.currentTimeMillis();
    }

    /**
     * Indexes the documents.
     *
     * @throws IOException if something goes wrong while indexing.
     */
    public void index() throws IOException {

        System.out.printf("%n#### Start indexing ####%n");

        Files.walkFileTree(docsDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().endsWith(extension)) {

                    DocumentParser dp = DocumentParser.create(dpCls, Files.newBufferedReader(file, cs));

                    bytesCount += Files.size(file);
                    filesCount += 1;

                    Document doc = null;

                    // TODO: decide the structure of our ParsedDocuments and our Documents
                    for (ParsedDocument pd : dp) {

                        doc = new Document();

                        // add the document identifier
                        //doc.add(new StringField(ParsedDocument.FIELDS.ID, pd.getIdentifier(), Field.Store.YES));
                        doc.add(new IdField(pd.getIdentifier()));

                        // add the document body
                        doc.add(new BodyField(pd.getBody()));

                        writer.addDocument(doc);

                        docsCount++;

                        // print progress every 10000 indexed documents
                        if (docsCount % 10000 == 0) {
                            System.out.printf("%d document(s) (%d files, %d Mbytes) indexed in %d seconds.%n",
                                    docsCount, filesCount, bytesCount / MBYTE,
                                    (System.currentTimeMillis() - start) / 1000);
                        }

                    }

                }
                return FileVisitResult.CONTINUE;
            }
        });

        writer.commit();
        writer.close();

        if (docsCount != expectedDocs) {
            System.out.printf("Expected to index %d documents; %d indexed instead.%n", expectedDocs, docsCount);
        }

        System.out.printf("%d document(s) (%d files, %d Mbytes) indexed in %d seconds.%n", docsCount, filesCount,
                bytesCount / MBYTE, (System.currentTimeMillis() - start) / 1000);

        System.out.printf("#### Indexing complete ####%n");
    }

    /**
     * Prints the inverted index to the console.
     *
     * @author Nicola Ferro
     * @throws IOException if something goes wrong while accessing the index.
     * //TODO: need to be tested
     */
    public void printInvertedIndex() throws IOException {

        System.out.printf("%n------------- PRINTING THE INVERTED INDEX -------------%n");

        // Open the directory in Lucene
        final Directory dir = FSDirectory.open(indexDir);

        // Open the index
        final IndexReader index = DirectoryReader.open(dir);

        // The inverted index. Keys are terms; values are lists of (docID, term frequency) pairs
        final Map<String, List<Map.Entry<String, Long>>> invertedIndex = new TreeMap<>();

        // Iterate over each document in the index
        for (int i = 0, docs = index.numDocs(); i < docs; i++) {

            // Read the document and get its identifier
            String docID = index.document(i).get(ParsedDocument.FIELDS.ID);

            // Get the vector of terms for that document
            Terms terms = index.getTermVector(i, ParsedDocument.FIELDS.BODY);

            // Get an iterator over the vector of terms
            TermsEnum termsEnum = terms.iterator();

            // Iterate until there are terms
            for (BytesRef term = termsEnum.next(); term != null; term = termsEnum.next()) {

                // Get the text string of the term
                String termstr = term.utf8ToString();

                // Get the total frequency of the term
                long freq = termsEnum.totalTermFreq();

                // Create a new (docID, term frequency) pair
                Map.Entry<String, Long> entry = new AbstractMap.SimpleEntry<>(docID, freq);

                // update the inverted index with the new entry
                invertedIndex.compute(termstr, (k, v) -> {

                    // if the term is not already in the index, create a new list for its pairs
                    if (v==null) {
                        v = new ArrayList<>();
                    }

                    // add the pair to the list
                    v.add(entry);

                    // return the updated list
                    return v;
                });
            }
        }

        // close the index and the directory
        index.close();
        dir.close();

        // Print the inverted index to the console
        invertedIndex.forEach( (k, v) -> {

            System.out.printf("+ %s ->", k);

            v.forEach((e) -> {
                System.out.printf(" %s:%d", e.getKey(), e.getValue());
            });

            System.out.printf("%n");

        });

        System.out.printf("-------------------------------------------------------%n");
    }

    /**
     * Prints statistics about the vocaulary to the console.
     *
     * @throws IOException if something goes wrong while accessing the index.
     * //TODO: need to be tested
     */
    public void printVocabularyStatistics() throws IOException {

        System.out.printf("%n------------- PRINTING VOCABULARY STATISTICS -------------%n");

        // Open the directory in Lucene
        final Directory dir = FSDirectory.open(indexDir);

        // Open the index - we need to get a LeafReader to be able to directly access terms
        final LeafReader index = DirectoryReader.open(dir).leaves().get(0).reader();

        // Total number of documents in the collection
        System.out.printf("+ Total number of documents: %d%n", index.numDocs());

        // Get the vocabulary of the index.
        final Terms voc = index.terms(ParsedDocument.FIELDS.BODY);

        // Total number of unique terms in the collection
        System.out.printf("+ Total number of unique terms: %d%n", voc.size());

        // Total number of terms in the collection
        System.out.printf("+ Total number of terms: %d%n", voc.getSumTotalTermFreq());

        // Get an iterator over the vector of terms in the vocabulary
        final TermsEnum termsEnum = voc.iterator();

        // Iterate until there are terms
        System.out.printf("+ Vocabulary:%n");
        System.out.printf("  - %-20s%-5s%-5s%n", "TERM", "DF", "FREQ");
        for (BytesRef term = termsEnum.next(); term != null; term = termsEnum.next()) {

            // Get the text string of the term
            String termstr = term.utf8ToString();

            // Get the document frequency (DF) of the term
            int df = termsEnum.docFreq();

            // Get the total frequency of the term
            long freq = termsEnum.totalTermFreq();
            System.out.printf("  - %-20s%-5d%-5d%n", termstr, df, freq);
        }

        // close the index and the directory
        index.close();
        dir.close();

        System.out.printf("----------------------------------------------------------%n");
    }

    /**
     * Main method of the class. Just for testing purposes.
     *
     * @param args command line arguments.
     * @throws Exception if something goes wrong while indexing.
     */
    public static void main(String[] args) throws Exception
    {
        final int ramBuffer = 256;
        final String docsPath = "/"; //TODO
        final String indexPath = "experiment/index-stop-stem";

        final String extension = "txt";
        final int expectedDocs = 528155;
        final String charsetName = "ISO-8859-1";

        final Analyzer a = CustomAnalyzer.builder().withTokenizer(StandardTokenizerFactory.class).addTokenFilter(
                LowerCaseFilterFactory.class).addTokenFilter(StopFilterFactory.class).addTokenFilter(PorterStemFilterFactory.class).build();

        DirectoryIndexer i = new DirectoryIndexer(a, new BM25Similarity(), ramBuffer, indexPath, docsPath, extension,
                charsetName, expectedDocs, LongevalParser.class);

        i.index();
    }

}