package view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;

import model.Location;
import view.base.AbstractModule;

import common.view.IncrementalSearchBox;

import controller.Controller;
import controller.DatabaseTools;

public class ModuleLocations extends AbstractModule<Location> {
	private EditorLocation editor;
	
	protected final DatabaseTools.eOrdering eOrder[] = {DatabaseTools.eOrdering.BY_NAME, 
			DatabaseTools.eOrdering.BY_TOWN, DatabaseTools.eOrdering.BY_REGION, 
			DatabaseTools.eOrdering.BY_KIND, DatabaseTools.eOrdering.BY_ALTITUDE};

	public ModuleLocations() {
		super(2);
		
		loadWidgets();
		loadData();
	}
	
	@Override
	protected void createObject() {
		Location newObj = Location.newLocation("");
		vecObjects.add(newObj);
		
		// show object in table
		TableItem item = new TableItem(tblData, SWT.NONE);
		item.setText(newObj.getDataRow());
		tblData.setSelection(item);
		
		// show object in editor
		showObject(newObj);
	}
	
	@Override
	public void showObjects() {
		showObject(null);
		vecObjects = Controller.getInstance().getLocations(eOrder[selCol], searchBox.getSearchText());
		reloadTable();
		lblStatus.setText(String.format("%d lieux", vecObjects.size()));
	}
	
	@Override
	public void locationUpdated(int idx) {
		setSelectedObject(idx);
		showObjects();
	}

	@Override
	protected void onTableSelection(Location obj) {
		showObject(obj);
	}
	
	private void showObject(Location obj) {
		editor.showObject(obj);
	}
//
//	private void enableButtons(boolean enabled) {
//	}

	@Override
	protected void loadWidgets() {
		initTable(new String[] {"Nom", "Localité", "Région", "Type", "Altitude"}, 
				  new double[] {0.30, 0.20, 0.20, 0.20, 0.10} );
		
	    editor = new EditorLocation(cRight);
	    
	    searchBox = new IncrementalSearchBox(cButtons) {
	    	public void onSearch() {
	    		showObjects();
	    	}
	    };
		
		Controller.getInstance().addDataListener(this);

		orderByColumn(0);
	}

	@Override
	protected void loadData() {
		showObjects();
		if (!vecObjects.isEmpty()) {
			tblData.select(0);
			showObject(vecObjects.firstElement());
		}
	}

}
