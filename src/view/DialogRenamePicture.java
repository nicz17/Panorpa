package view;

import java.util.Vector;

import model.HerbierPic;
import model.Taxon;
import model.TaxonRank;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import view.base.ViewTools;

import common.exceptions.AppException;
import common.view.WidgetsFactory;

import controller.Controller;
import controller.TaxonCache;

/**
 * A dialog window used to rename a picture according
 * to a taxon selected from a tree.
 * 
 * @author nicz
 *
 */
public class DialogRenamePicture {
	
	private static final WidgetsFactory widgetsFactory =
			WidgetsFactory.getInstance();

	private HerbierPic hpic;
	private Taxon newTaxon;
	private List listPics;
	private Shell  parent;
	private TaxonTree tree;
	private Button btnSave;
	private Label  lblOldName, lblNewName;
	
	public DialogRenamePicture(Shell parent) {
		this.parent = parent;
	}
	
	public void open(HerbierPic hpic) {
		
		// TODO new file name is loaded twice on open...
		
		this.hpic = hpic;
		this.newTaxon = null;
		
		Display display = parent.getDisplay();
		final Shell shell =
			new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
		shell.setText("Reclasser " + hpic.getFileName());
		shell.setLayout(new GridLayout(2, false));

		GridData data = new GridData();
		//data.widthHint = 1200;
		//shell.setLayoutData(data);
		
		Composite cLeft  = new Composite(shell, 0);
		Composite cRight = new Composite(shell, 0);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 10;
		cLeft.setLayout(layout);
		layout = new GridLayout();
		layout.verticalSpacing = 10;
		cRight.setLayout(layout);
		data = new GridData();
		data.widthHint = 500;
		cRight.setLayoutData(data);
	    
	    tree = new TaxonTree("dlgRenamePicTaxonTree", cLeft) {
			@Override
			public void onSelection(Taxon taxon) {
				newTaxon = taxon;
				if (newTaxon != null) {
					listPics.removeAll();
					for (HerbierPic hpic : newTaxon.getPicsCascade()) {
						listPics.add(hpic.getFileName());
					}
				}
				getNewName();
			}
		};
		data = new GridData();
		data.heightHint = 600;
		data.widthHint  = 500;
		tree.setLayoutData(data);
		
		widgetsFactory.createLabel(cRight, "Photos existantes :");
		listPics = widgetsFactory.createList(cRight, 500, 400);
		
		Group grpNames = widgetsFactory.createGroup(cRight, "Renommer photo", 2, false);
		
		widgetsFactory.createLabel(grpNames, "Nom actuel :");
		lblOldName = widgetsFactory.createLabel(grpNames);
		widgetsFactory.createLabel(grpNames, "Nouveau nom :");
		lblNewName = widgetsFactory.createLabel(grpNames);
		
		Composite cButtons = widgetsFactory.createComposite(cRight, 3, true, 12);
		 
		btnSave = widgetsFactory.createSaveButton(cButtons,
				new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				save();
				shell.dispose();
			}
		});
		
		widgetsFactory.createCancelButton(cButtons, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.dispose();
			}
		});
		
		Vector<Taxon> vecTaxons = new Vector<>(TaxonCache.getInstance().getTopLevel());
		tree.setData(vecTaxons);
		showObject(hpic);

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
	}
	
	private void getNewName() {
		if (newTaxon != null) {
			try {
				String newName = Controller.getInstance().getAvailableFilename(hpic, newTaxon);
				lblNewName.setText(newName);
			} catch (AppException e) {
				ViewTools.displayException(e);
			}
		}
		enableButtons();
	}

	private void showObject(HerbierPic pic) {
		tree.expandTree(hpic.getTaxon());
		lblOldName.setText(hpic.getFileName());
		lblNewName.setText(hpic.getFileName());
		enableButtons();
	}
	
	private boolean hasModifs() {
		if (!(lblOldName.getText().equals(lblNewName.getText()))) return true;
		if (newTaxon == null) return false;
		TaxonRank rank = newTaxon.getRank();
		if ( (TaxonRank.SPECIES == rank || TaxonRank.GENUS == rank) &&
				!hpic.getTaxon().equals(newTaxon)) return true;  // only for species, genus...
		return false;
	}
	
	private void enableButtons() {
		btnSave.setEnabled(hasModifs());
	}
	
	private void save() {
		if (hasModifs()) {
			try {
				Controller.getInstance().renamePic(hpic, newTaxon, lblNewName.getText());
			} catch (AppException e) {
				ViewTools.displayException(e);
			}
		}
	}

}
