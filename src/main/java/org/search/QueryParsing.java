import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryParsing {
    private static final String TOPIC_DIRECTORY = "src/main/resources/topics";
    private static final String TOPIC_HEAD = "<top>";
    private static final String TOPIC_FOOT = "</top>";
    private static final String NUMBER_HEAD = "<num>";
    private static final String TITLE_HEAD = "<title>";
    private static final String DESCRIPTION_HEAD = "<desc>";
    private static final String NARRATIVE_HEAD = "<narr>";

    // Parse topics file line by line, breaking up by headings - TODO
    public List<Query> parseTopicsFile(Analyzer analyzer, Directory directory,
        Similarity similarity) throws IOException {
        ArrayList<Query> queries = new ArrayList<>();

        // Retrieve the list of documents from the file.
        String content = new String(Files.readAllBytes(TOPIC_DIRECTORY));

        // Split the file into separate topics, not consuming the separator.
        String[] topics = content.split("(?=" + TOPIC_HEAD + ")");

        // Parse every topic and return all queries.
       for(String topic : topics) {
            queries.add(parseSingleTopic(topic));
       }
        return queries;
    }

    // Parse single topic into query

    // Applies an Analyzer's pre-processing to a string, returning list of strings as a result
    private static List<String> analyzeTextToTerms(String text, Analyzer analyzer) throws IOException {
        List<String> result = new ArrayList<String>();
        TokenStream tokenStream = analyzer.tokenStream("Text", text);
        CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            result.add(attr.toString());
        }
        tokenStream.close();
        return result;
    }

    // check if string is any of the identifiers from topics document
    private static boolean isIdentifier(String identity) {
    	switch(identity) {
    	case TOPIC_HEAD:
    	case TOPIC_FOOT:
    	case NUMBER_HEAD:
    	case TITLE_HEAD:
        case DESCRIPTION_HEAD:
        case NARRATIVE_HEAD:
    		return true;
    	default:
    		return false;
    	}
    }
}