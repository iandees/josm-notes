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

import java.util.Iterator;

import org.openstreetmap.josm.plugins.notes.Note;


public class NotesListItem {
    private Note note;

    public NotesListItem(Note node) {
        super();
        this.note = node;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note node) {
        this.note = node;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<html>");
        Iterator<Note.Comment> iterator = getNote().getComments().iterator();
        while(iterator.hasNext()) {
            Note.Comment comment = iterator.next();
            sb.append(comment.getText());
            sb.append("<br/>");
        }
        sb.append("</html>");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof NotesListItem) {
            NotesListItem other = (NotesListItem)obj;
            if(getNote() != null && other.getNote() != null) {
                return getNote().getId() == other.getNote().getId();
            }
        }

        return false;
    }
}
