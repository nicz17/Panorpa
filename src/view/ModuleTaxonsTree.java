package view;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.AppParam;
import model.AppParamName;
import model.HerbierPic;
import model.Taxon;
import model.TaxonRank;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;

import view.base.DragDropManager;
import view.base.MultiPhotoBox;
import view.base.ViewTools;

import common.base.Logger;
import common.exceptions.AppException;
import common.view.MessageBox;
import common.view.SashModule;
import common.view.SearchBox;
import common.view.dnd.DropCommand;

import controller.Controller;
import controller.TaxonCache;
import controller.listeners.DataListener;

public class ModuleTaxonsTree extends SashModule implements DataListener {
	
	private static final Logger log = new Logger("ModuleTaxonsTree", true);
	
	private TaxonTree   tree;
	private ToolBar toolbar;
	private SearchBox searchBox;
	private EditorTaxon editor;
	private MultiPhotoBox multiPhotoBox;
	
	public ModuleTaxonsTree() {
		super(Panorpa.getInstance().getFolder(), 750);
		
		setInitialSelection();
	}
	
	@Override
	protected void loadWidgets() {
		tree = new TaxonTree("TaxonTree", cLeft) {
			@Override
			public void onSelection(Taxon obj) {
				editor.showObject(obj);
				
				if (obj != null) {
					multiPhotoBox.setPics(new Vector<HerbierPic>(obj.getPicsCascade()), obj.getName());
				} else {
					multiPhotoBox.setPics(null, null);
				}
			}
		};
		
		Composite cBelowTree = new Composite(cLeft, 0);
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		gl.verticalSpacing = 0;
		cBelowTree.setLayout(gl);
	    cBelowTree.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		toolbar = widgetsFactory.createToolbar(cBelowTree, true);
		fillToolBar();
		
		searchBox = new SearchBox(cBelowTree) {
			@Override
			public void onSearch() {
				tree.searchInTree(searchBox.getSearchText());
			}
		};
		
		editor = new EditorTaxon(cRight);
		
		multiPhotoBox = new MultiPhotoBox(cRight);
		
		Controller.getInstance().addDataListener(this);
		
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				saveTaxonTreeSelection();
			}
		});
		
		// Drag-n-drop
		DragDropManager.getInstance().addDragSource(tree.getTree(), tree.getTreeId());
		DragDropManager.getInstance().addDropTarget(tree.getTree(), new DropCommand() {
			@Override
			public void execute() {
				executeDropAction(getSourceId(), getTargetId());
			}
		});
	}


	@Override
	public void pictureUpdated(int idx) {
		loadData();
	}

	@Override
	public void taxonUpdated(int idx) {
		loadData();
	}
	
	@Override
	public void locationUpdated(int idx) {
	}

	@Override
	public void selectObject(int idxTaxon) {
		tree.setSelection(idxTaxon);
	}
	
	/**
	 * Saves the current tree selection to application parameter.
	 */
	protected void saveTaxonTreeSelection() {
		Taxon sel = tree.getSelection();
		if (sel != null) {
			AppParam apSel = Controller.getInstance().getAppParam(AppParamName.TAXON_TREE_SEL);
			if (apSel != null) {
				apSel.setIntValue(sel.getIdx());
				Controller.getInstance().saveAppParam(apSel);
			}
		}
	}

	@Override
	protected void loadData() {
		// remember tree selection if needed
		Taxon selection = tree.getSelection();
		
		// reload tree
		Vector<Taxon> vecTaxons = new Vector<>(TaxonCache.getInstance().getTopLevel());
		tree.setData(vecTaxons);
		
		// restore selection if possible
		tree.expandTree(selection);
	}
	
	/**
	 * Creates a child for the selected taxon and displays it in editor.
	 */
	private void createChildTaxon() {
		Taxon selection = tree.getSelection();
		if (selection == null) {
			MessageBox.info("Choisir un taxon!");
		} else {
			TaxonRank rank = selection.getRank().getChildRank();
			if (rank != null) {
				Taxon child = new Taxon(0, "", rank);
				if (TaxonRank.SPECIES == rank) {
					child.setName(selection.getName() + " ");
					child.setNameFr(selection.getNameFr() + " ");
				}
				child.setParent(selection);
				editor.showObject(child);
				editor.setFocusToName();
			}
		}
	}
	
	private void executeDropAction(String sourceText, int targetId) {
		log.debug("Execute drop action from " + sourceText + " to " + targetId);
		Pattern patSource = Pattern.compile("(.+)=(.+)");
		Matcher mat = patSource.matcher(sourceText);
		if (mat.find()) {
			String sourceName = mat.group(1);
			int sourceIdx = Integer.valueOf(mat.group(2));
			log.debug("Drag source is " + sourceName + " at idx " + sourceIdx);
			
			if (tree.getTreeId().equals(sourceName)) {
				try {
					Controller.getInstance().setTaxonParent(sourceIdx, targetId);
				} catch (AppException e) {
					ViewTools.displayException(e);
				}
			} else {
				log.error("Unknown drag source : " + sourceText);
			}
		}
	}

	
//	/**
//	 * Searches the tree using the searchBox text.
//	 * Expands all tree items corresponding to search query.
//	 */
//	private void searchInTree() {
//		String strSearch = searchBox.getSearchText();
//		if (strSearch != null && !strSearch.isEmpty()) {
//			Vector<Taxon> vecSearchResult = Controller.getInstance().getTaxons(null, strSearch);
//			for (Taxon res : vecSearchResult) {
//				tree.expandTree(res);
//			}
//		}
//	}
	
	/**
	 * Adds buttons to the toolbar.
	 */
	private void fillToolBar() {
		// refresh button
		widgetsFactory.createToolItem(toolbar, "refresh", 
				"Tout recharger", new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Controller.getInstance().reloadCache();
				loadData();
			}
		});

		// expand all button
		widgetsFactory.createToolItem(toolbar, "redo", 
				"Tout ouvrir", new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tree.expandAll(true);
			}
		});

		// unexpand all button
		widgetsFactory.createToolItem(toolbar, "undo", 
				"Tout fermer", new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tree.expandAll(false);
			}
		});

		// add taxon button
		widgetsFactory.createToolItem(toolbar, "add", 
				"Créer sous-taxon", new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				createChildTaxon();
			}
		});
	}
	
	private void setInitialSelection() {
		AppParam apSel = Controller.getInstance().getAppParam(AppParamName.TAXON_TREE_SEL);
		if (apSel != null) {
			Integer idxTaxon = apSel.getIntValue();
			if (idxTaxon != null && idxTaxon.intValue() > 0) {
				selectObject(idxTaxon.intValue());
			}
		}
	}

}
