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
package org.openstreetmap.josm.plugins.notes.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.SimpleDateFormat;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.openstreetmap.josm.plugins.notes.Note;
import org.openstreetmap.josm.plugins.notes.Note.Comment;
import org.openstreetmap.josm.plugins.notes.NotesPlugin;

public class NotesBugListCellRenderer implements ListCellRenderer {

    private Color background = Color.WHITE;
    private Color altBackground = new Color(250, 250, 220);

    private SimpleDateFormat dayFormat = new SimpleDateFormat("MMM d, yyyy");
	
    public Component getListCellRendererComponent(JList list, Object n, int index, boolean isSelected,
            boolean cellHasFocus) {
    	
    	Note note = (Note)n;

        JLabel label = new JLabel();
        label.setOpaque(true);

        if(isSelected) {
            label.setForeground(UIManager.getColor("List.selectionForeground"));
            label.setBackground(UIManager.getColor("List.selectionBackground"));
        } else {
            label.setForeground(UIManager.getColor("List.foreground"));
            label.setBackground(index % 2 == 0 ? background : altBackground);
        }

        if(!list.isEnabled()) {
            label.setForeground(UIManager.getColor("Label.disabledForeground"));
        }

        Icon icon = null;
        switch(note.getState()) {
            case closed:
                icon = NotesPlugin.loadIcon("closed_note16.png");
                break;
            case open:
                icon = NotesPlugin.loadIcon("open_note16.png");
                break;
        }
        label.setIcon(icon);

        StringBuilder sb = new StringBuilder();
        Comment firstComment = note.getFirstComment();
        String text = firstComment.getText();
        text = text.replace("\n", " ");
        sb.append(text);
        sb.append(" (");
        String userName = firstComment.getUser().getName();
        if(userName == null || userName.trim().length() == 0) {
        	userName = "<Anonymous>";
        }
        sb.append(userName);
        sb.append(tr(" on"));
        sb.append(" ");
        sb.append(dayFormat.format(firstComment.getCreatedAt()));
        sb.append(")");
        label.setText(sb.toString());

        Dimension d = label.getPreferredSize();
        d.height += 10;
        label.setPreferredSize(d);

        label.setEnabled(list.isEnabled());
        return label;
    }



}
