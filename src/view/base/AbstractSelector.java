package view.base;

import java.util.Vector;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import common.base.Logger;
import common.view.WidgetsFactory;

public abstract class AbstractSelector<T> {
	
	private static final Logger log = new Logger("AbstractSelector");
	
	private final Combo combo;
	private Vector<T> vecData;
	private final String name;

	/**
	 * Constructs a new abstract selector.
	 * 
	 * @param name   the selector name
	 * @param parent the parent composite
	 * @param readOnly true if user cannot edit the selected item
	 * @param listener the listener to notify when the selection is modified (may be null)
	 */
	public AbstractSelector(String name, Composite parent, boolean readOnly, ModifyListener listener) {
		this.name = name;
		this.combo = WidgetsFactory.getInstance().createCombo(parent, -1, readOnly, listener);
		log.info("Creating AbstractSelector " + this.name);
	}
	
	/**
	 * Get the data to display in this combo.
	 * @return the data to display
	 */
	protected abstract Vector<T> getData();
	
	/**
	 * Get the combo display value for the specified object.
	 * @param obj  the object to display (may be null)
	 * @return  the text value to display in combo (may be null)
	 */
	public abstract String getDisplayValue(T obj);
	
	/**
	 * Create a new value from the specified text.
	 * @param text  the text
	 * @return  the created object
	 */
	protected abstract T newValueFromText(String text);
	
	/**
	 * Load or reload the combo.
	 */
	public void load() {
		combo.removeAll();
		vecData = getData();
		if (vecData != null) {
			for (T obj : vecData) {
				String displayValue = getDisplayValue(obj);
				if (displayValue != null && !displayValue.isEmpty()) {
					combo.add(displayValue);
				}
			}
		}
	}
	
	/**
	 * Sets the value to display in selector.
	 * @param value  the value to display (may be null)
	 */
	public void setValue(T value) {
		String displayValue = getDisplayValue(value);
		combo.setText(displayValue == null ? "" : displayValue);
	}
	
	public T getValue() {
		String displayValue = combo.getText();
		for (T obj : vecData) {
			if (displayValue.equals(getDisplayValue(obj))) {
				return obj;
			}
		}
		return newValueFromText(displayValue);
	}
	
	/**
	 * Clears the combo's text.
	 */
	public void clearDisplay() {
		combo.setText("");
	}
	
	public boolean hasUnsavedData(T forValue) {
		String currentVal = getDisplayValue(forValue);
		String displayVal = combo.getText();
		//log.info("Comparing <" + displayVal + "> to <" + currentVal + "> in " + name);
		return !displayVal.equals(currentVal);
		//return !getDisplayValue(forValue).equals(combo.getText());
	}
	
	public Combo getCombo() {
		return combo;
	}

}
