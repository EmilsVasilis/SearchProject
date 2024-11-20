// Run this file in IDE
package org.search;

import java.io.IOException;

import java.util.Arrays;
import java.util.Scanner;

import java.nio.file.Paths;
import java.nio.file.Files;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;

import org.apache.lucene.document.Document;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.HighFreqTerms.TotalTermFreqComparator;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StringField;

public class Statistics
{

    // Directory where the search index will be saved
    private static final String INDEX_DIRECTORY = "target/index";

    public static void main(String[] args) throws Exception {
        corpusStatistics();
    }

    public static void corpusStatistics() throws Exception {
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        IndexReader ireader = DirectoryReader.open(directory);

        TermStats[] stats = HighFreqTerms.getHighFreqTerms(ireader, 50, null, new TotalTermFreqComparator());

        System.out.println(Arrays.toString(stats));


    }
}