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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.widgets.HistoryChangedListener;
import org.openstreetmap.josm.plugins.notes.ConfigKeys;
import org.openstreetmap.josm.plugins.notes.Note;
import org.openstreetmap.josm.plugins.notes.NotesPlugin;
import org.openstreetmap.josm.plugins.notes.api.NewAction;
import org.openstreetmap.josm.plugins.notes.gui.dialogs.TextInputDialog;

public class NewNoteAction extends NotesAction {

    private static final long serialVersionUID = 1L;

    private NotesPlugin plugin;

    private String result;

    private Point p;

    private NewAction newAction = new NewAction();

    public NewNoteAction(NotesPlugin plugin, Point p) {
        super(tr("New note"), plugin.getDialog());
        this.plugin = plugin;
        this.p = p;
    }

    @Override
    protected void doActionPerformed(ActionEvent e) throws IOException, InterruptedException {
        List<String> history = new LinkedList<String>(Main.pref.getCollection(ConfigKeys.NOTES_NEW_HISTORY, new LinkedList<String>()));
        HistoryChangedListener l = new HistoryChangedListener() {
            public void historyChanged(List<String> history) {
                Main.pref.putCollection(ConfigKeys.NOTES_NEW_HISTORY, history);
            }
        };

        result = TextInputDialog.showDialog(
                Main.map,
                tr("Create note"),
                tr("Describe the problem precisely"),
                NotesPlugin.loadIcon("icon_error_add22.png"),
                history, l);

        if(result == null) {
            canceled = true;
        }
    }

    @Override
    public void execute() throws IOException {
        if (result.length() > 0) {
            Note n = newAction.execute(p, result);
            plugin.getDataSet().add(n);
            if (Main.pref.getBoolean(ConfigKeys.NOTES_API_DISABLED)) {
                plugin.updateGui();
            } else {
                plugin.updateData();
            }
        }
    }

    @Override
    public String toString() {
        return tr("Create: " + result);
    }

    @Override
    public NotesAction clone() {
        NewNoteAction action = new NewNoteAction(plugin, p);
        action.canceled = canceled;
        action.p = p;
        action.result = result;
        return action;
    }
}
