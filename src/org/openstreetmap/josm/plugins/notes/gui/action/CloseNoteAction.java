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
package org.openstreetmap.josm.plugins.notes.gui.action;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.widgets.HistoryChangedListener;
import org.openstreetmap.josm.io.OsmApiException;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.plugins.notes.ConfigKeys;
import org.openstreetmap.josm.data.notes.Note;
import org.openstreetmap.josm.plugins.notes.NotesPlugin;
import org.openstreetmap.josm.plugins.notes.api.CloseAction;
import org.openstreetmap.josm.plugins.notes.gui.NotesDialog;
import org.openstreetmap.josm.plugins.notes.gui.dialogs.TextInputDialog;

public class CloseNoteAction extends NotesAction {

    private static final long serialVersionUID = 1L;

    private CloseAction closeAction = new CloseAction();

    private String comment;

    private Note note;

    public CloseNoteAction(NotesDialog dialog) {
        super(tr("Mark as done"), dialog);
    }

    @Override
    protected void doActionPerformed(ActionEvent e) throws Exception {
        List<String> history = new LinkedList<String>(Main.pref.getCollection(ConfigKeys.NOTES_COMMENT_HISTORY, new LinkedList<String>()));
        HistoryChangedListener l = new HistoryChangedListener() {
            public void historyChanged(List<String> history) {
                Main.pref.putCollection(ConfigKeys.NOTES_COMMENT_HISTORY, history);
            }
        };
        note = dialog.getSelectedNote();
        comment = TextInputDialog.showDialog(Main.map,
                tr("Really close?"),
                tr("<html>Really mark this note as ''done''?<br><br>You may add an optional comment:</html>"),
                NotesPlugin.loadIcon("closed_note22.png"),
                history, l);

        if(comment == null) {
            canceled = true;
            return;
        }
        note.setState(Note.State.closed);
        dialog.refreshNoteStatus();

    }

    @Override
    public void execute() throws IOException {
        try {
            closeAction.execute(note, comment);
        } catch (OsmApiException e) {
            String reason;
            if (e.getErrorHeader().contains("capability")) {
                reason = tr("your JOSM OAuth key does not support modifying notes. Reauthenticate with OAuth, restart JOSM, and try again.");
            } else {
                reason = e.getErrorHeader();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    Main.parent,
                    tr("Could not create a new note because {0}", reason),
                    tr("Error create a new note"),
                    JOptionPane.WARNING_MESSAGE
            );
        } catch (OsmTransferException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    Main.parent,
                    tr("Could not create a new note because {0}", e.getMessage()),
                    tr("Error create a new note"),
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    @Override
    public String toString() {
        return tr("Close: Note " + note.getId() + " - Comment: " + comment);
    }

    @Override
    public CloseNoteAction clone() {
        CloseNoteAction action = new CloseNoteAction(dialog);
        action.canceled = canceled;
        action.comment = comment;
        action.note = note;
        return action;
    }
}
