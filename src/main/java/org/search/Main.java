package org.search;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;

public class Main {

    public static void main(String[] args) throws IOException {
        // Perform document parsing.
        DocumentParsing parsing = new DocumentParsing();
        List<Document> docs = parsing.parseDocuments();
        // Create custom analyzer
        Analyzer analyzer = CustomAnalyzer.builder()
            .withTokenizer("standard")
            .addTokenFilter("lowercase")
            .addTokenFilter("porterstem")
            .addTokenFilter("stop", "ignoreCase", "false", "words", "./SMART_stopwords.txt", "format", "wordset")
            .build();
        Indexing index = new Indexing();
        index.indexDocument(docs, analyzer);
        // Perform Query parsing
        QueryParsing topics = new QueryParsing();
        List<Query> queries = topics.parseTopicsFile(analyzer);
        Searching searcher = new Searching();
        searcher.search(queries);
    }
}