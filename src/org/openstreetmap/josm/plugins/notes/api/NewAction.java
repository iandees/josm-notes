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
package org.openstreetmap.josm.plugins.notes.api;

import java.awt.Point;
import java.io.IOException;
import java.net.URLEncoder;

import javax.xml.parsers.ParserConfigurationException;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.notes.ConfigKeys;
import org.openstreetmap.josm.plugins.notes.Note;
import org.openstreetmap.josm.plugins.notes.NotesXmlParser;
import org.openstreetmap.josm.plugins.notes.api.util.HttpUtils;
import org.xml.sax.SAXException;

public class NewAction {

    private final String CHARSET = "UTF-8";

    public Note execute(Point p, String text) throws IOException {
        // where has the issue been added
        LatLon latlon = Main.map.mapView.getLatLon(p.x, p.y);

        // create the URI for the data download
        String uri = Main.pref.get(ConfigKeys.NOTES_API_URI_BASE);

        String post = new StringBuilder("lon=")
            .append(latlon.lon())
            .append("&lat=")
            .append(latlon.lat())
            .append("&text=")
            .append(URLEncoder.encode(text, CHARSET))
            .toString();

        String result = null;
        if(Main.pref.getBoolean(ConfigKeys.NOTES_API_DISABLED)) {
            result = "ok 12345";
        } else {
            result = HttpUtils.post(uri, post, CHARSET);
        }

        Note osmNote = null;
        try {
            osmNote = NotesXmlParser.parseNotes(result).get(0);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return osmNote;
    }
}
