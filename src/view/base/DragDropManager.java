package view.base;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import common.base.Logger;
import common.view.dnd.DropCommand;

/**
 * Singleton to handle drag-n-drop operations.
 * 
 * @author nicz
 *
 */
public class DragDropManager {
	
	private static final Logger log = new Logger("DragDropManager", true);
	
	/** the singleton instance */
	private static DragDropManager instance = null;
	
	private final Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
	
	/** Get the singleton DragDropManager instance. */
	public static DragDropManager getInstance() {
		if (instance == null)
			instance = new DragDropManager();
		return instance;
	}
	
	public void addDragSource(final Table table, final String tableId) {
		DragSource source = new DragSource(table, DND.DROP_MOVE);
		source.setTransfer(types);
		source.addDragListener(new DragSourceAdapter() {
			@Override
			public void dragSetData(DragSourceEvent event) {
				// Provide the index of the selected list item as a String.
				if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
					event.data = tableId + "=" + String.valueOf(table.getSelectionIndex());
				}
			}
		});
	}
	
	public void addDragSource(final Tree tree, final String treeId) {
		DragSource source = new DragSource(tree, DND.DROP_MOVE);
		source.setTransfer(types);
		source.addDragListener(new DragSourceAdapter() {
			@Override
			public void dragSetData(DragSourceEvent event) {
				// Provide the index of the selected list item as a String.
				if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
					event.data = treeId + "=" + String.valueOf(tree.getSelection()[0].getData());
				}
			}
		});
	}
	
	public void addDropTarget(final Tree tree, final DropCommand cmdDrop) {
		DropTarget target = new DropTarget(tree, DND.DROP_MOVE);
		target.setTransfer(types);
		target.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				if (event.data == null) {
					event.detail = DND.DROP_NONE;
					return;
				}
				String sourceId = (String) event.data;
				
				TreeItem item = (TreeItem) event.item;
				if (item == null) {
					event.detail = DND.DROP_NONE;
					return;
				}
				
				Integer listIndex = (Integer) item.getData();
				
				log.debug("Dropped source " + sourceId + " to taxon " + listIndex);
				
				cmdDrop.setSourceId(sourceId);
				cmdDrop.setTargetId(listIndex.intValue());
				cmdDrop.execute();
			}
			@Override
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
				if (event.item != null) {
					//TreeItem item = (TreeItem) event.item;
					event.feedback |= DND.FEEDBACK_SELECT;
				}
			}
		});
	}
	
	/** Private singleton constructor */
	private DragDropManager() {
	}
	

}
