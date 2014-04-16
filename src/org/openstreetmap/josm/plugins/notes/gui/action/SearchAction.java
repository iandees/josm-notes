package org.openstreetmap.josm.plugins.notes.gui.action;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.progress.PleaseWaitProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.widgets.HistoryChangedListener;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.plugins.notes.ConfigKeys;
import org.openstreetmap.josm.plugins.notes.Note;
import org.openstreetmap.josm.plugins.notes.NotesPlugin;
import org.openstreetmap.josm.plugins.notes.api.util.NotesCapableOsmApi;
import org.openstreetmap.josm.plugins.notes.gui.NotesDialog;
import org.openstreetmap.josm.plugins.notes.gui.dialogs.TextInputDialog;
import org.xml.sax.SAXException;

public class SearchAction extends NotesAction {

    private static final long serialVersionUID = 1L;

    private NotesPlugin plugin;

    private String searchTerm;

    public SearchAction(NotesDialog dialog, NotesPlugin plugin) {
        super(tr("Reopen note"), dialog);
        this.plugin = plugin;
    }

    @Override
    protected void doActionPerformed(ActionEvent e) throws Exception {
        if(Main.pref.getBoolean(ConfigKeys.NOTES_API_OFFLINE)) {
            JOptionPane.showMessageDialog(Main.map, "You must be in online mode to use the search feature");
            canceled = true;
            return;
        }
        List<String> history = new LinkedList<String>(Main.pref.getCollection(ConfigKeys.NOTES_SEARCH_HISTORY, new LinkedList<String>()));
        HistoryChangedListener l = new HistoryChangedListener() {
            public void historyChanged(List<String> history) {
                Main.pref.putCollection(ConfigKeys.NOTES_SEARCH_HISTORY, history);
            }
        };
        searchTerm = TextInputDialog.showDialog(Main.map,
                tr("Search for notes?"),
                tr("<html>Search for notes (will go into offline mode)</html>"),
                NotesPlugin.loadIcon("find_notes.png"),
                history, l);

        if(searchTerm == null) {
            canceled = true;
            return;
        }
        dialog.refreshNoteStatus();
    }

    @Override
    public void execute() throws Exception {
        int noteLimit = Main.pref.getInteger(ConfigKeys.NOTES_SEARCH_LIMIT, 1000);
        int daysClosed = Main.pref.getInteger(ConfigKeys.NOTES_SEARCH_DAYS_CLOSED, 0);
        //make sure values are within API limits
        if(noteLimit < 1) {
            System.out.println("Note download too low. Setting to 1");
            noteLimit = 1;
            Main.pref.putInteger(ConfigKeys.NOTES_SEARCH_LIMIT, noteLimit);
        }
        if(noteLimit > NotesCapableOsmApi.NOTE_DOWNLOAD_LIMIT) {
            System.out.println("Note download limit too high. Setting to API limit");
            noteLimit = NotesCapableOsmApi.NOTE_DOWNLOAD_LIMIT;
            Main.pref.putInteger(ConfigKeys.NOTES_SEARCH_LIMIT, noteLimit);
        }
        if(daysClosed < -1) {
            System.out.println("Days closed parameter too low. Setting to -1");
            daysClosed = -1;
            Main.pref.putInteger(ConfigKeys.NOTES_SEARCH_DAYS_CLOSED, daysClosed);
        }

        final NoteSearchTask task = new NoteSearchTask("Searching for notes");
        task.initParams(searchTerm, noteLimit, daysClosed);
        Main.worker.submit(task);
        Main.worker.submit(new Runnable() {
            @Override
            public void run() {
                if (task.isCanceled() || task.isFailed()) return;
                List<Note> results = task.getFoundNotes();
                System.out.println("search results: " + results.size());
                plugin.getDataSet().clear();
                plugin.getDataSet().addAll(results);
                plugin.updateGui();
                dialog.setConnectionMode(true);
                dialog.showQueuePanel();
                Main.pref.put(ConfigKeys.NOTES_API_OFFLINE, true);
            }
        });
    }

    @Override
    public String toString() {
        return tr("Search term: " + searchTerm);
    }

    @Override
    public NotesAction clone() {
        SearchAction action = new SearchAction(dialog, plugin);
        action.canceled = canceled;
        action.searchTerm = searchTerm;
        return action;
    }

    private class NoteSearchTask extends PleaseWaitRunnable {

        public NoteSearchTask(String title) {
            super(title);
        }

        private String searchTerm;
        private Integer bugLimit;
        private Integer closedDays;
        private List<Note> returnedNotes;
        Boolean isCancelled = false;
        Boolean isFailed = false;

        public void initParams(String searchTerm, Integer bugLimit, Integer closedDays) {
            this.searchTerm = searchTerm;
            this.bugLimit = bugLimit;
            this.closedDays = closedDays;
            returnedNotes = new ArrayList<Note>();
        }

        @Override
        protected void cancel() {
            Main.info("note search cancelled");
            isCancelled = true;
        }

        @Override
        protected void realRun() throws SAXException, IOException,
        OsmTransferException {
            PleaseWaitProgressMonitor monitor = new PleaseWaitProgressMonitor();
            List<Note> noteList = NotesCapableOsmApi.getNotesApi().searchNotes(searchTerm, bugLimit, closedDays, monitor);
            Main.info("found notes: " + noteList.size());
            returnedNotes.addAll(noteList);
            //shouldn't be needed because the API calls finishTask but apparently the PleaseWaitProgressMonitor is buggy
            monitor.close();
        }

        @Override
        protected void finish() {
            // TODO Auto-generated method stub
        }

        public List<Note> getFoundNotes() {
            return returnedNotes;
        }

        public Boolean isCanceled() {
            return isCancelled;
        }

        public Boolean isFailed() {
            return isFailed;
        }
    }
}
