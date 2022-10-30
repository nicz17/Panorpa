package view;

import java.io.File;
import java.util.Collections;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TableItem;

import model.Expedition;
import model.HerbierPic;
import model.Location;
import view.base.AbstractModule;
import view.base.MultiPhotoBox;
import view.base.ViewTools;

import common.view.IncrementalSearchBox;

import controller.Controller;
import controller.DatabaseTools;
import controller.ExpeditionManager;
import controller.FileManager;
import controller.GeoTrack;
import controller.GeoTracker;
import controller.LocationCache;

/**
 * Module displaying excursion objects.
 * 
 * <p>It has a table of excursions, an editor and a photo box.
 *
 * <p><b>Modifications:</b>
 * <ul>
 * <li>11.12.2018: nicz - Creation</li>
 * </ul>
 */
public class ModuleExpeditions extends AbstractModule<Expedition> {
	private EditorExpedition editor;
	private MultiPhotoBox multiPhotoBox;
	private File dirGeoTrack;
	
	protected final DatabaseTools.eOrdering eOrder[] = {
			DatabaseTools.eOrdering.BY_DATE, DatabaseTools.eOrdering.BY_NAME, 
			DatabaseTools.eOrdering.BY_LOCATION, DatabaseTools.eOrdering.BY_DATE};

	public ModuleExpeditions() {
		super(2);
		
		loadWidgets();
		loadData();
	}
	
	@Override
	protected void createObject() {
		Location loc = LocationCache.getInstance().getLocation(Controller.getInstance().getDefaultLocation().getIdx());
		Vector<Expedition> newObects = ExpeditionManager.getInstance().buildExpeditions(loc);
		for (Expedition newObj : newObects) {
			vecObjects.add(newObj);

			// show object in table
			TableItem item = new TableItem(tblData, SWT.NONE);
			item.setText(newObj.getDataRow());
			tblData.setSelection(item);
		}
	}
	
	@Override
	public void showObjects() {
		showObject(null);
		vecObjects = Controller.getInstance().getExpeditions(eOrder[selCol], searchBox.getSearchText());
		reloadTable();
		lblStatus.setText(String.format("%d excursions", vecObjects.size()));
	}

	
	@Override
	public void expeditionUpdated(int idx) {
		setSelectedObject(idx);
		showObjects();
	}

	@Override
	protected void onTableSelection(Expedition obj) {
		showObject(obj);
	}
	
	private void showObject(Expedition obj) {
		editor.showObject(obj);
		
		if (obj != null) {
			ExpeditionManager.getInstance().setExpeditionPics(obj);
			Vector<HerbierPic> vecPics = new Vector<HerbierPic>(obj.getPics());
			Collections.sort(vecPics);
			multiPhotoBox.setPics(vecPics, obj.getTitle());
		} else {
			multiPhotoBox.setPics(null, null);
		}
	}

	@Override
	protected void loadWidgets() {
		initTable(new String[] {"Début", "Titre", "Lieu", "Fin"}, 
				  new double[] {0.20, 0.30, 0.20, 0.20} );
		
	    editor = new EditorExpedition(cRight);
		multiPhotoBox = new MultiPhotoBox(cRight);
		
		widgetsFactory.createPushButton(cButtons, null, "location24", 
				"Créer une excursion à partir de données GeoTracker", false, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createFromGeoTrack();
			}
		});
	    
	    searchBox = new IncrementalSearchBox(cButtons) {
	    	public void onSearch() {
	    		showObjects();
	    	}
	    };
	    
	    btnNew.setToolTipText("Ajouter les excursions du lieu par défaut");
		
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
				Expedition newObj = Expedition.newExpedition(track);
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
