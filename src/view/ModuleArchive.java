package view;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Vector;

import model.HerbierPic;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;

import view.base.LegendPhotoBox;
import view.base.ViewTools;

import common.exceptions.AppException;
import common.view.SashModule;
import common.view.SearchBox;

import controller.Controller;
import controller.FileManager;
import controller.RawFileMatcher;

/**
 * A module allowing to rename RAW image files using file names of recent HerbierPics.
 * 
 * @author nicz
 *
 */
public class ModuleArchive extends SashModule {
	
	private String dirRaw;
	private String sRawFileName;
	private String sPicFileName;
	private Vector<HerbierPic> vecPics;
	
	private List listRaw;
	private List listPics;
	private Button btnDirRaw;
	private Button btnRename;
	
	private SearchBox searchBox;
	private LegendPhotoBox  photoBox;
	
	private RawFileMatcher rawFileMatcher;

	public ModuleArchive() {
		super(Panorpa.getInstance().getFolder(), 800);
	}

	@Override
	protected void loadWidgets() {
		dirRaw = FileManager.getInstance().getCurrentRawDir();
		sRawFileName = null;
		sPicFileName = null;
		vecPics = new Vector<HerbierPic>();
		
		Group gRaw = widgetsFactory.createGroup(cLeft, "Renommer raw");
		btnDirRaw = widgetsFactory.createPushButton(gRaw, "Ouvrir", "folder", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showDirectoryDialog();
			}
		});
		searchBox = new SearchBox(gRaw) {
			@Override
			public void onSearch() {
				try {
					reloadRawFiles();
				} catch (AppException e) {
					ViewTools.displayException(e);
				}
			}
		};
		searchBox.setSearchText(".*\\.NEF");
		
		Composite cFiles = widgetsFactory.createComposite(gRaw, 2, true, 0);
		listPics = widgetsFactory.createList(cFiles, 370, 700);
		listRaw  = widgetsFactory.createList(cFiles, 370, 700);
		
		listPics.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (listPics.getSelectionCount() > 0) {
					onPicSelection();
				}
				enableWidgets();
			}
		});
		listRaw.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (listRaw.getSelectionCount() > 0) {
					onRawSelection();
				}
				enableWidgets();
			}
		});
		
		Composite cButtons = widgetsFactory.createComposite(gRaw, 3, true, 5);
		btnRename = widgetsFactory.createPushButton(cButtons, "Renommer", "edit", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				renameSelection();
			}
		});
		widgetsFactory.createPushButton(cButtons, "Recharger", "refresh", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadData();
			}
		});
		widgetsFactory.createPushButton(cButtons, "Déplacer raw", "go-next", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					FileManager.getInstance().moveRawFiles(dirRaw);
				} catch (AppException exc) {
					ViewTools.displayException(exc);
				}
			}
		});
		
		photoBox = new LegendPhotoBox(cRight);
		
		rawFileMatcher = new RawFileMatcher();
		
		enableWidgets();
	}

	@Override
	protected void loadData() {
		try {
			vecPics = Controller.getInstance().getLatestHerbierPics(150);
			if (vecPics != null) {
				Collections.sort(vecPics, new Comparator<HerbierPic>() {
					@Override
					public int compare(HerbierPic pic1, HerbierPic pic2) {
						Date date1 = pic1.getShotAt();
						Date date2 = pic2.getShotAt();
						if (date1.equals(date2)) {
							return pic1.getFileName().compareTo(pic2.getFileName());
						}
						return date2.compareTo(date1);
					}
				});
				
				listPics.removeAll();
				photoBox.showObject(null);
				if (vecPics.isEmpty()) {
					listRaw.add("Aucune photo.");
				} else {
					for (HerbierPic pic : vecPics) {
						listPics.add(pic.getFileName());
					}
				}
			}
			
			reloadRawFiles();
		} catch (AppException e) {
			ViewTools.displayException(e);
		}
		
		enableWidgets();
	}
	
	private void onPicSelection() {
		sPicFileName = listPics.getSelection()[0];
		HerbierPic hpic = vecPics.get(listPics.getSelectionIndex());
		photoBox.showObject(hpic);
		
		if (rawFileMatcher != null) {
			File picFile = new File(Controller.picturesPath + hpic.getFileName());
			File rawFile = rawFileMatcher.getMatchingRaw(picFile);
			if (rawFile != null) {
				listRaw.setSelection(new String[]{rawFile.getName()});
				onRawSelection();
				enableWidgets();
			}
		}
	}
	
	private void onRawSelection() {
		sRawFileName = listRaw.getSelection()[0];
	}
	
	private void renameSelection() {
		try {
			FileManager.getInstance().renameRawFile(sPicFileName, new File(dirRaw + sRawFileName));
			reloadRawFiles();
		} catch (AppException e) {
			ViewTools.displayException(e);
		}
	}
	
	private void showDirectoryDialog() {
		try {
			DirectoryDialog dlg = new DirectoryDialog(getShell(), SWT.OPEN);
			dlg.setText("Choisir un répertoire raw");
			File dirFile = new File(FileManager.pathRaw);
			dlg.setFilterPath(dirFile.getCanonicalPath());
			String result = dlg.open();
			if (result != null) {
				dirRaw = result;
				if (!dirRaw.endsWith("/")) {
					dirRaw += "/";
				}
				if (!dirRaw.endsWith("raw/")) {
					dirRaw += "raw/";
				}
				
				reloadRawFiles();
			}
		} catch (Exception e) {
			ViewTools.displayException(e);
		}
	}
	
	private void reloadRawFiles() throws AppException {
		sRawFileName = null;
		btnDirRaw.setText(dirRaw);
		Vector<File> vecFiles = FileManager.getInstance().getRawFiles(dirRaw, searchBox.getSearchText());
		if (vecFiles != null) {
			listRaw.removeAll();
			if (vecFiles.isEmpty()) {
				listRaw.add("Aucun fichier raw.");
			} else {
				for (File file : vecFiles) {
					listRaw.add(file.getName());
				}
			}
			if (rawFileMatcher != null) {
				rawFileMatcher.setRawFiles(vecFiles);
			}
		}
		enableWidgets();
	}
	
	private void enableWidgets() {
		btnRename.setEnabled(sRawFileName != null && sPicFileName != null);
	}

}
