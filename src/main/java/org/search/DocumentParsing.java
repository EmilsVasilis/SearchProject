package org.search;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

public class DocumentParsing {
    private static final String DOCUMENT_DIRECTORY = "src/main/resources/docs";
    private static final String ID_FIELD = "id";
    private static final String TITLE_FIELD = "title";
    private static final String DATE_FIELD = "date";
    public static final String TEXT_FIELD = "content";

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
            // LA Times - same as FBIS
            case "latimes":
            // Foreign Broadcast Information Service
            case "fbis":
                // FBIS stores lists of documents in files at the top level directory,
                // so these can be looped through and parsed.
                for (String file : files) {
                    documents.addAll(parseStandardFile(Paths.get(DOCUMENT_DIRECTORY, source, file), source));
                }
                break;
            // Federal Register - same as Financial Times, documents split into multiple subdirectories
            case "fr94":
            // Financial Times
            case "ft":
                for (String subDirectory : files) {
                    File subDirectoryFile = new File(Paths.get(DOCUMENT_DIRECTORY, source, subDirectory).toString());
                    String[] subFiles = subDirectoryFile.list();
                    if (subFiles == null) {
                        continue;
                    }
                    for (String subFile : subFiles) {
                        documents.addAll(parseStandardFile(Paths.get(DOCUMENT_DIRECTORY, source, subDirectory, subFile), source));
                    }
                }
        }
        return documents;
    }

    public List<Document> parseStandardFile(Path fullFilePath, String source) throws IOException {
        ArrayList<Document> documents = new ArrayList<>();

        // Retrieve the list of documents from the file.
        String content = new String(Files.readAllBytes(fullFilePath));

        // Split the file into separate documents, not consuming the separator.
        String[] docs = content.split("(?=<DOC>)");

        // Parse every FBIS document.
        for (String doc : docs) {
            Document newDoc;
            switch (source) {
                case "latimes":
                    newDoc = parseLaDoc(doc);
                    break;
                case "fbis":
                    newDoc = parseFbisDoc(doc);
                    break;
                case "ft":
                    newDoc = parseFtDoc(doc);
                    break;
                case "fr94":
                    newDoc = parseFr94Doc(doc);
                    break;
                default:
                    newDoc = null;
            }
            // In case preprocessing has failed, drop any documents that have failed to parse.
            // (In practice, this is just newlines at the start of documents)
            if (newDoc != null) {
                documents.add(newDoc);
            }
        }
        return documents;
    }

    public Document parseFr94Doc(String content) {
        // Find the TREC Document ID - stored as DOCNO
        Pattern pattern = Pattern.compile("<DOCNO>\\s(.*)\\s</DOCNO>");
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return null;
        }
        String docNo = matcher.group(1);

        // There is no specific field for Title in FR94

        // Find the Document's Date - stored in DATE. This is parsed as is follows the form Month Date, Year.
        // Shortest possible month is May (3 chars), longest is September (9 chars).
        // Date is one or two digits.
        // Year is always 4 digits.
        // Some Date sections are too big to be handled by regex.
        // Given the faulty split of documents, not all documents contain a date field.
        String date = "";
        if (content.contains("<DATE>")) {
            String dateContent = (content.split("<DATE>")[1]).split("</DATE>")[0];
            pattern = Pattern.compile("(\\w{3,9}\\s\\d{1,2},\\s\\d{4})");
            matcher = pattern.matcher(dateContent);
            if (matcher.find()) {
                date = matcher.group(1);
            }
        }

        // Get contents of document and remove comment tags
        String text = (content.split("<TEXT>")[1]).split("</TEXT>")[0];
        text  = text.replaceAll("<!--(.|\\s)*?-->", "");

        return createDocument(docNo, date, "", text);
    }

    public Document parseLaDoc(String content) {
        // Find the TREC Document ID - stored as DOCNO
        Pattern pattern = Pattern.compile("<DOCNO>\\s(.*)\\s</DOCNO>");
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return null;
        }
        String docNo = matcher.group(1);

        // Find the Document Title - stored as HEADLINE
        // This also contains the author's name, other details about publication, as well as purchasing details,
        // which are removed.
        pattern = Pattern.compile("<HEADLINE>\\s*(((.*)\\s)*?)\\s*</HEADLINE>");
        matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return null;
        }
        String title = matcher.group(1);
        title = title.split("BY")[0];
        title = title.replace("<P>", "");
        title = title.replace("</P>", "");

        // Find the Document's Date - stored as DATE - note this also includes the edition, which is removed.
        pattern = Pattern.compile("<DATE>\\s*<P>\\s*(.*)\\s*</P>\\s*</DATE>");
        matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return null;
        }
        String date = matcher.group(1);
        // Bring (eg) December 29, 1990, Saturday, San Diego County Edition to December 29, 1990
        String[] elements = date.split(",");
        date = elements[0] + "," + elements[1];

        // Split out the document text - cannot be done via regex due to large text blocks and stack size.
        if (!content.contains("<TEXT>")) { // If the document does not contain contents (eg LA011290-0165), ignore it.
            return null;
        }
        String text = (content.split("<TEXT>")[1]).split("</TEXT>")[0];
        text  = text.replace("<P>", "");
        text = text.replace("</P>", "");

        return createDocument(docNo, title, date, text);
    }

    public Document parseFtDoc(String content) {
        // Find the Document ID - stored as DOCNO
        Pattern pattern = Pattern.compile("<DOCNO>(.*)</DOCNO>");
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return null;
        }
        String docNo = matcher.group(1);

        // Find the Document Title and Date - stored in HEADLINE
        // Headline takes the form FT Date / Title
        pattern = Pattern.compile("<HEADLINE>\\s*FT\\s*(.*)\\s*/\\s*((.*\\s)*?)\\s*</HEADLINE>");
        matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return null;
        }
        String date = matcher.group(1);
        String title = matcher.group(2);

        // Split out the document text - cannot be done via regex due to large text blocks and stack size.
        String text = (content.split("<TEXT>")[1]).split("</TEXT>")[0];

        return createDocument(docNo, title, date, text);
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

        return createDocument(docNo, title, date, text);
    }

    private Document createDocument(String documentId, String date, String title, String content) {
        Document doc = new Document();
        doc.add(new StringField(ID_FIELD, documentId, Field.Store.YES));
        doc.add(new StringField(DATE_FIELD, date, Field.Store.YES));
        doc.add(new TextField(TITLE_FIELD, title, Field.Store.YES));
        doc.add(new TextField(TEXT_FIELD, content, Field.Store.YES));
        return doc;
    }
}
