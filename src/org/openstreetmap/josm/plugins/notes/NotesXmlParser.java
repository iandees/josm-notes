package org.openstreetmap.josm.plugins.notes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class NotesXmlParser extends DefaultHandler {
    private String chars;

    private Node thisNote = null;
    private List<Node> notes = new ArrayList<Node>(100);

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        chars = new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("id".equals(qName)) {
            thisNote.setOsmId(Long.parseLong(chars), 1);
        } else if ("status".equals(qName)) {
            thisNote.put("state", chars);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
        if ("note".equals(qName)) {
            double lat = Double.parseDouble(attrs.getValue("lat"));
            double lon = Double.parseDouble(attrs.getValue("lon"));
            thisNote = new Node(new LatLon(lat, lon));
        }
    }

    public List<Node> getNotes() {
        return Collections.unmodifiableList(notes);
    }

}
