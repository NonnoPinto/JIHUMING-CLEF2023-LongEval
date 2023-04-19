package search;

import analyze.NGramAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.benchmark.quality.QualityQuery;
import org.apache.lucene.benchmark.quality.trec.TrecTopicsReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import parse.ParsedDocument;
import topic.LongEvalTopic;
import topic.LongEvalTopicReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Searches a document collection.
 *
 * @author Nicola Ferro
 * @version 1.00
 * @since 1.00
 */
public class Searcher {

    /**
     * Fiends of our topics/queries. Note that LongEval only provides a query title.
     */
    private static final class TOPIC_FIELDS {

        /**
         * The number of a topic
         */
        private static final String NUM = "num";

        /**
         * The title of a topic.
         */
        public static final String TITLE = "title";
    }

    /**
     * The identifier of the run
     */
    private final String runID;

    /**
     * The run to be written
     */
    private final PrintWriter run;

    /**
     * The index reader
     */
    private final IndexReader reader;

    /**
     * The index searcher.
     */
    private final IndexSearcher searcher;

    /**
     * The topics to be searched.
     */
    private final List<LongEvalTopic> topics;

    /**
     * Query parser for English queries.
     */
    private final QueryParser enQp;

    /**
     * Query parser for French queries.
     */
    private final QueryParser frQp;

    /**
     * Query parser to generate N-Grams for both English and French queries.
     */
    private final QueryParser ngramQp;

    /**
     * The maximum number of documents to retrieve
     */
    private final int maxDocsRetrieved;

    /**
     * The total elapsed time.
     */
    private long elapsedTime = Long.MIN_VALUE;


