
package org.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

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

   
    public void indexDocument(List<Document> docs) throws IOException {

        Directory dir = FSDirectory.open(Paths.get("target/index"));  
        IndexWriterConfig config = new IndexWriterConfig(new CustomAnalyzer());  
        writer = new IndexWriter(dir, config);  

        for (Document doc: docs){
             writer.addDocument(doc);
        }
             writer.close();
    }
   
}