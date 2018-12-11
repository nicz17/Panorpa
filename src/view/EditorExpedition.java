package view;

import model.Expedition;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import view.base.AbstractEditor;
import controller.Controller;

/**
 * Editor for Expedition properties.
 * 
 * @author nicz
 *
 */
public class EditorExpedition extends AbstractEditor {
	private Text txtTitle, txtNotes;
	private LocationSelector selLocation;
	
	private Expedition theObject;
	
	public EditorExpedition(Composite parent) {
		super(parent);
		
		widgetsFactory.createLabel(cMain, "Titre");
		txtTitle = widgetsFactory.createText(cMain, 64, modifListener);

		widgetsFactory.createLabel(cMain, "Notes", true);
		txtNotes = widgetsFactory.createMultilineText(cMain, 
				1024, 125, modifListener);

		widgetsFactory.createLabel(cMain, "Lieu");
		selLocation = new LocationSelector("LocationSelector", cMain, false, modifListener);

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
		
//		try {
//			Controller.getInstance().saveExpedition(theObject);
//			enableWidgets(false);
//		} catch (ValidationException e) {
//			MessageBox.error(e.getMessage());
//		}
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
			enableWidgets(true);
		} else {
			theObject = null;
			txtTitle.setText("");
			txtNotes.setText("");
			selLocation.clearDisplay();
			enableWidgets(false);
		}
	}

}
