package org.openstreetmap.josm.plugins.notes.api.util;

import java.net.HttpURLConnection;

import org.openstreetmap.josm.io.OsmConnection;
import org.openstreetmap.josm.io.OsmTransferException;

public class NoteConnection extends OsmConnection {

	/**
	 * Hacking around OsmConnection having this method set to protected
	 */
	protected void addAuth(HttpURLConnection connection) throws OsmTransferException {
		super.addAuth(connection);
	}
}
