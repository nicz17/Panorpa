package view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import model.Expedition;
import model.Location;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import common.exceptions.ValidationException;
import common.view.MessageBox;

import view.base.AbstractEditor;
import controller.Controller;

/**
 * Editor for Expedition properties.
 * 
 * @author nicz
 *
 */
public class EditorExpedition extends AbstractEditor {
	
	private static final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	
	private Text txtTitle, txtNotes;
	private LocationSelector selLocation;
	private Label lblDateFrom, lblDateTo;
	private Label lblNPics;
	
	private Expedition theObject;
	
	public EditorExpedition(Composite parent) {
		super(parent);
		
		widgetsFactory.createLabel(cMain, "Titre");
		txtTitle = widgetsFactory.createText(cMain, 64, modifListener);

		widgetsFactory.createLabel(cMain, "Notes", true);
		txtNotes = widgetsFactory.createMultilineText(cMain, 
				1024, 125, modifListener);
		
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

		widgetsFactory.createLabel(cMain, "Début");
		lblDateFrom = widgetsFactory.createLabel(cMain, 250);

		widgetsFactory.createLabel(cMain, "Fin");
		lblDateTo = widgetsFactory.createLabel(cMain, 250);

		widgetsFactory.createLabel(cMain, "Photos");
		lblNPics = widgetsFactory.createLabel(cMain, 250);

		Controller.getInstance().addDataListener(this);
	    selLocation.load();
	}
	
	/**
	 * Save the object under edition.
	 */
	@Override
	protected void saveObject() {
		assert(theObject != null);
		
		theObject.setTitle(txtTitle.getText());
		theObject.setNotes(txtNotes.getText());
		theObject.setLocation(selLocation.getValue());
		
		try {
			Controller.getInstance().saveExpedition(theObject);
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
		if (!txtTitle.getText().equals(theObject.getTitle())) return true;
		if (!txtNotes.getText().equals(theObject.getNotes())) return true;
		if (selLocation.hasUnsavedData(theObject.getLocation())) return true;
		return false;
	}

	/**
	 * Display the given expedition in the editor.
	 * @param obj the expedition to display.
	 */
	public void showObject(Expedition obj) {
		if (obj != null) {
			theObject = obj;
			txtTitle.setText(obj.getTitle());
			txtNotes.setText(obj.getNotes());
			selLocation.setValue(obj.getLocation());
			lblDateFrom.setText(obj.getDateFrom() == null ? "-" : dateFormat.format(obj.getDateFrom()));
			lblDateTo.setText(obj.getDateTo() == null ? "-" : dateFormat.format(obj.getDateTo()));
			lblNPics.setText(String.valueOf(obj.getPics().size()));
			enableWidgets(true);
		} else {
			theObject = null;
			txtTitle.setText("");
			txtNotes.setText("");
			selLocation.clearDisplay();
			lblDateFrom.setText("");
			lblDateTo.setText("");
			lblNPics.setText("");
			enableWidgets(false);
		}
	}

}
