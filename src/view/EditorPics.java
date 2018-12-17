package view;

import model.HerbierPic;
import model.Location;
import model.Taxon;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import view.base.AbstractEditor;
import view.base.ViewTools;

import common.exceptions.ValidationException;
import common.view.MessageBox;

import controller.Controller;

/**
 * Editor for Picture properties.
 * 
 * @author nicz
 *
 */
public class EditorPics extends AbstractEditor {
	
	private Text txtRemark;
	private LocationSelector selLocation;
	private Label lblName, lblNameFr, lblDate, lblFile, lblFamily;
	private Spinner spiRating;
	private Button btnRename;
	private Button btnDelete;
	
	private HerbierPic theObject;
	
	public EditorPics(Composite parent) {
		super(parent);
		
		widgetsFactory.createLabel(cMain, "Nom latin");
		lblName = widgetsFactory.createLabel(cMain);
 
		widgetsFactory.createLabel(cMain, "Nom fr.");
		lblNameFr = widgetsFactory.createLabel(cMain);
		
		widgetsFactory.createLink(cMain, "<a>Classification</a>", 
				"Voir cette photo dans la classification des espèces", new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						if (theObject != null) {
							Taxon taxon = theObject.getTaxon();
							if (taxon != null) {
								Panorpa.getInstance().navigate(Module.TAXA, taxon.getIdx());
							}
						}
					}
				});
		lblFamily = widgetsFactory.createLabel(cMain);
		 
		widgetsFactory.createLabel(cMain, "Photo");
		lblFile = widgetsFactory.createLabel(cMain);
 
		widgetsFactory.createLabel(cMain, "Date");
		lblDate = widgetsFactory.createLabel(cMain);
		
		widgetsFactory.createLink(cMain, "<a>Lieu</a>", 
				"Editer le lieu", new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						if (theObject != null) {
							Location location = theObject.getLocation();
							if (location != null) {
								Panorpa.getInstance().navigate(Module.LOCATIONS, location.getIdx());
							}
						}
					}
				});
		selLocation = new LocationSelector("LocationSelector", cMain, false, modifListener);
 
		widgetsFactory.createLabel(cMain, "Remarques", true);
		txtRemark = widgetsFactory.createMultilineText(cMain, 
				512, 125, modifListener);
		 
		widgetsFactory.createLabel(cMain, "Qualité", true);
		spiRating = widgetsFactory.createSpinner(cMain, 1, 5, 1, modifListener);
		
		btnRename = widgetsFactory.createPushButton(
	    		cButtons, "Reclasser", "edit", "Reclasser et renommer la photo",
	    		new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DialogRenamePicture dlg = new DialogRenamePicture(getShell());
				dlg.open(theObject);
			}
		});
		
		// delete button
		btnDelete = widgetsFactory.createDeleteButton(cButtons, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (MessageBox.askYesNo("Effacer la photo de " + theObject.getName() + 
						"\n" + theObject.getFileName() + " ?")) {
					try {
						Controller.getInstance().deletePicture(theObject);
					} catch (ValidationException exc) {
						ViewTools.displayException(exc);
					}
				}
			}
		});
		btnDelete.setEnabled(false);

		Controller.getInstance().addDataListener(this);
	    selLocation.load();
	}
	
	/**
	 * Save the object under edition.
	 */
	@Override
	protected void saveObject() {
		assert(theObject != null);
		
		theObject.setLocation(selLocation.getValue());
		theObject.setRemarks(txtRemark.getText());
		theObject.setRating(spiRating.getSelection());
		
		try {
			Controller.getInstance().savePicture(theObject);
			enableWidgets(false);
		} catch (ValidationException e) {
			MessageBox.error(e.getMessage());
		}
		
		if (theObject != null) {  // may have changed through save...
			selLocation.setValue(theObject.getLocation());
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
	
	@Override
	protected boolean hasUnsavedData() {
		if (theObject == null) return false;
		if (selLocation.hasUnsavedData(theObject.getLocation())) return true;
		if (!txtRemark.getText().equals(theObject.getRemarks())) return true;
		if (spiRating.getSelection() != theObject.getRating()) return true;
		
		return false;
	}

	/**
	 * Display the given picture in the editor.
	 * @param obj the picture to display.
	 */
	public void showObject(HerbierPic obj) {
		if (obj != null) {
			theObject = obj;
			lblName.setText(obj.getName());
			lblNameFr.setText(obj.getFrenchName());
			txtRemark.setText(obj.getRemarks());
			selLocation.setValue(obj.getLocation());
			lblDate.setText(Panorpa.dateTimeFormat.format(obj.getShotAt()));
			lblFile.setText(obj.getFileName());
			lblFamily.setText(obj.getClassification());
			spiRating.setSelection(obj.getRating());
			btnDelete.setEnabled(true);
			enableWidgets(true);
		} else {
			theObject = null;
			lblName.setText("");
			lblNameFr.setText("");
			txtRemark.setText("");
			selLocation.clearDisplay();
			lblDate.setText("");
			lblFile.setText("");
			lblFamily.setText("");
			spiRating.setSelection(3);
			btnDelete.setEnabled(false);
			enableWidgets(false);
		}
		btnRename.setEnabled(obj != null);
	}
	
	@Override
	public void locationUpdated(int idx) {
		selLocation.load();
	}

}
