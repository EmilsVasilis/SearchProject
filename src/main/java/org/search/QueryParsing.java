package org.search;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BoostQuery;

public class QueryParsing {
    private static final String TOPIC_DIRECTORY = "src/main/resources/topics";
    private static final String TOPIC_FOOT = "(</top>)";
    private static final String NUMBER_HEAD = "(<num> Number: )";
    private static final String TITLE_HEAD = "(<title>)";
    private static final String DESCRIPTION_HEAD = "(<desc> Description: )";
    private static final String NARRATIVE_HEAD = "(<narr> Narrative: )";

    private static final float TITLE_BOOST = 4.0f;
    private static final float DESCRIPTION_BOOST = 1.7f;
    private static final float NARRATIVE_BOOST = 1.2f;
    private static final float IRRELEVANT_BOOST = 2.0f;

    // Parse topics file, breaking up by topic, then return a query per topic
    public List<Query> parseTopicsFile(Analyzer analyzer) throws IOException {
        ArrayList<Query> queries = new ArrayList<>();

        // Retrieve the list of documents from the file.
        Path path = Paths.get(TOPIC_DIRECTORY);
        String content = new String(Files.readAllBytes(path));

        // Split the file into separate topics, not consuming the separator.
        String[] topics = content.split("(?=<top>)");

        // Parse every topic and return all queries.
       for(String topic : topics) {
            queries.add(parseSingleTopic(topic, analyzer));
       }
        return queries;
    }

    // Parse single topic into query
    private static Query parseSingleTopic(String topic, Analyzer analyzer) throws IOException {
        // Find the Topic Number
        Pattern pattern = Pattern.compile(NUMBER_HEAD + "((.|\\s)+?)" + TITLE_HEAD);
        Matcher matcher = pattern.matcher(topic);
        if (!matcher.find()) {
            return null;
        }
        String number = matcher.group(2);

        // Find the Topic Title
        pattern = Pattern.compile(TITLE_HEAD + "((.|\\s)+?)" + DESCRIPTION_HEAD);
        matcher = pattern.matcher(topic);
        if (!matcher.find()) {
            return null;
        }
        String title = matcher.group(2);

        // Find the Topic Description
        pattern = Pattern.compile(DESCRIPTION_HEAD + "((.|\\s)+?)" + NARRATIVE_HEAD);
        matcher = pattern.matcher(topic);
        if (!matcher.find()) {
            return null;
        }
        String description = matcher.group(2);

        // Find the Topic Narrative
        pattern = Pattern.compile(NARRATIVE_HEAD + "((.|\\s)+?)" + TOPIC_FOOT);
        matcher = pattern.matcher(topic);
        if (!matcher.find()) {
            return null;
        }
        String narrative = matcher.group(2);

        // pass collected information to analyzer for pre-processing

        //layout of how to generate queries
        BooleanQuery.Builder query = new BooleanQuery.Builder();
        // Generate a query from the title field with a boost of 4.0
        List<String> titleTerms = analyzeTextToTerms(title, analyzer);
        for(String term : titleTerms){
            Query qterm = new TermQuery(new Term("title", term));
            Query boostTerm = new BoostQuery(qterm, TITLE_BOOST);
        	query.add(new BooleanClause(boostTerm, BooleanClause.Occur.SHOULD));
        }

        // Generate a query from the description with a boost of 1.7
        List<String> descriptionTerms = analyzeTextToTerms(title, analyzer);
        for(String term : descriptionTerms){
            Query qterm = new TermQuery(new Term("description", term));
            Query boostTerm = new BoostQuery(qterm, DESCRIPTION_BOOST);
        	query.add(new BooleanClause(boostTerm, BooleanClause.Occur.SHOULD));
        }

        // Run a method on the narrative splitting it into Relevant and Not Relevant

        // (if present) Generate a query from relevant narrative with boost of 1.2

        // (if present) Generate a filter clause with 2.0 boost for Not Relevant narrative
        
        // Build and return query
        return query.build();
    }

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

}