package view;

import java.util.Vector;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;

import controller.Controller;
import controller.DatabaseTools.eOrdering;

import model.Location;
import view.base.AbstractSelector;

public class LocationSelector extends AbstractSelector<Location> {

	public LocationSelector(String name, Composite parent, boolean readOnly,
			ModifyListener listener) {
		super(name, parent, readOnly, listener);
	}

	@Override
	protected Vector<Location> getData() {
		Vector<Location> data = Controller.getInstance().getLocations(eOrdering.BY_NAME, null);
		return data;
	}

	@Override
	public String getDisplayValue(Location obj) {
		String result = "";
		if (obj != null) {
			result = obj.getName() + " (" + obj.getAltitude() + "m)";
		}
		return result;
	}

	@Override
	protected Location newValueFromText(String text) {
		return Location.newLocation(text);
	}

}
