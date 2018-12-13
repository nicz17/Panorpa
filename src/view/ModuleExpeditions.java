package view;

import java.util.Collections;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;

import model.Expedition;
import model.HerbierPic;
import model.Location;
import view.base.AbstractModule;
import view.base.MultiPhotoBox;

import common.view.IncrementalSearchBox;

import controller.Controller;
import controller.DatabaseTools;
import controller.ExpeditionManager;
import controller.LocationCache;

/**
 * Module displaying expedition objects.
 * 
 * <p>It has a table of expeditions, an editor and a photo box.
 *
 * <p><b>Modifications:</b>
 * <ul>
 * <li>11.12.2018: nicz - Creation</li>
 * </ul>
 */
public class ModuleExpeditions extends AbstractModule<Expedition> {
	private EditorExpedition editor;
	private MultiPhotoBox multiPhotoBox;
	
	protected final DatabaseTools.eOrdering eOrder[] = {DatabaseTools.eOrdering.BY_NAME, 
			DatabaseTools.eOrdering.BY_LOCATION, DatabaseTools.eOrdering.BY_DATE, 
			DatabaseTools.eOrdering.BY_DATE};

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
		lblStatus.setText(String.format("%d expéditions", vecObjects.size()));
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
		initTable(new String[] {"Titre", "Lieu", "Début", "Fin"}, 
				  new double[] {0.30, 0.20, 0.20, 0.20} );
		
	    editor = new EditorExpedition(cRight);
		multiPhotoBox = new MultiPhotoBox(cRight);
	    
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
