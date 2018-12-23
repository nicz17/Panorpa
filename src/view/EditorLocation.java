package view;

import model.Location;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import view.base.AbstractEditor;

import common.exceptions.ValidationException;
import common.view.LonLatZoomSelector;
import common.view.LonLatZoomSelector.LonLatZoomSelectionListener;
import common.view.MessageBox;

import controller.Controller;

/**
 * Editor for Location properties.
 * 
 * @author nicz
 *
 */
public class EditorLocation extends AbstractEditor {
	private Text txtName, txtDesc, txtKind, txtRegion, txtTown, txtState;
	private Spinner spiAltitude;
	private Button btnDefaultLocation;
	private LonLatZoomSelector selMapCoords;
	
	private Location theObject;
	
	public EditorLocation(Composite parent) {
		super(parent);
		
		widgetsFactory.createLabel(cMain, "Nom");
		txtName = widgetsFactory.createText(cMain, 64, modifListener);

		widgetsFactory.createLabel(cMain, "Description", true);
		txtDesc = widgetsFactory.createMultilineText(cMain, 
				1024, 125, modifListener);

		widgetsFactory.createLabel(cMain, "Type", true);
		txtKind = widgetsFactory.createText(cMain, 64, modifListener);

		widgetsFactory.createLabel(cMain, "Localité", true);
		txtTown = widgetsFactory.createText(cMain, 64, modifListener);

		widgetsFactory.createLabel(cMain, "Région", true);
		txtRegion = widgetsFactory.createText(cMain, 64, modifListener);

		widgetsFactory.createLabel(cMain, "Pays", true);
		txtState = widgetsFactory.createText(cMain, 64, modifListener);
		
		widgetsFactory.createLabel(cMain, "Altitude (m)");
		spiAltitude = widgetsFactory.createSpinner(cMain, 0, 9000, 100, modifListener);
		
		widgetsFactory.createLabel(cMain, "Carte");
		selMapCoords = new LonLatZoomSelector(cMain);
		selMapCoords.addSelectionListener(new LonLatZoomSelectionListener() {
			@Override
			public void onSelectionChange() {
				enableWidgets(true);
			}
		});
		
		btnDefaultLocation = widgetsFactory.createPushButton(
	    		cButtons, "Lieu par défaut", "star", 
	    		"Définir comme lieu par défault pour les nouvelles photos",
	    		new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Controller.getInstance().setDefaultLocation(theObject);
			}
		});

		Controller.getInstance().addDataListener(this);
	}
	
	/**
	 * Save the object under edition.
	 */
	@Override
	protected void saveObject() {
		assert(theObject != null);
		
		theObject.setName(txtName.getText());
		theObject.setDescription(txtDesc.getText());
		theObject.setKind(txtKind.getText());
		theObject.setTown(txtTown.getText());
		theObject.setRegion(txtRegion.getText());
		theObject.setState(txtState.getText());
		theObject.setAltitude(spiAltitude.getSelection());
		theObject.setLongitude(selMapCoords.getLongitude());
		theObject.setLatitude(selMapCoords.getLatitude());
		theObject.setMapZoom(selMapCoords.getZoom());
		
		try {
			Controller.getInstance().saveLocation(theObject);
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
	
	@Override
	protected boolean hasUnsavedData() {
		if (theObject == null) return false;
		if (!txtName.getText().equals(theObject.getName())) return true;
		if (!txtDesc.getText().equals(theObject.getDescription())) return true;
		if (!txtKind.getText().equals(theObject.getKind())) return true;
		if (!txtTown.getText().equals(theObject.getTown())) return true;
		if (!txtRegion.getText().equals(theObject.getRegion())) return true;
		if (!txtState.getText().equals(theObject.getState())) return true;
		if (spiAltitude.getSelection() != theObject.getAltitude()) return true;
		if (selMapCoords.getZoom() != theObject.getMapZoom()) return true;
		if (selMapCoords.getLongitude() != null && selMapCoords.getLatitude() != null) {
			if (theObject.getLongitude() == null || theObject.getLatitude() == null) {
				return true;
			} else if (selMapCoords.getLongitude().doubleValue() != theObject.getLongitude().doubleValue()) {
				return true;
			} else if (selMapCoords.getLatitude().doubleValue() != theObject.getLatitude().doubleValue()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Display the given location in the editor.
	 * @param obj the location to display.
	 */
	public void showObject(Location obj) {
		if (obj != null) {
			theObject = obj;
			txtName.setText(obj.getName());
			txtDesc.setText(obj.getDescription());
			txtTown.setText(obj.getTown());
			txtRegion.setText(obj.getRegion());
			txtState.setText(obj.getState());
			txtKind.setText(obj.getKind());
			spiAltitude.setSelection(obj.getAltitude());
			selMapCoords.setSelection(obj.getLongitude(), obj.getLatitude(), obj.getMapZoom());
			btnDefaultLocation.setEnabled(true);
			enableWidgets(true);
		} else {
			theObject = null;
			txtName.setText("");
			txtDesc.setText("");
			txtKind.setText("");
			txtTown.setText("");
			txtRegion.setText("");
			txtState.setText("");
			spiAltitude.setSelection(0);
			selMapCoords.clear();
			btnDefaultLocation.setEnabled(false);
			enableWidgets(false);
		}
	}

}
