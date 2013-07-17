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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.io.OsmApi;
import org.openstreetmap.josm.plugins.notes.Note;
import org.openstreetmap.josm.plugins.notes.NotesXmlParser;
import org.openstreetmap.josm.plugins.notes.api.util.HttpUtils;
import org.openstreetmap.josm.tools.OsmUrlToBounds;
import org.xml.sax.SAXException;

public class DownloadAction {

    private final String CHARSET = "UTF-8";

    public void execute(List<Note> dataset, Bounds bounds) throws IOException {
        // create the URI for the data download
        String uri = Main.pref.get("osm-server.url", OsmApi.DEFAULT_API_URL) + "/0.6/notes";

        int zoom = OsmUrlToBounds.getZoom(Main.map.mapView.getRealBounds());
        // check zoom level
        if(zoom > 15 || zoom < 9) {
            return;
        }

        // add query params to the uri
        StringBuilder sb = new StringBuilder(uri)
            .append("?bbox=").append(bounds.getMin().lon())
            .append(",").append(bounds.getMin().lat())
            .append(",").append(bounds.getMax().lon())
            .append(",").append(bounds.getMax().lat());
        uri = sb.toString();

        // download the data
        String content = HttpUtils.get(uri, CHARSET);

        // clear dataset
        dataset.clear();

        // parse the data
        parseData(dataset, content);
    }

    private void parseData(List<Note> dataSet, String content) {
        List<Note> notes = new ArrayList<Note>();
        try {
            notes = NotesXmlParser.parseNotes(content);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        dataSet.clear();
        dataSet.addAll(notes);
    }
}
