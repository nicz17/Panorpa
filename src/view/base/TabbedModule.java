package view.base;


import view.Panorpa;

import common.view.BaseModule;

import controller.listeners.DataListener;

/**
 * A SWT {@link BaseModule} that acts as a {@link DataListener}.
 * 
 * @author nicz
 *
 */
public abstract class TabbedModule extends BaseModule implements DataListener {
	

	public TabbedModule() {
		super(Panorpa.getInstance().getFolder());
	}
	
	@Override
	public void pictureUpdated(int idx) {
	}
	
	@Override
	public void taxonUpdated(int idx) {
	}
	
	@Override
	public void locationUpdated(int idx) {
	}
	
	@Override
	public void expeditionUpdated(int idx) {
	}

}
