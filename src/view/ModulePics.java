package view;

import java.io.File;

import model.HerbierPic;
import model.Location;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

import view.base.AbstractModule;
import view.base.PhotoBox;
import view.base.ViewTools;

import common.exceptions.ValidationException;
import common.view.IncrementalSearchBox;
import common.view.MessageBox;

import controller.Controller;
import controller.DatabaseTools;
import controller.FileManager;

public class ModulePics extends AbstractModule<HerbierPic> {
	private EditorPics editor;
	private PhotoBox photoBox;
	private LocationSelector defaultLocationSelector;
	private File dirPics;
	
	protected final DatabaseTools.eOrdering eOrder[] = {DatabaseTools.eOrdering.BY_IDX, 
			DatabaseTools.eOrdering.BY_FILENAME, DatabaseTools.eOrdering.BY_FILENAME, 
			DatabaseTools.eOrdering.BY_FILENAME, DatabaseTools.eOrdering.BY_LOCATION,
			DatabaseTools.eOrdering.BY_RATING};

	public ModulePics() {
		super(3);
		
		dirPics = new File(FileManager.getInstance().getCurrentBaseDir() + "photos/");
		if (!dirPics.exists()) {
			dirPics = new File(FileManager.pathRaw);
		}
		
		loadWidgets();
		loadData();
	}
	
	@Override
	protected void createObject() {
		Runnable runExport = new Runnable() {
			public void run() {
				try {
					Controller.getInstance().scanForNewPics();
					showObjects();
				} catch (ValidationException e) {
					MessageBox.error(e.getMessage());
				}
			}
		};
		BusyIndicator.showWhile(getDisplay(), runExport);
	}
	
	@Override
	public void showObjects() {
		showObject(null);
		vecObjects = Controller.getInstance().getHerbierPics(eOrder[selCol], searchBox.getSearchText());
		reloadTable();
		lblStatus.setText(String.format("%d images", vecObjects.size()));
	}
	
	@Override
	public void pictureUpdated(int idx) {
		setSelectedObject(idx);
		showObjects();
	}
	
	@Override
	public void locationUpdated(int idx) {
		defaultLocationSelector.load();
		defaultLocationSelector.setValue(Controller.getInstance().getDefaultLocation());
	}

	@Override
	protected void onTableSelection(HerbierPic obj) {
		showObject(obj);
	}
	
	private void showObject(HerbierPic obj) {
		photoBox.showObject(obj);
		editor.showObject(obj);
	}

	@Override
	protected void loadWidgets() {
		initTable(new String[] {"Idx", "Nom", "Nom français", "Famille", "Lieu", "Qualité"}, 
				  new double[] {0.06, 0.35, 0.35, 0.25, 0.15, 0.04} );
		
	    editor = new EditorPics(cRight);
	    photoBox = new PhotoBox(cRight);
	    
	    // default location
		Composite cDefLoc = widgetsFactory.createComposite(cRight, 2, false, 0);
	    widgetsFactory.createLabel(cDefLoc, "Lieu par défaut :");
	    defaultLocationSelector = new LocationSelector("DefaultLocationSelector", cDefLoc, true, new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				Location location = defaultLocationSelector.getValue();
				if (location.getIdx() > 0) {
					Controller.getInstance().setDefaultLocation(location);
				}
			}
		});
	    defaultLocationSelector.load();
	    
	    widgetsFactory.createPushButton(cButtons, null, "folder", "Importer une photo", false, new SelectionAdapter() {
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	    		showOpenFileDialog();
	    	}
	    });
	    
	    searchBox = new IncrementalSearchBox(cButtons) {
	    	public void onSearch() {
	    		showObjects();
	    	}
	    };
		
		Controller.getInstance().addDataListener(this);
		//Controller.getInstance().addOptionListener(this);

		orderByColumn(1);
		setNewButtonTooltip("Importer les nouvelles images");
	}

	@Override
	protected void loadData() {
		showObjects();
		if (!vecObjects.isEmpty()) {
			tblData.select(0);
			showObject(vecObjects.firstElement());
		}
		
		defaultLocationSelector.setValue(Controller.getInstance().getDefaultLocation());
	}
	
	private void showOpenFileDialog() {
		try {
			FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
			dlg.setText("Choisir la photo à importer");
			dlg.setFilterPath(dirPics.getCanonicalPath());
			dlg.setFilterExtensions(new String[] {"*.jpg"});
			dlg.setFilterNames(new String[] { "Photos jpeg (*.jpg)" });
			String result = dlg.open();
			if (result != null) {
				File file = new File(result);
				Controller.getInstance().importNewPic(file);
				// remember last successful dir
				dirPics = file.getParentFile();
				showObjects();
			}
		} catch (Exception e) {
			ViewTools.displayException(e);
		}
	}

}
