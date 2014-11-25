/* Copyright (c) 2013, Ian Dees
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.openstreetmap.josm.plugins.notes;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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

public class NotesDumpXmlParser extends DefaultHandler {
    private StringBuffer buffer = new StringBuffer();

    private final static SimpleDateFormat NOTE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ssZ", Locale.ENGLISH);

    private List<Note> notes = new ArrayList<Note>(100000);
    private Note thisNote;

    private Date commentCreateDate;
    private String commentUsername;
    private long commentUid;

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        buffer.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("note".equals(qName)) {
            notes.add(thisNote);
        } else if ("comment".equals(qName)) {
            User commentUser = User.createOsmUser(commentUid, commentUsername);
            thisNote.addComment(thisNote.new Comment(commentCreateDate, commentUser, buffer.toString()));
            commentUid = 0;
            commentUsername = null;
            commentCreateDate = null;
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
            thisNote.setId(Long.parseLong(attrs.getValue("id")));

            try {
                thisNote.setCreatedAt(NOTE_DATE_FORMAT.parse(attrs.getValue("created_at")));
            } catch (ParseException e) {
                System.err.println("Could not parse note date from notes dump: \"" + buffer.toString() + "\":");
                e.printStackTrace();
            }
        } else if ("comment".equals(qName)) {
            commentUid = Long.parseLong(attrs.getValue("uid"));
            commentUsername = attrs.getValue("user");
            try {
                commentCreateDate = NOTE_DATE_FORMAT.parse(attrs.getValue("timestamp"));
            } catch (ParseException e) {
                System.err.println("Could not parse comment date from notes dump: \"" + buffer.toString() + "\":");
                e.printStackTrace();
            }
        }
    }

    public List<Note> getNotes() {
        return Collections.unmodifiableList(notes);
    }

    public static List<Note> parseNotes(File file) throws SAXException, ParserConfigurationException, IOException {
        NotesDumpXmlParser handler = new NotesDumpXmlParser();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(handler);
        xmlReader.parse(new InputSource(new FileReader(file)));
        return handler.getNotes();
    }

}
