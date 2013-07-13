package org.openstreetmap.josm.plugins.notes.gui.action;

import java.util.LinkedList;

import javax.swing.AbstractListModel;

public class ActionQueue extends AbstractListModel {

    private LinkedList<NotesAction> queue = new LinkedList<NotesAction>();

    public boolean offer(NotesAction e) {
        boolean result = queue.offer(e);
        fireIntervalAdded(this, queue.size()-1, queue.size()-1);
        return result;
    }

    public NotesAction peek() {
        return queue.peek();
    }

    public NotesAction poll() {
        NotesAction action = queue.poll();
        fireIntervalRemoved(this, 0, 0);
        return action;
    }

    public boolean remove(Object o) {
        int index = queue.indexOf(o);
        if(index >= 0) {
            fireIntervalRemoved(this, index, index);
        }
        return queue.remove(o);
    }

    public void processQueue() throws Exception {
        while(!queue.isEmpty()) {
            // get the first action, but leave it in queue
            NotesAction action = queue.peek();

            // execute the action
            action.execute();

            // notify observers
            for (NotesActionObserver obs : action.getActionObservers()) {
                obs.actionPerformed(action);
            }

            // if no exception has been thrown, remove the action from the queue
            queue.remove();
            fireIntervalRemoved(this, 0, 0);
        }
    }

    public Object getElementAt(int index) {
        return queue.get(index);
    }

    public int getSize() {
        return queue.size();
    }
}
