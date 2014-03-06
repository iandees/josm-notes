package org.openstreetmap.josm.plugins.notes.api.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.OsmApi;
import org.openstreetmap.josm.io.OsmApiInitializationException;
import org.openstreetmap.josm.io.OsmTransferCanceledException;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.plugins.notes.Note;
import org.openstreetmap.josm.plugins.notes.NotesXmlParser;
import org.xml.sax.SAXException;

import static org.openstreetmap.josm.tools.I18n.tr;

public class NotesCapableOsmApi extends OsmApi {

    public static final int NOTE_DOWNLOAD_LIMIT = 10000;

	private static HashMap<String, NotesCapableOsmApi> instances = new HashMap<String, NotesCapableOsmApi>();

	public List<Note> getNotesInBoundingBox(Bounds bounds) throws OsmTransferException {
		ProgressMonitor monitor = NullProgressMonitor.INSTANCE;
		
		String url = new StringBuilder()
			.append("notes?bbox=")
			.append(bounds.getMin().lon())
			.append(",").append(bounds.getMin().lat())
            .append(",").append(bounds.getMax().lon())
            .append(",").append(bounds.getMax().lat())
            .toString();
		String response = sendRequest("GET", url, null, monitor, false, false);
		return parseNotes(response);
	}
	
	
	public void closeNote(Note note, String closeMessage) throws OsmTransferException {
		ProgressMonitor monitor = NullProgressMonitor.INSTANCE;
		String encodedMessage;
		try {
			encodedMessage = URLEncoder.encode(closeMessage, "UTF-8");
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
		StringBuilder urlBuilder = new StringBuilder()
			.append("notes/")
			.append(note.getId())
			.append("/close");
		if(encodedMessage != null && !encodedMessage.trim().isEmpty()) {
			urlBuilder.append("?text=");
			urlBuilder.append(encodedMessage);
		}
		
		sendRequest("POST", urlBuilder.toString(), null, monitor, true, false);
	}
	
	public Note reopenNote(Note note, String reactivateMessage) throws OsmTransferException {
		ProgressMonitor monitor = NullProgressMonitor.INSTANCE;
		String encodedMessage;
		try {
			encodedMessage = URLEncoder.encode(reactivateMessage, "UTF-8");
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		StringBuilder urlBuilder = new StringBuilder()
			.append("notes/")
			.append(note.getId())
			.append("/reopen");
		if(encodedMessage != null && !encodedMessage.trim().isEmpty()) {
			urlBuilder.append("?text=");
			urlBuilder.append(encodedMessage);
		}
	
		String response = sendRequest("POST", urlBuilder.toString(), null, monitor, true, false);
		List<Note> newNote = parseNotes(response);
		if(newNote.size() != 0) {
			return newNote.get(0);
		}
		return null;
	}
	
	public Note createNote(LatLon latlon, String text) throws OsmTransferException {
		ProgressMonitor monitor = NullProgressMonitor.INSTANCE;
		String encodedText;
		try {
			encodedText = URLEncoder.encode(text, "UTF-8");
		}
		catch(UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		String url = new StringBuilder()
			.append("notes?lat=")
			.append(latlon.lat())
			.append("&lon=")
			.append(latlon.lon())
			.append("&text=")
			.append(encodedText).toString();
		
		String response = sendRequest("POST", url, null, monitor, true, false);
		List<Note> newNote = parseNotes(response);
		if(newNote.size() != 0) {
			return newNote.get(0);
		}
		return null;
	}
	
	public Note AddCommentToNote(Note note, String comment) throws OsmTransferException {
		if(comment == null || comment.trim().isEmpty()) {
			return note;
		}
		ProgressMonitor monitor = NullProgressMonitor.INSTANCE;
		String encodedComment;
		try {
			encodedComment = URLEncoder.encode(comment, "UTF-8");
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
			return note;
		}
		String url = new StringBuilder()
			.append("notes/")
			.append(note.getId())
			.append("/comment?text=")
			.append(encodedComment).toString();
		
		String response = sendRequest("POST", url, null, monitor, true, false);
		List<Note> modifiedNote = parseNotes(response);
		if(modifiedNote.size() !=0) {
			return modifiedNote.get(0);
		}
        return note;
	}

    /**
     * Sends a request to the note search API which searches notes and comments
     * for a specified string.
     * @param searchTerm The string to search for
     * @param bugLimit   How many bugs to limit the search to.
     *                    Must be between 1 and 9999
     * 					  If not specified (null) the API defaults to 100
     * @param closedDays In addition to open notes, also find notes that have been
     *                    closed in the last "x" days.
     *                    0 means only open notes
     *                    -1 means all notes
     *                    If not specified (null) the API defaults to 7
     * @return List of notes matching the search criteria or empty list if no matches are found
     * @throws OsmTransferException
     */
    public List<Note> searchNotes(String searchTerm, Integer bugLimit, Integer closedDays) throws OsmTransferException {
        if(bugLimit != null && (bugLimit < 1 || bugLimit > NOTE_DOWNLOAD_LIMIT)) {
            throw new IllegalArgumentException("Bug limit must be between 1 and 9999");
        }
        if(closedDays != null && closedDays < -1) {
            throw new IllegalArgumentException("Closed date must be -1 or greater");
        }
        ProgressMonitor monitor = NullProgressMonitor.INSTANCE;
        String searchTermEncoded = "";
        try {
            searchTermEncoded = URLEncoder.encode(searchTerm, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new ArrayList<Note>();
        }
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("notes/search?q=");
        urlBuilder.append(searchTermEncoded);
        if(bugLimit != null) {
            urlBuilder.append("&limit=");
            urlBuilder.append(bugLimit);
        }
        if(closedDays != null) {
            urlBuilder.append("&closed=");
            urlBuilder.append(closedDays);
        }
        String url = urlBuilder.toString();
        String response = sendRequest("GET", url, null, monitor, false, false);
        return parseNotes(response);
    }
	
	private List<Note> parseNotes(String notesXml) {
		try {
            return NotesXmlParser.parseNotes(notesXml);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        }
		return new ArrayList<Note>();
		
	}
	
	/**
	 * The rest of this class is basically copy/paste from OsmApi.java in core. Since the 
	 * methods are private/protected, I copied them here. Hopefully notes functionality will
	 * be integrated into core at some point since this is now a core OSM API feature 
	 * and this copy/paste business can go away.
	 */
	
	protected NotesCapableOsmApi(String serverUrl) {
		super(serverUrl);
	}
	
    static public NotesCapableOsmApi getNotesApi(String serverUrl) {
        NotesCapableOsmApi api = instances.get(serverUrl);
    	if (api == null) {
    		api = new NotesCapableOsmApi(serverUrl);
    		try {
    			api.initialize(null);
    		} catch(OsmApiInitializationException e) {
    			Main.error("problem initializing Notes API");
    			e.printStackTrace();
    		} catch(OsmTransferCanceledException e) {
    			Main.error("problem initializing Notes API");
    			e.printStackTrace();
    		}
    		instances.put(serverUrl, api);
    	}
    	return api;
    }
	
	public static NotesCapableOsmApi getNotesApi() {
		String serverUrl = Main.pref.get("osm-server.url", OsmApi.DEFAULT_API_URL);
        if (serverUrl == null)
            throw new IllegalStateException(tr("Preference ''{0}'' missing. Cannot initialize NotesApi.", "osm-server.url"));
        return getNotesApi(serverUrl);
	}
}
