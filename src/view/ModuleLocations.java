package view;

import java.io.File;

import model.Location;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TableItem;

import view.base.AbstractModule;
import view.base.ViewTools;

import common.view.IncrementalSearchBox;

import controller.Controller;
import controller.DatabaseTools;
import controller.FileManager;
import controller.GeoTrack;
import controller.GeoTracker;

public class ModuleLocations extends AbstractModule<Location> {
	private EditorLocation editor;
	//private Button btnCreateFromTrack;
	private File dirGeoTrack;
	
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
		
		//btnCreateFromTrack = 
		widgetsFactory.createPushButton(cRight, "Créer par GeoTrack", "location", 
				"Créer un lieu à partir de données GeoTracker", false, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createFromGeoTrack();
			}
		});
		
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
		dirGeoTrack = new File(FileManager.getInstance().getCurrentBaseDir() + "geotracker/");
	}
	
	private void createFromGeoTrack() {
		try {
			FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
			dlg.setText("Choisir les données GeoTracker");
			if (dirGeoTrack != null) {
				dlg.setFilterPath(dirGeoTrack.getCanonicalPath());
			}
			dlg.setFilterExtensions(new String[] {"*.gpx"});
			dlg.setFilterNames(new String[] { "GeoTracker (*.gpx)" });
			String result = dlg.open();
			if (result != null) {
				File file = new File(result);
				// read GeoTracker data and create a new location
				GeoTrack track = GeoTracker.getInstance().readGeoData(file);
				Location newObj = Location.newLocation(track);
				vecObjects.add(newObj);
				
				// show object in table
				TableItem item = new TableItem(tblData, SWT.NONE);
				item.setText(newObj.getDataRow());
				tblData.setSelection(item);
				
				// show object in editor
				showObject(newObj);
			}
		} catch (Exception e) {
			ViewTools.displayException(e);
		}
	}

}
