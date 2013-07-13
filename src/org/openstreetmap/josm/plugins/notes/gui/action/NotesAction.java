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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.notes.ConfigKeys;
import org.openstreetmap.josm.plugins.notes.gui.NotesDialog;

public abstract class NotesAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private List<NotesActionObserver> observers = new ArrayList<NotesActionObserver>();

    protected final NotesDialog dialog;

    protected boolean canceled = false;
    protected final ActionQueue actionQueue;

    public NotesAction(String name, NotesDialog osbDialog) {
        super(name);
        this.dialog = osbDialog;
        this.actionQueue = osbDialog.getActionQueue();
    }

    public void actionPerformed(ActionEvent e) {
        canceled = false;
        try {
            doActionPerformed(e);
            if(!canceled) {
                if (!Main.pref.getBoolean(ConfigKeys.NOTES_API_OFFLINE)) {
                    execute();
                    for (NotesActionObserver obs : observers) {
                        obs.actionPerformed(this);
                    }
                } else {
                    NotesAction action = clone();
                    actionQueue.offer(action);
                }
            }
        } catch (Exception e1) {
            System.err.println("Couldn't execute action " + getClass().getSimpleName());
            e1.printStackTrace();
        }
    }

    protected abstract void doActionPerformed(ActionEvent e) throws Exception;

    public void addActionObserver(NotesActionObserver obs) {
        observers.add(obs);
    }

    public void removeActionObserver(NotesActionObserver obs) {
        observers.remove(obs);
    }

    public List<NotesActionObserver> getActionObservers() {
        return observers;
    }

    public abstract void execute() throws Exception;

    @Override
    public abstract NotesAction clone();
}
