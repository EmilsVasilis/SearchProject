package org.search;


import org.apache.lucene.document.Document;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        // Perform document parsing.
        DocumentParsing parsing = new DocumentParsing();
        List<Document> docs = parsing.parseDocuments();
        System.out.println(docs.size());
    }
}