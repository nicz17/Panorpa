package view;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import model.Taxon;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import view.base.ViewTools;

import common.base.Logger;
import controller.Controller;

/**
 * A Tree widget displaying the taxons.
 * 
 * @author nicz
 *
 */
public abstract class TaxonTree {
	
	private static final Logger log = new Logger("TaxonTree");

	private String treeId;
	private Tree tree;
	
	/** Map of Taxon by Taxon idx */
	private Map<Integer, Taxon> mapData;
	
	/** The selected taxon */
	private Taxon selection;
	
	/**
	 * Constructs a new tree of taxa.
	 * 
	 * @param treeId  the name of the tree
	 * @param parent  the parent composite
	 */
	public TaxonTree(String treeId, Composite parent) {
		
		this.mapData = new HashMap<Integer, Taxon>();
		this.treeId = treeId;
		this.selection = null;

		tree = new Tree(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Integer index = (Integer) event.item.getData();
				selection = mapData.get(index);
				onSelection(selection);
			}
		});
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (tree.getItem(new Point(e.x, e.y)) == null) {
					tree.deselectAll();
					selection = null;
					onSelection(null);
				}
			}
		});
	}
	
	/**
	 * Set the tree data to the specified list of top-level taxons.
	 * 
	 * @param vecTaxons  top-level taxons.
	 */
	public void setData(Vector<Taxon> vecTaxons) {
		clearData();
		for (Taxon taxon : vecTaxons) {
			mapData.put(taxon.getIdx(), taxon);
			TreeItem item = new TreeItem(tree, 0);
			setItemData(item, taxon);
			addChildren(item, taxon);
		}
	}

	public void updateData(Vector<Taxon> vecTaxons) {
		// update map
		mapData.clear();
		for (Taxon taxon : vecTaxons)
			updateMap(taxon);
		
		// update tree
		for (TreeItem treeItem : tree.getItems()) 
			updateTree(treeItem);
	}
	
	public void clearData() {
		mapData.clear();
		tree.removeAll();
		selection = null;
	}
	
	public String getTreeId() {
		return treeId;
	}
	
	public Tree getTree() {
		return tree;
	}
	
	public void setLayoutData(Object layoutData) {
		tree.setLayoutData(layoutData);
	}

	/**
	 * Gets the selected taxon.
	 * Returns null if nothing is selected.
	 * 
	 * @return the selected taxon (may be null)
	 */
	public Taxon getSelection() {
		return selection;
	}
	
	/**
	 * Tries to select the taxon with the specified database index.
	 * Expands and triggers a selection event if found.
	 * @param idxTaxon  the database index of taxon to select
	 */
	public void setSelection(int idxTaxon) {
		Taxon taxon = mapData.get(new Integer(idxTaxon));
		if (taxon != null) {
			tree.deselectAll();
			expandAll(false);
			expandTree(taxon);
		}
	}
	
