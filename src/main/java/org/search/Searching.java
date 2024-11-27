package org.search;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Searching {

    private static final int RESULTS_PER_QUERY = 1000;

    public void search(List<Query> queries) {
        String indexDir = "target/index";
        String outputFile = "src/main/resources/output.txt";

        try (Directory dir = FSDirectory.open(Paths.get(indexDir));
             IndexReader reader = DirectoryReader.open(dir);
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            IndexSearcher searcher = new IndexSearcher(reader);
            searcher.setSimilarity(new BM25Similarity());

            int queryNumber = 401;
            for (Query query : queries) {
                // Execute each pre-processed query
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
            // Execute the search and retrieve top results
            ScoreDoc[] hits = searcher.search(query, RESULTS_PER_QUERY).scoreDocs;
           
            for (int i = 0; i < hits.length; i++) {
                ScoreDoc hit = hits[i];
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