package view;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;
import java.util.regex.Matcher;

import model.HasPhoto;
import model.HerbierPic;
import model.Location;
import model.OriginalPic;
import model.Taxon;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;

import view.base.AbstractModule;
import view.base.PhotoBox;
import view.base.ViewTools;

import common.base.Logger;
import common.exceptions.AppException;
import common.view.MessageBox;
import common.view.ProgressBox;
import common.view.ProgressTimeBox;
import common.view.SearchBox;

import controller.Controller;
import controller.DatabaseTools;
import controller.FileManager;
import controller.GeoTrack;
import controller.GeoTracker;
import controller.LocationCache;
import controller.PicNameGenerator;
import controller.PicNameGeneratorLast;
import controller.TaxonCache;

/**
 * A module allowing to preselect and rename original JPEG image files using taxon names.
 * Also renames raw files.
 * 
 * @author nicz
 *
 */
public class ModulePreselection extends AbstractModule<OriginalPic> {
	
	private static final Logger log = new Logger("ModulePreselection", true);
	private static final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	private static final int iCenterWidth = 600;
	
	private File dirOrig;
	private File dirPhotos;
	private OriginalPic selectedPic;
	private Taxon selectedTaxon;
	private PicNameGenerator nameGenerator;
	private Vector<String> vecSelectedNames;
	private File lastSelectedFile;
	
	private Text txtBaseName;
	private Text txtName;
	private Button btnRename;
	private Button btnCompare;
	private Button btnGimp;
	private Button btnGeoTracking;
	private Label lblDir;
	private ToolBar toolbarTaxonTree;
	
	private TaxonTree taxonTree;
	private List listExistingPics;
	private SearchBox searchBoxPic;
	private SearchBox searchBoxTaxon;
	private PhotoBox  photoBox;
	private ProgressBox progressBox;
	
	protected final DatabaseTools.eOrdering eOrder[] = {
			DatabaseTools.eOrdering.BY_IDX, 
			DatabaseTools.eOrdering.BY_FILENAME, 
			DatabaseTools.eOrdering.BY_DATE};

	public ModulePreselection() {
		super(3);
		nameGenerator = null;
		vecSelectedNames = new Vector<>();
		lastSelectedFile = null;
		
		loadWidgets();
		setDirectory(new File(FileManager.getInstance().getCurrentBaseDir() + "orig/"));
		loadData();		
	}

