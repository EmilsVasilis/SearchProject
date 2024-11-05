package org.search;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.similarities.*;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Searching {

    public void search(List<Query> queries) {
        String indexDir = "target/index";
        String outputFile = "src/main/resources/output.txt";

        try (Directory dir = FSDirectory.open(Paths.get(indexDir));
             IndexReader reader = DirectoryReader.open(dir);
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            IndexSearcher searcher = new IndexSearcher(reader);
            searcher.setSimilarity(new BM25Similarity());

            int queryNumber = 1;
            for (Query query : queries) {
                // Execute each pre-processed query
                System.out.println(query);
                executeQuery(searcher, queryNumber, query, writer);
                queryNumber++;
            }

            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void executeQuery(IndexSearcher searcher, int queryNumber, Query query, BufferedWriter writer) {
        try {
            // Execute the search and retrieve top 50 results
            ScoreDoc[] hits = searcher.search(query, 50).scoreDocs;
           
            for (int i = 0; i < hits.length; i++) {
                ScoreDoc hit = hits[i];
                System.out.println(hits[i]);
                Document doc = searcher.doc(hit.doc);
                String docId = doc.get("id"); // Assuming the id field is a string

                float score = hit.score;

                // Write each result in the specified format
                writer.write(String.format("%d Q0 %s %d %.6f STANDARD%n", queryNumber, docId, i + 1, score));

            }

            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}