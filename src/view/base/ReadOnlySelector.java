package view.base;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;

public abstract class ReadOnlySelector<T> extends AbstractSelector<T> {

	public ReadOnlySelector(String name, Composite parent, ModifyListener listener) {
		super(name, parent, true, listener);
	}
	
	protected T newValueFromText(String text) {
		// useless for read-only selector
		return null;
	}

}
