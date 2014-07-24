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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.openstreetmap.josm.plugins.notes.NotesPlugin;
import org.openstreetmap.josm.plugins.notes.gui.action.AddCommentAction;
import org.openstreetmap.josm.plugins.notes.gui.action.CloseNoteAction;
import org.openstreetmap.josm.plugins.notes.gui.action.NewNoteAction;
import org.openstreetmap.josm.plugins.notes.gui.action.NotesAction;
import org.openstreetmap.josm.plugins.notes.gui.action.ReopenAction;

public class NotesQueueListCellRenderer implements ListCellRenderer<NotesAction> {

    private Color background = Color.WHITE;
    private Color altBackground = new Color(250, 250, 220);

    public Component getListCellRendererComponent(JList<? extends NotesAction> list, NotesAction action, int index, boolean isSelected,
            boolean cellHasFocus) {
    	
    	

        JLabel label = new JLabel();
        label.setOpaque(true);

        if(isSelected) {
            label.setForeground(UIManager.getColor("List.selectionForeground"));
            label.setBackground(UIManager.getColor("List.selectionBackground"));
        } else {
            label.setForeground(UIManager.getColor("List.foreground"));
            label.setBackground(index % 2 == 0 ? background : altBackground);
        }

        Icon icon = null;
        if(action instanceof NewNoteAction) {
            icon = NotesPlugin.loadIcon("new_note16.png");
        } else if(action instanceof AddCommentAction) {
            icon = NotesPlugin.loadIcon("add_comment16.png");
        } else if(action instanceof CloseNoteAction) {
            icon = NotesPlugin.loadIcon("closed_note16.png");
        } else if(action instanceof ReopenAction) {
        	icon = NotesPlugin.loadIcon("reopen_note16.png");
        }
        label.setIcon(icon);
        String text = action.toString();
        if(text.indexOf("<hr />") > 0) {
            text = text.substring(0, text.indexOf("<hr />"));
        }
        label.setText("<html>" + text + "</html>");

        Dimension d = label.getPreferredSize();
        d.height += 10;
        label.setPreferredSize(d);

        return label;
    }

}
