package org.openstreetmap.josm.plugins.notes;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.User;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class NotesXmlParser extends DefaultHandler {
    private StringBuffer buffer = new StringBuffer();

    private final static SimpleDateFormat NOTE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.ENGLISH);

    private List<Note> notes = new ArrayList<Note>(100);
    private Note thisNote;

    private Date commentCreateDate;
    private String commentUsername;
    private long commentUid;
    private String commentText;

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        buffer.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("id".equals(qName)) {
            thisNote.setId(Long.parseLong(buffer.toString()));
        } else if ("status".equals(qName)) {
            thisNote.setState(Note.State.valueOf(buffer.toString()));
        } else if ("url".equals(qName)) {
        	thisNote.setNoteUrl(buffer.toString());
        } else if ("date_created".equals(qName)) {
            // Note create date
            try {
                thisNote.setCreatedAt(NOTE_DATE_FORMAT.parse(buffer.toString()));
            } catch (ParseException e) {
                System.err.println("Could not parse note date from API: \"" + buffer.toString() + "\":");
                e.printStackTrace();
            }
        } else if ("note".equals(qName)) {
            notes.add(thisNote);
        } else if ("date".equals(qName)) {
            // Comment create date
            try {
                commentCreateDate = NOTE_DATE_FORMAT.parse(buffer.toString());
            } catch (ParseException e) {
                System.err.println("Could not parse comment date from API: \"" + buffer.toString() + "\":");
                e.printStackTrace();
            }
        } else if ("user".equals(qName)) {
            commentUsername = buffer.toString();
        } else if ("uid".equals(qName)) {
            commentUid = Long.parseLong(buffer.toString());
        } else if ("text".equals(qName)) {
            commentText = buffer.toString();
        } else if ("comment".equals(qName)) {
            User commentUser = User.createOsmUser(commentUid, commentUsername);
            thisNote.addComment(thisNote.new Comment(commentCreateDate, commentUser, commentText));
            commentUid = 0;
            commentUsername = null;
            commentCreateDate = null;
            commentText = null;
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
        buffer.setLength(0);
        if ("note".equals(qName)) {
            double lat = Double.parseDouble(attrs.getValue("lat"));
            double lon = Double.parseDouble(attrs.getValue("lon"));
            LatLon noteLatLon = new LatLon(lat, lon);
            thisNote = new Note(noteLatLon);
        }
    }

    public List<Note> getNotes() {
        return Collections.unmodifiableList(notes);
    }

    public static List<Note> parseNotes(String data) throws SAXException, ParserConfigurationException, IOException {
        NotesXmlParser handler = new NotesXmlParser();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(handler);
        xmlReader.parse(new InputSource(new StringReader(data)));
        return handler.getNotes();
    }

}