    /**
     * Creates a new searcher.
     *
     * @param enAnalyzer      the {@code Analyzer} used for the English documents.
     * @param frAnalyzer      the {@code Analyzer} used for the French documents.
     * @param ngramAnalyzer   the {@code Analyzer} used for N-Gram field of documents.
     * @param similarity       the {@code Similarity} to be used.
     * @param indexPath        the directory where containing the index to be searched.
     * @param topicsFile       the file containing the topics to search for.
     * @param expectedTopics   the total number of topics expected to be searched.
     * @param runID            the identifier of the run to be created.
     * @param runPath          the path where to store the run.
     * @param maxDocsRetrieved the maximum number of documents to be retrieved.
     * @throws NullPointerException     if any of the parameters is {@code null}.
     * @throws IllegalArgumentException if any of the parameters assumes invalid values.
     */
    public Searcher(final Analyzer enAnalyzer, final Analyzer frAnalyzer, final Analyzer ngramAnalyzer,
                    final Similarity similarity, final String indexPath, final String topicsFile,
                    final int expectedTopics, final String runID, final String runPath, final int maxDocsRetrieved) {
        // enAnalyzer
        if (enAnalyzer == null) {
            throw new NullPointerException("English analyzer cannot be null.");
        }

        // frAnalyzer
        if (frAnalyzer == null) {
            throw new NullPointerException("French analyzer cannot be null.");
        }

        // nAnalyzer
        if (ngramAnalyzer == null) {
            throw new NullPointerException("N-Gram analyzer cannot be null.");
        }

        // similarity
        if (similarity == null) {
            throw new NullPointerException("Similarity cannot be null.");
        }

        // indexPath
        if (indexPath == null) {
            throw new NullPointerException("Index path cannot be null.");
        }
        if (indexPath.isEmpty()) {
            throw new IllegalArgumentException("Index path cannot be empty.");
        }

        final Path indexDir = Paths.get(indexPath);
        if (!Files.isReadable(indexDir)) {
            throw new IllegalArgumentException(
                    String.format("Index directory %s cannot be read.", indexDir.toAbsolutePath().toString()));
        }

        if (!Files.isDirectory(indexDir)) {
            throw new IllegalArgumentException(String.format("%s expected to be a directory where to search the index.",
                                                             indexDir.toAbsolutePath().toString()));
        }

        try {
            reader = DirectoryReader.open(FSDirectory.open(indexDir));
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Unable to create the index reader for directory %s: %s.",
                                                             indexDir.toAbsolutePath().toString(), e.getMessage()), e);
        }

        searcher = new IndexSearcher(reader);
        searcher.setSimilarity(similarity);

        // topicsFile
        if (topicsFile == null) {
            throw new NullPointerException("Topics file cannot be null.");
        }
        if (topicsFile.isEmpty()) {
            throw new IllegalArgumentException("Topics file cannot be empty.");
        }

        try {
            BufferedReader in = Files.newBufferedReader(Paths.get(topicsFile), StandardCharsets.UTF_8);

            // Reading all the topics/queries
            LongEvalTopicReader tReader = new LongEvalTopicReader(in);
            topics = new ArrayList<>();
            for (LongEvalTopic t : tReader)
                topics.add(t);

            in.close();
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    String.format("Unable to process topic file %s: %s.", topicsFile, e.getMessage()), e);
        }

        // expectedTopics
        if (expectedTopics <= 0) {
            throw new IllegalArgumentException(
                    "The expected number of topics to be searched cannot be less than or equal to zero.");
        }

        if (topics.size() != expectedTopics) {
            System.out.printf("Expected to search for %s topics; %s topics found instead.", expectedTopics,
                              topics.size());
        }

        /*
            A query parser contains information about:
                - The document field to search the query.
                - The analyzer to process the query before searching.
         */
        // English query parser
        enQp = new QueryParser(ParsedDocument.FIELDS.ENGLISH_BODY, enAnalyzer);
        // French query parser
        frQp = new QueryParser(ParsedDocument.FIELDS.FRENCH_BODY, frAnalyzer);
        // N-Gram query parser
        ngramQp = new QueryParser(ParsedDocument.FIELDS.N_GRAM, ngramAnalyzer);

        if (runID == null) {
            throw new NullPointerException("Run identifier cannot be null.");
        }

        if (runID.isEmpty()) {
            throw new IllegalArgumentException("Run identifier cannot be empty.");
        }

        this.runID = runID;

        // runPath
        if (runPath == null) {
            throw new NullPointerException("Run path cannot be null.");
        }
        if (runPath.isEmpty()) {
            throw new IllegalArgumentException("Run path cannot be empty.");
        }

        final Path runDir = Paths.get(runPath);
        if (!Files.isWritable(runDir)) {
            throw new IllegalArgumentException(
                    String.format("Run directory %s cannot be written.", runDir.toAbsolutePath().toString()));
        }

        if (!Files.isDirectory(runDir)) {
            throw new IllegalArgumentException(String.format("%s expected to be a directory where to write the run.",
                                                             runDir.toAbsolutePath().toString()));
        }

        Path runFile = runDir.resolve(runID + ".txt");
        try {
            run = new PrintWriter(Files.newBufferedWriter(runFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                                                          StandardOpenOption.TRUNCATE_EXISTING,
                                                          StandardOpenOption.WRITE));
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    String.format("Unable to open run file %s: %s.", runFile.toAbsolutePath(), e.getMessage()), e);
        }

        // maxDocsRetrieved
        if (maxDocsRetrieved <= 0) {
            throw new IllegalArgumentException(
                    "The maximum number of documents to be retrieved cannot be less than or equal to zero.");
        }
        this.maxDocsRetrieved = maxDocsRetrieved;
    }

    /**
     * Returns the total elapsed time.
     *
     * @return the total elapsed time.
     */
    public long getElapsedTime() {
        return elapsedTime;
    }

    /**
     * /** Searches for the specified topics.
     *
     * @throws IOException    if something goes wrong while searching.
     * @throws ParseException if something goes wrong while parsing topics.
     */
    public void search() throws IOException, ParseException {

        System.out.printf("%n#### Start searching ####%n");

        // the start time of the searching
        final long start = System.currentTimeMillis();

        final Set<String> idField = new HashSet<>();
        idField.add(ParsedDocument.FIELDS.ID);

        BooleanQuery.Builder bq = null;
        Query q = null;
        TopDocs docs = null;
        ScoreDoc[] sd = null;
        String docID = null;

        try {

            for (LongEvalTopic t : topics) {

                System.out.printf("Searching for topic %s.%n", t.getNum());

                bq = new BooleanQuery.Builder();

                // TODO: (maybe) detect if the query is English or French and search only either on ENGLISH_BODY or FRENCH_BODY
                bq.add(enQp.parse(QueryParserBase.escape(t.getTitle())), BooleanClause.Occur.SHOULD);
                bq.add(frQp.parse(QueryParserBase.escape(t.getTitle())), BooleanClause.Occur.SHOULD);
                // Always in N_GRAM field
                bq.add(ngramQp.parse(QueryParserBase.escape(t.getTitle())), BooleanClause.Occur.SHOULD);

                q = bq.build();

                docs = searcher.search(q, maxDocsRetrieved);

                sd = docs.scoreDocs;

                for (int i = 0, n = sd.length; i < n; i++) {
                    docID = reader.document(sd[i].doc, idField).get(ParsedDocument.FIELDS.ID);

                    run.printf(Locale.ENGLISH, "%s\tQ0\t%s\t%d\t%.6f\t%s%n", t.getNum(), docID, i, sd[i].score,
                               runID);
                }

                run.flush();

            }
        } finally {
            run.close();

            reader.close();
        }

        elapsedTime = System.currentTimeMillis() - start;

        System.out.printf("%d topic(s) searched in %d seconds.%n", topics.size(), elapsedTime / 1000);

        System.out.printf("#### Searching complete ####%n");
    }

    /**
     * Main method of the class. Just for testing purposes.
     *
     * @param args command line arguments.
     * @throws Exception if something goes wrong while indexing.
     */
    public static void main(String[] args) throws Exception {

        //all paths to write
        final String topics = "C:\\longeval_train\\app_test\\Searcher\\train.trec";

        final String indexPath = "created_indexes/multilingual-index-stop-stem";

        final String runPath = "runs";

        final String runID = "run00001";

        final int maxDocsRetrieved = 100;

        final Analyzer a = CustomAnalyzer.builder()
                .withTokenizer(StandardTokenizerFactory.class)
                .addTokenFilter(LowerCaseFilterFactory.class)
                .addTokenFilter(StopFilterFactory.class)
                .addTokenFilter(PorterStemFilterFactory.class).build();

        // final EnglishAnalyzer enAn = new EnglishAnalyzer(); //TODO: uncomment when EnglishAnalyzer is ready
        // final FrenchAnalyzer frAn = new FrenchAnalyzer(); //TODO: uncomment when FrenchAnalyzer is ready
        final NGramAnalyzer ngramAn = new NGramAnalyzer();

        Searcher s = new Searcher(a, a, ngramAn, new BM25Similarity(), indexPath, topics, 50,
                runID, runPath, maxDocsRetrieved);

        s.search();
    }
}
