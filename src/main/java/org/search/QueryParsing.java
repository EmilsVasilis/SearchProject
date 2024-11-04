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



    // Applies an Analyzer's pre-processing to a string, returning list of strings as a result
    public static List<String> analyzeTextToTerms(String text, Analyzer analyzer) throws IOException {
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
}