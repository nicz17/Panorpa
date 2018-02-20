package view.base;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;

public abstract class SimpleTextSelector extends AbstractSelector<String> {

	public SimpleTextSelector(String name, Composite parent, boolean readOnly, ModifyListener listener) {
		super(name, parent, readOnly, listener);
	}

	@Override
	public String getDisplayValue(String obj) {
		return obj;
	}

	@Override
	protected String newValueFromText(String text) {
		return text;
	}

}
