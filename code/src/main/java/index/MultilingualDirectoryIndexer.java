package index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import parse.DocumentParser;
import parse.ParsedDocument;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Indexes two versions of the same documents (in English and in French) processing a whole directory tree.
 * It will create documents with their ID (field), their ENGLISH BODY (field) and their FRENCH BODY (field).
 *
 * @version 1.00
 * @since 1.00
 */
public class MultilingualDirectoryIndexer {

    // One megabyte
    private static final int MBYTE = 1024 * 1024;

    // The index writer
    private final IndexWriter writer;

    // The class of the {@code DocumentParser} to be used.
    private final Class<? extends DocumentParser> dpCls;

    // The directory where the index is stored.
    private final Path indexDir;

    // The directory (and sub-directories) where English documents are stored.
    private final Path enDocsDir;

    // The directory (and sub-directories) where French documents are stored.
    private final Path frDocsDir;

    // The extension of the files to be indexed.
    private final String extension;

    // The charset used for encoding documents.
    private final Charset cs;

    /**
     * The total number of documents expected to be indexed. For every document there will be two versions (English and
     * French), the count is 1, not 2.
     */
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
     * @param enDocsPath      the directory from which English documents have to be read.
     * @param frDocsPath      the directory from which French documents have to be read.
     * @param extension       the extension of the files to be indexed.
     * @param charsetName     the name of the charset used for encoding documents.
     * @param expectedDocs    the total number of documents expected to be indexed
     * @param dpCls           the class of the {@code DocumentParser} to be used.
     * @throws NullPointerException     if any of the parameters is {@code null}.
     * @throws IllegalArgumentException if any of the parameters assumes invalid values.
     */
    public MultilingualDirectoryIndexer(final Analyzer analyzer, final Similarity similarity, final int ramBufferSizeMB,
                                        final String indexPath, final String enDocsPath, final String frDocsPath,
                                        final String extension, final String charsetName, final long expectedDocs,
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

        // enDocsPath
        if (enDocsPath == null) {
            throw new NullPointerException("English documents path cannot be null.");
        }
        if (enDocsPath.isEmpty()) {
            throw new IllegalArgumentException("English documents path cannot be empty.");
        }
        final Path enDocsDir = Paths.get(enDocsPath);
        if (!Files.isReadable(enDocsDir)) {
            throw new IllegalArgumentException(
                    String.format("English documents directory %s cannot be read.", enDocsDir.toAbsolutePath()));
        }
        if (!Files.isDirectory(enDocsDir)) {
            throw new IllegalArgumentException(
                    String.format("%s expected to be a directory of documents.", enDocsDir.toAbsolutePath()));
        }
        this.enDocsDir = enDocsDir;

        // frDocsPath
        if (frDocsPath == null) {
            throw new NullPointerException("French documents path cannot be null.");
        }
        if (frDocsPath.isEmpty()) {
            throw new IllegalArgumentException("French documents path cannot be empty.");
        }
        final Path frDocsDir = Paths.get(frDocsPath);
        if (!Files.isReadable(frDocsDir)) {
            throw new IllegalArgumentException(
                    String.format("French documents directory %s cannot be read.", frDocsDir.toAbsolutePath()));
        }
        if (!Files.isDirectory(frDocsDir)) {
            throw new IllegalArgumentException(
                    String.format("%s expected to be a directory of documents.", frDocsDir.toAbsolutePath()));
        }
        this.frDocsDir = frDocsDir;

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

        // Create an iterator to access every file in the English directory (enFileIterator)
        File[] enFiles = enDocsDir.toFile().listFiles();
        Iterator<File> enFileIterator;
        if (enFiles != null) {
            enFileIterator = Arrays.stream(enFiles).iterator();
        } else {
            throw new RuntimeException("List of files in English documents directory is null");
        }

        // Create an iterator to access every file in the French directory (frFileIterator)
        File[] frFiles = frDocsDir.toFile().listFiles();
        Iterator<File> frFileIterator;
        if (frFiles != null) {
            frFileIterator = Arrays.stream(frFiles).iterator();
        } else {
            throw new RuntimeException("List of files in French documents directory is null");
        }

        while (enFileIterator.hasNext() && frFileIterator.hasNext())
        {
            File enFile = enFileIterator.next();
            File frFile = frFileIterator.next();

            bytesCount += Files.size(enFile.toPath()) + Files.size(frFile.toPath());
            filesCount += 2;

            // Create a document parser for English documents
            DocumentParser enDp = DocumentParser.create(dpCls, Files.newBufferedReader(enFile.toPath(), cs));

            // Create a document parser for French documents
            DocumentParser frDp = DocumentParser.create(dpCls, Files.newBufferedReader(frFile.toPath(), cs));

            // Create an iterator for the English documents
            Iterator<ParsedDocument> enParDocIterator = enDp.iterator();

            // Create an iterator for the French documents
            Iterator<ParsedDocument> frParDocIterator = frDp.iterator();

            while (enParDocIterator.hasNext() && frParDocIterator.hasNext())
            {
                ParsedDocument enParDoc = enParDocIterator.next();
                ParsedDocument frParDoc = frParDocIterator.next();

                if (!enParDoc.getIdentifier().equals(frParDoc.getIdentifier()))
                {
                    throw new RuntimeException("English and French versions of a document don't have the same ID");
                }

                Document doc = new Document();

                // add the document identifier
                doc.add(new IdField(enParDoc.getIdentifier()));

                // add the English document body
                doc.add(new EnglishBodyField(enParDoc.getBody()));

                // add the French document body
                doc.add(new EnglishBodyField(frParDoc.getBody()));

                writer.addDocument(doc);

                docsCount++;

                // print progress every 10000 indexed documents
                if (docsCount % 10000 == 0) {
                    System.out.printf("%d document(s) in both languages (%d files, %d Mbytes) indexed in %d seconds.%n",
                            docsCount, filesCount, bytesCount / MBYTE,
                            (System.currentTimeMillis() - start) / 1000);
                }
            }
        }
        
        writer.commit();
        writer.close();

        if (docsCount != expectedDocs) {
            System.out.printf("Expected to index %d documents; %d indexed instead.%n", expectedDocs, docsCount);
        }

        System.out.printf("%d document(s) (%d files, %d Mbytes) indexed in %d seconds.%n", docsCount, filesCount,
                bytesCount / MBYTE, (System.currentTimeMillis() - start) / 1000);

        System.out.printf("#### Indexing complete ####%n");
    }
}