//	public SelectionPath getSelectionPath() {
//		SelectionPath result = null;
//		if (selection != null) {
//			result = new SelectionPath(selection);
//		}
//		return result;
//	}
	
	/**
	 * Notifies that a tree item has been selected.
	 * 
	 * @param obj the selected object. May be null.
	 */
	public abstract void onSelection(Taxon obj);
	
	/**
	 * Searches the tree using the specified text.
	 * Expands all tree items corresponding to search query.
	 * @param strSearch  the search string
	 */
	public void searchInTree(String strSearch) {
		if (strSearch != null && !strSearch.isEmpty()) {
			strSearch = strSearch.replaceAll(" ", "%");
			Vector<Taxon> vecSearchResult = Controller.getInstance().getTaxons(null, strSearch);
			for (Taxon res : vecSearchResult) {
				expandTree(res);
			}
			//tree.showSelection();  // fails
//			if (!vecSearchResult.isEmpty()) {
//				showTaxon(vecSearchResult.lastElement());
//			}
		}
	}
	
	/**
	 * Expands and scrolls the tree to the specified taxon.
	 * @param taxon  the taxon to display in tree.
	 */
	public void showTaxon(Taxon taxon) {
		if (taxon != null) {
			TreeItem[] treeItems = tree.getItems();
			// find tree item
			for (TreeItem it : treeItems) {
				int dataIdx = (Integer) it.getData();
				if (dataIdx == taxon.getIdx()) {
					tree.showItem(it);
					break;
				}
			}
		}
	}

	/**
	 * Expands the tree to display the specified taxon.
	 * @param taxon  the taxon to display
	 */
	public void expandTree(Taxon taxon) {
		if (taxon == null) return;
		
		// reload from tree map to guarantee correct children/parents structure...
		taxon = mapData.get(taxon.getIdx());
		if (taxon == null) return;
		
		expandTree(new SelectionPath(taxon));
	}
	
	/**
	 * Expands the tree for the given selection path
	 * @param path  the path to expand
	 */
	public void expandTree(SelectionPath path) {
		if (path == null) return;  // nothing to do
		
		TreeItem[] treeItems = tree.getItems();
		for (Integer idxTaxon : path.getPath()) {
			// find tree item
			TreeItem theItem = null;
			for (TreeItem it : treeItems) {
				int dataIdx = (Integer) it.getData();
				if (dataIdx == idxTaxon.intValue()) {
					theItem = it;
					break;
				}
			}
			
			if (theItem == null) {
				// could not find item to expand.
				log.warn(treeId + ": failed to expand tree from " + path);
				return;
			} else {
				// expand it
				theItem.setExpanded(true);
				
				if (idxTaxon.equals(path.getPath().lastElement())) {
					// select it if last one
					tree.select(theItem);
					selection = mapData.get(idxTaxon);
					onSelection(selection);
				} else {
					// prepare next iteration
					treeItems = theItem.getItems();
					if (treeItems.length == 0)
						return;
				}
			}
		}
	}
	

	/**
	 * Expands or closes all tree items.
	 * 
	 * @param expand  true to expand
	 */
	public void expandAll(boolean expand) {
		TreeItem[] treeItems = tree.getItems();
		if (expand && tree.getSelectionCount() > 0) {
			treeItems = tree.getSelection();
		}
		
		if (treeItems != null) {
			for (TreeItem treeItem : treeItems) {
				expandAll(treeItem, expand);
			}
		}
	}

	private void expandAll(TreeItem treeItem, boolean expand) {
		if (treeItem != null) {
			treeItem.setExpanded(expand);
			for (TreeItem subItem : treeItem.getItems()) {
				expandAll(subItem, expand);
			}
		}
	}

	private void updateMap(Taxon taxon) {
		mapData.put(taxon.getIdx(), taxon);
		for (Taxon child : taxon.getChildren())
			updateMap(child);
	}
	
	private void updateTree(TreeItem item) {
		Taxon list = mapData.get(item.getData());
		if (list != null) {
			setItemData(item, mapData.get(item.getData()));
			for (TreeItem child : item.getItems())
				updateTree(child);			
		} else {
			item.dispose();
		}
	}

	private void setItemData(TreeItem item, final Taxon taxon) {
		item.setText(getItemLabel(taxon));
		item.setImage(ViewTools.getRankIcon(taxon.getRank()));
		item.setData(new Integer(taxon.getIdx()));
	}
	
	private String getItemLabel(final Taxon taxon) {
		String itemText = taxon.getName();
		itemText += " - " + taxon.getNameFr();
		if (!taxon.getChildren().isEmpty()) {
			itemText += " (" + taxon.getChildren().size() + ")";
		}
		return itemText;
	}
	
	private void addChildren(TreeItem item, Taxon taxon) {
		for (Taxon child : taxon.getChildren()) {
			mapData.put(child.getIdx(), child);
			TreeItem subitem = new TreeItem(item, 0);
			setItemData(subitem, child);
			addChildren(subitem, child);
		}
	}

}
