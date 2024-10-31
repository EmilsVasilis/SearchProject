package org.search;


import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentParsing {
    private static final String DOCUMENT_DIRECTORY = "src/main/resources/docs";
    private static final String ID_FIELD = "id";
    private static final String TITLE_FIELD = "title";
    private static final String DATE_FIELD = "date";
    private static final String TEXT_FIELD = "content";

    public List<Document> parseDocuments() throws IOException {
        ArrayList<Document> documents = new ArrayList<>();

        // Find the list of document sources, stored in the top directory.
        File file = new File(DOCUMENT_DIRECTORY);
        String[] sources = file.list();
        if (sources == null) {
            return documents;
        }

        // Add all sources' documents to the document list.
        for (String source : sources) {
            documents.addAll(parseSingleSource(source));
        }

        return documents;
    }

    // Given the folder name of one of the document sources, parse this source's documents into Lucene documents.
    public List<Document> parseSingleSource(String source) throws IOException {
        // Find the list of files from the source.
        File directoryFile = new File(Paths.get(DOCUMENT_DIRECTORY, source).toString());
        String[] files = directoryFile.list();

        // Error checking - if this directory does not contain any files, give up on this source.
        ArrayList<Document> documents = new ArrayList<>();
        if (files == null) {
            return documents;
        }

        switch (source) {
            // Ignore this directory, not a source
            case "dtds":
                break;
            // Foreign Broadcast Information Service
            case "fbis":
                // FBIS stores lists of documents in files at the top level directory,
                // so these can be looped through and parsed.
                for (String file : files) {
                    documents.addAll(parseFbisFile(Paths.get(DOCUMENT_DIRECTORY, source, file)));
                }
            // Federal Register - TODO
            case "fr94":
            // Financial Times - TODO
            case "ft":
            // LA Times - TODO
            case "latimes":
                break;
        }
        return documents;
    }

    public List<Document> parseFbisFile(Path fullFilePath) throws IOException {
        ArrayList<Document> documents = new ArrayList<>();

        // Retrieve the list of documents from the file.
        String content = new String(Files.readAllBytes(fullFilePath));

        // Split the file into separate documents, not consuming the separator.
        String[] docs = content.split("(?=<DOC>)");

        // Parse every FBIS document.
        for (String doc : docs) {
            Document newDoc = parseFbisDoc(doc);
            // In case preprocessing has failed, drop any documents that have failed to parse.
            // (In practice, this is just newlines at the start of documents)
            if (newDoc != null) {
                documents.add(newDoc);
            }
        }

        return documents;
    }

    public Document parseFbisDoc(String content) {
        // Find the Document ID - stored as DOCNO
        Pattern pattern = Pattern.compile("<DOCNO>\\s(.*)\\s</DOCNO>");
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return null;
        }
        String docNo = matcher.group(1);

        // Find the Document Title - stored as TI
        pattern = Pattern.compile("<TI>\\s*(.*)\\s*</TI>");
        matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return null;
        }
        String title = matcher.group(1);

        // Find the Document's Date - stored as DATE1
        pattern = Pattern.compile("<DATE1>\\s*(.*)\\s*</DATE1>");
        matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return null;
        }
        String date = matcher.group(1);

        // Split out the document text - cannot be done via regex due to large text blocks and stack size.
        String text = (content.split("<TEXT>")[1]).split("</TEXT>")[0];

        Document doc = new Document();
        doc.add(new StringField(ID_FIELD, docNo, Field.Store.YES));
        doc.add(new StringField(DATE_FIELD, date, Field.Store.YES));
        doc.add(new TextField(TITLE_FIELD, title, Field.Store.YES));
        doc.add(new TextField(TEXT_FIELD, text, Field.Store.YES));


        return doc;
    }
}
