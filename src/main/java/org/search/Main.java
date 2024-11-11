package org.search;


import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;

public class Main {
    public static void main(String[] args) throws IOException {
        // Perform document parsing.
        DocumentParsing parsing = new DocumentParsing();
        List<Document> docs = parsing.parseDocuments();
        System.out.println(docs.size());
        Indexing index = new Indexing();
        index.indexDocument(docs);
        // Perform Query parsing
        Analyzer analyzer = new EnglishAnalyzer(); // TODO - Delete this temp analyzer and replace
        QueryParsing topics = new QueryParsing();
        List<Query> queries = topics.parseTopicsFile(analyzer);
        Searching searcher = new Searching();
        searcher.search(queries);
    }
}