
package org.search;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexing {

    private IndexWriter writer;

    
    public static class CustomAnalyzer extends Analyzer {
        private static final CharArraySet STOP_WORDS = EnglishAnalyzer.ENGLISH_STOP_WORDS_SET;

        @Override
        protected TokenStreamComponents createComponents(String fieldName) {
            StandardTokenizer tokenizer = new StandardTokenizer();
            TokenStream tokenStream = new LowerCaseFilter(tokenizer);  

            return new TokenStreamComponents(tokenizer, tokenStream);
        }
    }

   
    public void indexDocument(List<Document> docs, Analyzer analyzer) throws IOException {

        Directory dir = FSDirectory.open(Paths.get("target/index"));  
        IndexWriterConfig config = new IndexWriterConfig(analyzer);  
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        config.setSimilarity(new BM25Similarity(0.75F, 0.75F));
        writer = new IndexWriter(dir, config);  

        for (Document doc: docs){
             writer.addDocument(doc);
        }
             writer.close();
    }
   
}