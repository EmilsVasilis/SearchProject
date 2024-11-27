// Run this file in IDE
package org.search;

import java.nio.file.Paths;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.HighFreqTerms.TotalTermFreqComparator;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

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


    }
}