package search;

import analyze.EnglishAnalyzer;
import analyze.FrenchAnalyzer;
import analyze.NERAnalyzer;
import analyze.NGramAnalyzer;
import opennlp.tools.parser.Parse;
import org.apache.lucene.analysis.Analyzer;
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
     * Query parser to generate NER information for queries.
     */
    private final QueryParser nerQp;

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
     * @param enAnalyzer       the {@code Analyzer} used for the English documents.
     * @param frAnalyzer       the {@code Analyzer} used for the French documents.
     * @param ngramAnalyzer    the {@code Analyzer} used for N-Gram field of
     *                         documents.
     * @param nerAnalyzer      the {@code Analyzer} to be used for NER extracted
     *                         information from documents.
     * @param similarity       the {@code Similarity} to be used.
     * @param indexPath        the directory where containing the index to be
     *                         searched.
     * @param topicsFile       the file containing the topics to search for.
     * @param expectedTopics   the total number of topics expected to be searched.
     * @param runID            the identifier of the run to be created.
     * @param runPath          the path where to store the run.
     * @param maxDocsRetrieved the maximum number of documents to be retrieved.
     * @throws NullPointerException     if any of the parameters is {@code null}.
     * @throws IllegalArgumentException if any of the parameters assumes invalid
     *                                  values.
     */
    public Searcher(final Analyzer enAnalyzer, final Analyzer frAnalyzer, final Analyzer ngramAnalyzer,
            final Analyzer nerAnalyzer,
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

        // nerAnalyzer
        if (nerAnalyzer == null) {
            throw new NullPointerException("NER analyzer cannot be null.");
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
            // Create the topic reader
            LongEvalTopicReader tReader = new LongEvalTopicReader(Paths.get(topicsFile));

            // Retrieve all the topics from the reader
            topics = tReader.read();
            // TODO: include control of expected topics (and warning)
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
         * A query parser contains information about:
         * - The document field to search the query.
         * - The analyzer to process the query before searching.
         */
        // English query parser
        enQp = new QueryParser(ParsedDocument.FIELDS.ENGLISH_BODY, enAnalyzer);
        // French query parser
        frQp = new QueryParser(ParsedDocument.FIELDS.FRENCH_BODY, frAnalyzer);
        // N-Gram query parser
        ngramQp = new QueryParser(ParsedDocument.FIELDS.N_GRAM, ngramAnalyzer);
        // NER query parser
        nerQp = new QueryParser(ParsedDocument.FIELDS.NER, nerAnalyzer);

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
     * @param runNumber run number that controls in which fields the search must be
     *                  done following the scheme.
     *                  See experimental evaluation in the report.
     *
     * @throws IOException    if something goes wrong while searching.
     * @throws ParseException if something goes wrong while parsing topics.
     */
    public void search(Integer runNumber) throws IOException, ParseException {

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

                // Control in which fields to search based on runNumber
                switch (runNumber) {

                case 1:
                    bq.add(enQp.parse(QueryParserBase.escape(t.getTitle())), BooleanClause.Occur.SHOULD);
                    break;
                case 2:
                    //TODO: add your code here
                    break;
                case 3:
                    //TODO: add your code here
                    break;
                case 4:
                    //TODO: add your code here
                    break;
                case 5:
                    //TODO: add your code here
                    break;
                case 6:
                    //TODO: add your code hereD);
                    break;
                case 7:
                    bq.add(frQp.parse(QueryParserBase.escape(t.getTitle())), BooleanClause.Occur.SHOULD);
                    break;
                case 8:
                    bq.add(frQp.parse(QueryParserBase.escape(t.getTitle())), BooleanClause.Occur.SHOULD);
                    bq.add(ngramQp.parse(QueryParserBase.escape(t.getTitle())), BooleanClause.Occur.SHOULD);
                    break;
                case 9:
                    bq.add(enQp.parse(QueryParserBase.escape(t.getTitle())), BooleanClause.Occur.SHOULD);
                    bq.add(frQp.parse(QueryParserBase.escape(t.getTitle())), BooleanClause.Occur.SHOULD);
                    bq.add(ngramQp.parse(QueryParserBase.escape(t.getTitle())), BooleanClause.Occur.SHOULD);
                    break;
                case 10:
                    //TODO: add your code here
                    break;
                default:
                    throw new IllegalArgumentException("Invalid run number.");
                }

                // Search the title in the English body field
                // bq.add(enQp.parse(QueryParserBase.escape(t.getTitle())),
                // BooleanClause.Occur.SHOULD);
                // Search the title in the French body field
                // bq.add(frQp.parse(QueryParserBase.escape(t.getTitle())),
                // BooleanClause.Occur.SHOULD);
                // Search the title in N-Gram field
                // bq.add(ngramQp.parse(QueryParserBase.escape(t.getTitle())),
                // BooleanClause.Occur.SHOULD);
                // Search the title in NER field
                // bq.add(nerQp.parse(QueryParserBase.escape(t.getTitle())),
                // BooleanClause.Occur.SHOULD);

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
        System.out.printf("Created file: %s.txt", runID);
        System.out.printf("#### Searching complete ####%n");
    }

    /**
     * Main method of the class. Just for testing purposes.
     *
     * @param args command line arguments.
     * @throws Exception if something goes wrong while indexing.
     */
    public static void main(String[] args) throws Exception {

        // Topics path
        final String TOPICS_EN_P = "C:\\longeval_train\\publish\\English\\Queries\\train.trec";
        final String TOPICS_FR_P = "C:\\longeval_train\\publish\\French\\Queries\\train.trec";

        // Indexes path
        final String INDEX_MUL_3GRAM_P = "C:\\Users\\jemon\\IdeaProjects\\seupd2223-jihuming\\code\\created_indexes\\2023_04_24_multilingual_3gram";
        final String INDEX_MUL_3GRAM_SYN_P = "C:\\Users\\jemon\\IdeaProjects\\seupd2223-jihuming\\code\\created_indexes\\2023_04_29_multilingual_3gram_synonym";
        final String INDEX_MUL_4GRAM_SYN_P = "C:\\Users\\jemon\\IdeaProjects\\seupd2223-jihuming\\code\\created_indexes\\2023_05_01_multilingual_4gram_synonym";
        final String INDEX_MUL_5GRAM_SYN_P = "C:\\Users\\jemon\\IdeaProjects\\seupd2223-jihuming\\code\\created_indexes\\2023_05_01_multilingual_5gram_synonym";
        final String INDEX_MUL_4GRAM_SYN_NER_P = "C:\\Users\\jemon\\IdeaProjects\\seupd2223-jihuming\\code\\created_indexes\\2023_05_05_multilingual_4gram_synonym_ner";

        // Runs path
        final String RUN_P = "runs";

        // Run info
        final String RUN_PREFIX = "seupd2223-JIHUMING-";
        final String runInfo;
        final int MAX_DOCS_RETRIEVED = 1000;

        Scanner input = new Scanner(System.in);

        System.out.print("Chose searcher:\n" +
                "1. English topics - English\n" +
                "2. English topics - English + 4-gram\n" +
                "3. English topics - English + French + 3-gram\n" +
                "4. English topics - English + French + 4-gram\n" +
                "5. English topics - English + French + 5-gram\n" +
                "6. English topics - English + French + 4-gram + NER\n" +
                "7. French topics - French\n" +
                "8. French topics - French + 4-gram\n" +
                "9. French topics - English + French + 3-gram\n" +
                "10. French topics - English + French + 4-gram\n" +
                "11. French topics - English + French + 5-gram\n" +
                "12. French topics - English + French + 4-gram + NER\n" +
                "Enter your choice: ");

        int runId = input.nextInt();

        while (runId < 1 || runId > 12) {
            System.out.println("Invalid choice. Please enter a number between 1 and 12.\n");
            runId = input.nextInt();
        }

        input.close();

        // Analyzers
        final EnglishAnalyzer enAn = new EnglishAnalyzer();
        final FrenchAnalyzer frAn = new FrenchAnalyzer();
        final NERAnalyzer nerAn = new NERAnalyzer();

        NGramAnalyzer ngramAn;
        Searcher s;

        switch (runId) {
            case 1:
                System.out.println("You entered 1: English topics - English");
                runInfo = "01_en_en";
                ngramAn = new NGramAnalyzer(3);

                s = new Searcher(enAn, frAn, ngramAn, nerAn, new BM25Similarity(),
                        INDEX_MUL_3GRAM_P, TOPICS_EN_P, 50,
                        RUN_PREFIX + runInfo, RUN_P, MAX_DOCS_RETRIEVED);
                s.search(runId);
                break;
            case 2:
                System.out.println("You entered 2: English topics - English + 4-gram");
                runInfo = "02_en_en_4gram";
                ngramAn = new NGramAnalyzer(4);

                s = new Searcher(enAn, frAn, ngramAn, nerAn, new BM25Similarity(),
                        INDEX_MUL_4GRAM_SYN_P, TOPICS_EN_P, 50,
                        RUN_PREFIX + runInfo, RUN_P, MAX_DOCS_RETRIEVED);
                s.search(runId);
                break;
            case 3:
                System.out.println("You entered 3: English topics - English + French + 3-gram");
                runInfo = "03_en_en_fr_3gram";
                ngramAn = new NGramAnalyzer(3);

                s = new Searcher(enAn, frAn, ngramAn, nerAn, new BM25Similarity(),
                        INDEX_MUL_3GRAM_P, TOPICS_EN_P, 50,
                        RUN_PREFIX + runInfo, RUN_P, MAX_DOCS_RETRIEVED);
                s.search(runId);
                break;
            case 4:
                System.out.println("You entered 4: English topics - English + French + 4-gram");
                runInfo = "04_en_en_fr_4gram";
                ngramAn = new NGramAnalyzer(4);

                s = new Searcher(enAn, frAn, ngramAn, nerAn, new BM25Similarity(),
                        INDEX_MUL_4GRAM_SYN_P, TOPICS_EN_P, 50,
                        RUN_PREFIX + runInfo, RUN_P, MAX_DOCS_RETRIEVED);
                s.search(runId);
                break;
            case 5:
                System.out.println("You entered 5: English topics - English + French + 5-gram");
                runInfo = "05_en_en_fr_5gram";
                ngramAn = new NGramAnalyzer(5);

                s = new Searcher(enAn, frAn, ngramAn, nerAn, new BM25Similarity(),
                        INDEX_MUL_5GRAM_SYN_P, TOPICS_EN_P, 50,
                        RUN_PREFIX + runInfo, RUN_P, MAX_DOCS_RETRIEVED);
                s.search(runId);
                break;
            case 6:
                System.out.println("You entered 6: English topics - English + French + 4-gram + NER");
                runInfo = "06_en_en_fr_4gram_ner";
                ngramAn = new NGramAnalyzer(4);

                s = new Searcher(enAn, frAn, ngramAn, nerAn, new BM25Similarity(),
                        INDEX_MUL_4GRAM_SYN_P, TOPICS_EN_P, 50,
                        RUN_PREFIX + runInfo, RUN_P, MAX_DOCS_RETRIEVED);
                s.search(runId);
                break;
            case 7:
                System.out.println("You entered 7: French topics - French");
                ngramAn = new NGramAnalyzer(3);
                runInfo = "07_fr_fr";

                s = new Searcher(enAn, frAn, ngramAn, nerAn, new BM25Similarity(),
                        INDEX_MUL_3GRAM_P, TOPICS_FR_P, 50,
                        RUN_PREFIX + runInfo, RUN_P, MAX_DOCS_RETRIEVED);
                s.search(runId);
                break;
            case 8:
                System.out.println("You entered 8: French topics - French + 4-gram");
                ngramAn = new NGramAnalyzer(4);
                runInfo = "08_fr_fr_4gram";

                s = new Searcher(enAn, frAn, ngramAn, nerAn, new BM25Similarity(),
                        INDEX_MUL_4GRAM_SYN_P, TOPICS_FR_P, 50,
                        RUN_PREFIX + runInfo, RUN_P, MAX_DOCS_RETRIEVED);
                s.search(runId);
                break;
            case 9:
                System.out.println("You entered 9: French topics - English + French + 3-gram");
                ngramAn = new NGramAnalyzer(3);
                runInfo = "09_fr_en_fr_3gram";

                s = new Searcher(enAn, frAn, ngramAn, nerAn, new BM25Similarity(),
                        INDEX_MUL_3GRAM_P, TOPICS_FR_P, 50,
                        RUN_PREFIX + runInfo, RUN_P, MAX_DOCS_RETRIEVED);
                s.search(runId);
                break;
            case 10:
                System.out.println("You entered 10: French topics - English + French + 4-gram");
                runInfo = "10_fr_en_fr_4gram";
                ngramAn = new NGramAnalyzer(4);

                s = new Searcher(enAn, frAn, ngramAn, nerAn, new BM25Similarity(),
                        INDEX_MUL_4GRAM_SYN_P, TOPICS_FR_P, 50,
                        RUN_PREFIX + runInfo, RUN_P, MAX_DOCS_RETRIEVED);
                s.search(runId);
                break;
            case 11:
                System.out.println("You entered 11: French topics - English + French + 5-gram");
                runInfo = "11_fr_en_fr_5gram";
                ngramAn = new NGramAnalyzer(5);

                s = new Searcher(enAn, frAn, ngramAn, nerAn, new BM25Similarity(),
                        INDEX_MUL_5GRAM_SYN_P, TOPICS_FR_P, 50,
                        RUN_PREFIX + runInfo, RUN_P, MAX_DOCS_RETRIEVED);
                s.search(runId);
                break;
            case 12:
                System.out.println("You entered 12: French topics - English + French + 4-gram + NER");
                runInfo = "12_fr_en_fr_4gram_ner";
                ngramAn = new NGramAnalyzer(4);

                s = new Searcher(enAn, frAn, ngramAn, nerAn, new BM25Similarity(),
                        INDEX_MUL_4GRAM_SYN_P, TOPICS_FR_P, 50,
                        RUN_PREFIX + runInfo, RUN_P, MAX_DOCS_RETRIEVED);
                s.search(runId);
                break;
            default:
                throw new IllegalArgumentException("Invalid run number.");
        }
        System.out.println("Done.");
    }
}
