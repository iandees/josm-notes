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

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.openstreetmap.josm.plugins.notes.Note;
import org.openstreetmap.josm.plugins.notes.NotesPlugin;
import org.openstreetmap.josm.plugins.notes.gui.NotesDialog;

public class PopupFactory {

    private static JPopupMenu issuePopup;
    private static JPopupMenu fixedPopup;

    public static synchronized JPopupMenu createPopup(Note note, NotesDialog dialog) {
    	switch (note.getState()) {
		case open:
			return getIssuePopup(dialog);
		case closed:
			return getFixedPopup(dialog);
		default:
			throw new RuntimeException(tr("Unknown note state"));
		}
    }

    private static JPopupMenu getIssuePopup(NotesDialog dialog) {
        if(issuePopup == null) {
            issuePopup = new JPopupMenu();
            JMenuItem add = new JMenuItem();
            add.setAction(new AddCommentAction(dialog));
            add.setIcon(NotesPlugin.loadIcon("add_comment16.png"));
            issuePopup.add(add);
            JMenuItem close = new JMenuItem();
            close.setAction(new CloseNoteAction(dialog));
            close.setIcon(NotesPlugin.loadIcon("closed_note16.png"));
            issuePopup.add(close);
            JMenuItem reopen = new JMenuItem();
            reopen.setAction(new ReopenAction(dialog));
            reopen.setIcon(NotesPlugin.loadIcon("reopen_note16.png"));
            reopen.setEnabled(false);
            issuePopup.add(reopen);
            JMenuItem openInBrowser = new JMenuItem();
            openInBrowser.setAction(new OpenInBrowserAction(dialog));
            openInBrowser.setIcon(NotesPlugin.loadIcon("internet-web-browser.png"));
            issuePopup.add(openInBrowser);
        }
        return issuePopup;
    }

    private static JPopupMenu getFixedPopup(NotesDialog dialog) {
        if(fixedPopup == null) {
            fixedPopup = new JPopupMenu();
            JMenuItem add = new JMenuItem();
            AddCommentAction aca = new AddCommentAction(dialog);
            aca.setEnabled(false);
            add.setAction(aca);
            add.setIcon(NotesPlugin.loadIcon("add_comment16.png"));
            fixedPopup.add(add);
            JMenuItem close = new JMenuItem();
            CloseNoteAction cia = new CloseNoteAction(dialog);
            cia.setEnabled(false);
            close.setAction(cia);
            close.setIcon(NotesPlugin.loadIcon("closed_note16.png"));
            fixedPopup.add(close);
            JMenuItem reopen = new JMenuItem();
            reopen.setAction(new ReopenAction(dialog));
            reopen.setIcon(NotesPlugin.loadIcon("reopen_note16.png"));
            fixedPopup.add(reopen);
            JMenuItem openInBrowser = new JMenuItem();
            openInBrowser.setAction(new OpenInBrowserAction(dialog));
            openInBrowser.setIcon(NotesPlugin.loadIcon("internet-web-browser.png"));
            fixedPopup.add(openInBrowser);
        }
        return fixedPopup;
    }
}
