package org.openstreetmap.josm.plugins.notes.gui.action;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.util.LinkedList;

import javax.swing.AbstractListModel;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.progress.PleaseWaitProgressMonitor;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.plugins.notes.gui.NotesDialog;
import org.xml.sax.SAXException;

public class ActionQueue extends AbstractListModel<NotesAction> {

	private static final long serialVersionUID = 1L;
	
	private LinkedList<NotesAction> queue = new LinkedList<NotesAction>();
	
	private NotesDialog dialog;
	
	public ActionQueue(NotesDialog dialog) {
	    this.dialog = dialog;
	}

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
        final QueueProcessTask task = new QueueProcessTask(tr("Processing notes queue"));
        task.initParams(queue);
        Main.worker.submit(task);
    }

    public NotesAction getElementAt(int index) {
        return queue.get(index);
    }

    public int getSize() {
        return queue.size();
    }
    
    private class QueueProcessTask extends PleaseWaitRunnable {
        
        private boolean isCancelled = false;
        LinkedList<NotesAction> queue;
        
        public QueueProcessTask(String title) {
            super(title);
        }
        
        public void initParams(LinkedList<NotesAction> queue) {
            this.queue = queue;
        }
        
        @Override
        protected void realRun() throws SAXException, IOException,
        OsmTransferException {
            PleaseWaitProgressMonitor monitor = new PleaseWaitProgressMonitor();
            monitor.beginTask("Processing notes queue");
            int notesCount = queue.size();
            monitor.setTicksCount(notesCount);
            monitor.setCustomText("0/" + notesCount);
            int processedCount = 0;
            
            try {
                while(!queue.isEmpty() && !isCancelled) {
                    NotesAction action = queue.pop();
                    try {
                        action.execute();
                    }
                    catch(Exception e) {
                        Main.error("problem executing note action");
                        Main.error(e, true);
                        queue.add(action);
                    }
                    for (NotesActionObserver obs : action.getActionObservers()) {
                        obs.actionPerformed(action);
                    }
                    fireIntervalRemoved(this, 0, 0);
                    monitor.worked(1);
                    monitor.setCustomText(++processedCount + "/" + notesCount);
                    //Avoid infinite loop in case a note has a persistent upload error
                    if(processedCount >= notesCount) {
                        break;
                    }
                }
            }
            finally {
                monitor.close();
            }
        }
        
        @Override
        protected void cancel() {
            Main.info("note queue processing cancelled");
            isCancelled = true;
        }
        
        @Override
        protected void finish() {
            System.out.println("finish method called. queue: " + queue.size());
            if(queue.size() == 0) {
                dialog.hideQueuePanel();
            } else {
                Main.error("The queue was not 0 after processing!");
                JOptionPane.showMessageDialog(Main.parent, tr("Not all of your notes were uploaded. Please check the queue panel."));
            }
        }
    }
}