	@Override
	protected void loadWidgets() {
		selectedPic = null;
		selectedTaxon = null;
		
		GridData data = (GridData) tblData.getLayoutData();
		data.widthHint = 400;
		initTable(new String[] {"No", "Nom de fichier", "Date de prise de vue"}, 
				  new double[] {0.10, 0.20, 0.25} );
		
		//btnDir = 
		widgetsFactory.createPushButton(cButtons, null, "folder", "Choisir un répertoire orig/", false, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showDirectoryDialog();
			}
		});
		searchBoxPic = new SearchBox(cButtons) {
			@Override
			public void onSearch() {
				showObjects();
			}
		};
		searchBoxPic.setSearchText(".JPG");
		
		data = new GridData(SWT.LEFT, SWT.FILL, true, true);
    	data.verticalAlignment = SWT.TOP;
    	data.verticalSpan = 2;
    	cRight.setLayoutData(data);
    	
		taxonTree = new TaxonTree("PreselectionTaxonTree", cRight) {
			@Override
			public void onSelection(Taxon taxon) {
				onTaxonSelection(taxon);
			}
		};
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 600;
		data.widthHint  = iCenterWidth;
		taxonTree.setLayoutData(data);
		
		Composite cBelowTree = new Composite(cRight, 0);
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		gl.verticalSpacing = 0;
		cBelowTree.setLayout(gl);
	    cBelowTree.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		toolbarTaxonTree = widgetsFactory.createToolbar(cBelowTree, false);
		fillToolBar();
		
		searchBoxTaxon = new SearchBox(cBelowTree) {
			@Override
			public void onSearch() {
				taxonTree.expandAll(false);
				taxonTree.searchInTree(searchBoxTaxon.getSearchText());
			}
		};
		searchBoxTaxon.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Group gExistingPics = widgetsFactory.createGroup(cRight, "Photos existantes");
		
		listExistingPics = widgetsFactory.createList(gExistingPics, iCenterWidth, 400);		
		lblDir = widgetsFactory.createLabel(cRight);
		
		btnNew.dispose();
		btnReload.setToolTipText("Recharger les photos");
		
		photoBox = new PhotoBox(cThird) {
			@Override
			public void showObject(HasPhoto obj) {
				hasPhoto = obj;
				if (hasPhoto == null) {
					lblPhoto.setImage(ViewTools.getPhotoThumb(null));
				} else {
					lblPhoto.setImage(ViewTools.getOrigThumb(hasPhoto));
				}
			}
		};
		
		widgetsFactory.createLabel(cThird, "Base de nom :");
		txtBaseName = widgetsFactory.createText(cThird, -1, new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				//enableWidgets();
			}
		});
		
		widgetsFactory.createLabel(cThird, "Sélectionner photo sous :");
		txtName = widgetsFactory.createText(cThird, -1, new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				enableWidgets(true);
			}
		});
		
		Composite cButtonsRight = widgetsFactory.createComposite(cThird, 4, true, 6);
		
		btnRename = widgetsFactory.createPushButton(cButtonsRight, "Préselection", "ok", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				renameSelection();
			}
		});
		
		btnCompare = widgetsFactory.createPushButton(cButtonsRight, "Comparer", "zoom", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				compareSelection();
			}
		});
		
		btnGimp = widgetsFactory.createPushButton(cButtonsRight, "Ouvrir avec Gimp", "gimp", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openWithGimp();
			}
		});
		
		btnGeoTracking = widgetsFactory.createPushButton(cButtonsRight, "GeoTracking", "location24", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addGeoTracking();
			}
		});
		
		progressBox = new ProgressTimeBox(cThird, 100);
	}

	@Override
	protected void loadData() {
		showObjects();
		loadTaxonTree();
		enableWidgets(true);
	}
	
	private void loadTaxonTree() {
		// remember tree selection if needed
		Taxon selection = taxonTree.getSelection();
		
		// reload tree
		Vector<Taxon> vecTaxons = new Vector<>(TaxonCache.getInstance().getTopLevel());
		taxonTree.setData(vecTaxons);
		
		// restore selection if possible
		taxonTree.expandTree(selection);		
	}
	
	private void showExistingPics() {
		listExistingPics.removeAll();
		if (selectedTaxon != null) {
			for (HerbierPic hpic : selectedTaxon.getPics()) {
				listExistingPics.add(hpic.getFileName() + " (" + 
						dateFormat.format(hpic.getShotAt()) + ", " +
						Panorpa.getInstance().getAppName() + ")");
			}
			
			// add pics from current photos/ dir matching taxon name
			for (String name : vecSelectedNames) {
				listExistingPics.add(name + " (" + dirPhotos.getParentFile().getName() + ")");
			}
		}
	}
	
	private void renameSelection() {
		try {
			FileManager.getInstance().preselectFile(selectedPic.getOrigFile(), txtName.getText());
			lastSelectedFile = new File(dirPhotos.getAbsolutePath() + "/" + txtName.getText());
			btnGimp.setToolTipText("Ouvrir " + lastSelectedFile.getAbsolutePath() + " dans Gimp");
			txtName.setText("");
			enableWidgets(true);
			loadExistingPics();
			showExistingPics();
		} catch (AppException e) {
			ViewTools.displayException(e);
		}
	}
	
	private void showDirectoryDialog() {
		try {
			DirectoryDialog dlg = new DirectoryDialog(getShell(), SWT.OPEN);
			dlg.setText("Choisir un répertoire orig");
			File dirFile = (dirOrig == null ? new File(FileManager.pathRaw) : dirOrig);
			dlg.setFilterPath(dirFile.getCanonicalPath());
			String result = dlg.open();
			if (result != null) {
				if (!result.endsWith("/")) {
					result += "/";
				}
				if (!result.endsWith("orig/")) {
					result += "orig/";
				}
				setDirectory(new File(result));
				loadData();
			}
		} catch (Exception e) {
			ViewTools.displayException(e);
		}
	}
	
	private void setDirectory(File dir) {
		if (dir != null && dir.exists()) {
			dirOrig = dir;
			lblDir.setText(dirOrig.getAbsolutePath());
			dirPhotos = new File(dirOrig.getAbsolutePath().replace("orig", "photos"));
		} else {
			MessageBox.error("Le répertoire n'existe pas :\n" + dir.getAbsolutePath());
			lblDir.setText("Choisir un répertoire orig/");
		}
	}
	
	private void enableWidgets(boolean bEnabled) {
		btnRename.setEnabled(bEnabled && selectedPic != null && !txtName.getText().isEmpty());
		btnCompare.setEnabled(bEnabled && selectedTaxon != null && selectedPic != null);
		btnGimp.setEnabled(bEnabled && lastSelectedFile != null && lastSelectedFile.exists());
		btnGeoTracking.setEnabled(bEnabled);
	}

	@Override
	protected void onTableSelection(OriginalPic pic) {
		log.info("onTableSelection: selIdx " + selIdx + ", " + pic);
		selectedPic = pic;

		if (pic != null) {
			generateName();

			// create thumbnail if needed
			Controller.getInstance().createThumbnailIfMissing(pic.getOrigFile(), pic.getThumbFile());

			photoBox.showObject(pic);
		}

		enableWidgets(true);
	}
	
	private void onTaxonSelection(Taxon taxon) {
		selectedTaxon = taxon;
		
		if (taxon != null) {
			createNameGenerator();
			txtBaseName.setText(nameGenerator.getBaseName());
			generateName();
		} else {
			txtName.setText("");
			txtBaseName.setText("");
			nameGenerator = null;
		}
		
		showExistingPics();
		enableWidgets(true);
	}
	
	private void createNameGenerator() {
		if (selectedTaxon != null) {
			//nameGenerator = new PicNameGenerator(selectedTaxon);
			nameGenerator = new PicNameGeneratorLast(selectedTaxon);
			loadExistingPics();
		}
	}
	
	private void loadExistingPics() {
		if (dirPhotos != null && nameGenerator != null) {
			try {
				Vector<File> vecSelectedFiles = FileManager.getInstance().getSelectedFiles(dirPhotos, nameGenerator.getBaseName());
				vecSelectedNames.clear();
				for (File file : vecSelectedFiles) {
					vecSelectedNames.add(file.getName());
				}
				nameGenerator.addExistingNames(vecSelectedNames);
			} catch (AppException e) {
				ViewTools.displayException(e);
			}
		}
	}
	
	private void generateName() {
		if (selectedPic != null && nameGenerator != null) {
			String picNumber = null;
			Matcher match = OriginalPic.patOrigFileName.matcher(selectedPic.getName());
			if (match.matches()) {
				picNumber = match.group(1);
				//log.info("Selected file number is " + picNumber);
			}
			try {
				String sName = nameGenerator.generateName(picNumber);
				if (sName != null) {
					txtName.setText(sName);
				} else {
					txtName.setText("");
				}
			} catch (AppException e) {
				ViewTools.displayException(e);
			}
		} else {
			txtName.setText("");
		}
	}

	@Override
	protected void showObjects() {
		showObject(null);
		vecObjects = new Vector<>();
		if (dirOrig != null) {
			try {
				vecObjects = FileManager.getInstance().getOrigFiles(dirOrig, searchBoxPic.getSearchText());
			} catch (AppException e) {
				ViewTools.displayException(e);
			}
		}
		reloadTable();
		lblStatus.setText(String.format("%d images", vecObjects.size()));
	}
	
	private void showObject(OriginalPic obj) {
		photoBox.showObject(obj);
	}
	
	/**
	 * Adds buttons to the toolbar.
	 */
	private void fillToolBar() {
		// refresh button
		widgetsFactory.createToolItem(toolbarTaxonTree, "refresh", 
				"Recharger les taxa", new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//Controller.getInstance().reloadCache();
				loadTaxonTree();
			}
		});

		// unexpand all button
		widgetsFactory.createToolItem(toolbarTaxonTree, "undo", 
				"Tout fermer", new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				taxonTree.expandAll(false);
			}
		});

		// add taxon button
		widgetsFactory.createToolItem(toolbarTaxonTree, "add", 
				"Créer sous-taxon", new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				createChildTaxon();
			}
		});
	}
	
	private void createChildTaxon() {
		MessageBox.error("Pas encore implémenté!");
	}
	
	private void compareSelection() {
		if (selectedPic != null && selectedTaxon != null) {
			DialogComparePicture dlg = new DialogComparePicture(getShell(), selectedPic, selectedTaxon);
			dlg.open();
		} else {
			MessageBox.error("Choisir une photo et un taxon.");
		}
	}
	
	private void openWithGimp() {
		if (lastSelectedFile != null && lastSelectedFile.exists()) {
			try {
				Runtime.getRuntime().exec(new String[] {"gimp", lastSelectedFile.getAbsolutePath()});
			} catch (IOException e) {
				ViewTools.displayException(e);
			}
		}
	}
	
	private void addGeoTracking() {
		// Open a .gpx file (XML GeoTracker data)
		try {
			FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
			dlg.setText("Choisir les données GeoTracker");
			if (dirOrig != null) {
				dlg.setFilterPath(dirOrig.getCanonicalPath().replaceFirst("orig", "geotracker"));
			}
			dlg.setFilterExtensions(new String[] {"*.gpx"});
			dlg.setFilterNames(new String[] { "GeoTracker (*.gpx)" });
			String result = dlg.open();
			if (result != null) {
				final File file = new File(result);
				// read GeoTracker data and apply to pics
				final GeoTrack track = GeoTracker.getInstance().readGeoData(file);
				
				// TODO handle DST offset (optionally, but better to set it on camera)
				//int iOffset = -3600*1000;
				//track.setOffset(iOffset);
				
				int nMatches = GeoTracker.getInstance().addGeoDataToPics(vecObjects, track, true);
				Location locClosest = LocationCache.getInstance().getClosestLocation(track.getMeanPosition());
				String msg = track.getDescription();
				msg += "\n\nPhotos sur le parcours: " + nMatches + "/" + vecObjects.size();
				if (locClosest != null) {
					msg += "\n\nLieu le plus proche : " + locClosest.getName();
				}
				msg += "\n\nAppliquer les données GPS ?\n";
				boolean bApply = MessageBox.askYesNo(msg, "Données GeoTracker", "location");
				
				if (bApply) {
					enableWidgets(false);
					progressBox.taskStarted(nMatches);
					progressBox.info("Ajout de données GPS à " + nMatches + " photos");
					Runnable runGeoTagging = new Runnable() {
						public void run() {
							GeoTracker.getInstance().addGeoDataToPics(vecObjects, track, false, progressBox);
						}
					};
					BusyIndicator.showWhile(getDisplay(), runGeoTagging);
					progressBox.taskFinished();
					FileManager.getInstance().storeGeoTrack(file);
					enableWidgets(true);
				}
			}
		} catch (Exception e) {
			ViewTools.displayException(e);
		}
	}

}
