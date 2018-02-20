package view;

import java.util.Vector;

import model.Taxon;
import model.TaxonRank;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import view.base.AbstractEditor;
import view.base.ReadOnlySelector;
import view.base.ViewTools;

import common.exceptions.ValidationException;
import common.view.MessageBox;

import controller.Controller;

/**
 * Editor for Taxon properties.
 * 
 * @author nicz
 *
 */
public class EditorTaxon extends AbstractEditor {
	private Text txtName;
	private Text txtNameFr;
	private ReadOnlySelector<TaxonRank> selRank;
	private TaxonSelector selParent;
	private Spinner spiOrder;
	private Button chkTypical;
	
	private Button btnDelete;
	
	private Taxon theObject;
	
	public EditorTaxon(Composite parent) {
		super(parent);
		
		widgetsFactory.createLabel(cMain, "Nom latin");
		txtName = widgetsFactory.createText(cMain, 64, modifListener);
 
		widgetsFactory.createLabel(cMain, "Nom français");
		txtNameFr = widgetsFactory.createText(cMain, 64, modifListener);
		
		ModifyListener rankModifListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				selParent.setRank(selRank.getValue().getParentRank());
				enableWidgets(true);
			}
		};

		widgetsFactory.createLabel(cMain, "Rang");
		selRank = new ReadOnlySelector<TaxonRank>("RankSelector", cMain, rankModifListener) {
			@Override
			protected Vector<TaxonRank> getData() {
				return TaxonRank.getValuesVector();
			}

			@Override
			public String getDisplayValue(TaxonRank rank) {
				if (rank == null) return "(aucun)";
				return rank.getGuiName();
			}
		};

		widgetsFactory.createLabel(cMain, "Parent");
		selParent = new TaxonSelector("ParentTaxonSel", cMain, modifListener);
		
		widgetsFactory.createLabel(cMain, "Ordre");
		spiOrder = widgetsFactory.createSpinner(cMain, 0, 100, 1, modifListener);
		
		widgetsFactory.createLabel(cMain, "");
		chkTypical = widgetsFactory.createCheckBox(cMain, "Taxon-type du parent", false);
		chkTypical.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				enableWidgets(true);
			}
		});
		
		// delete button
		btnDelete = widgetsFactory.createDeleteButton(cButtons, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (MessageBox.askYesNo("Effacer le taxon " + theObject.getName() + " ?")) {
					try {
						Controller.getInstance().deleteTaxon(theObject);
					} catch (ValidationException exc) {
						ViewTools.displayException(exc);
					}
				}
			}
		});
		btnDelete.setEnabled(false);
	    
	    Controller.getInstance().addDataListener(this);
	    selRank.load();
	    selParent.load();
	}
	
	/**
	 * Save the object under edition.
	 */
	@Override
	protected void saveObject() {
		assert(theObject != null);
		
		theObject.setName(txtName.getText());
		theObject.setNameFr(txtNameFr.getText());
		theObject.setRank(selRank.getValue());
		theObject.setParent(selParent.getValue());
		theObject.setOrder(spiOrder.getSelection());
		theObject.setTypical(chkTypical.getSelection());
		
		try {
			Controller.getInstance().saveTaxon(theObject);
			enableWidgets(false);
		} catch (ValidationException e) {
			MessageBox.error(e.getMessage());
		}
	}

	@Override
	protected void cancel() {
		showObject(theObject);
	}
	
	/**
	 * Reset the editor, clearing all widgets.
	 */
	public void reset() {
		showObject(null);
	}
	
	public void setFocusToName() {
		txtName.setFocus();
	}
	
	@Override
	protected boolean hasUnsavedData() {
		if (theObject == null) return false;
		if (!txtName.getText().equals(theObject.getName())) return true;
		if (!txtNameFr.getText().equals(theObject.getNameFr())) return true;
		if (selRank.hasUnsavedData(theObject.getRank())) return true;
		if (selParent.hasUnsavedData(theObject.getParent())) return true;
		if (spiOrder.getSelection() != theObject.getOrder()) return true;
		if (chkTypical.getSelection() != theObject.isTypical()) return true;
		return false;
	}

	/**
	 * Display the given picture in the editor.
	 * @param obj the picture to display.
	 */
	public void showObject(Taxon obj) {
		if (obj != null) {
			theObject = obj;
			txtName.setText(obj.getName());
			txtNameFr.setText(obj.getNameFr());
			selRank.setValue(obj.getRank());
			selParent.setRank(obj.getRank().getParentRank());
			selParent.setValue(obj.getParent());
			spiOrder.setSelection(obj.getOrder());
			chkTypical.setSelection(obj.isTypical());
			btnDelete.setEnabled(true);
			enableWidgets(true);
		} else {
			theObject = null;
			txtName.setText("");
			txtNameFr.setText("");
			selRank.clearDisplay();
			selParent.clearDisplay();
			spiOrder.setSelection(0);
			chkTypical.setSelection(false);
			btnDelete.setEnabled(false);
			enableWidgets(false);
		}
	}

}
