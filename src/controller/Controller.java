package controller;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import model.AppParam;
import model.AppParamName;
import model.Expedition;
import model.HerbierPic;
import model.Location;
import model.StatItem;
import model.Taxon;
import model.TaxonRank;

import common.base.Logger;
import common.exceptions.AppException;
import common.exceptions.ValidationException;
import common.listeners.ProgressListener;
import common.text.FileSizeNumberFormat;
import common.view.MessageBox;

import controller.DatabaseTools.UpdateType;
import controller.DatabaseTools.eOrdering;
import controller.checks.DataCheckManager;
import controller.checks.LocationChecker;
import controller.export.ExportManager;
import controller.listeners.DataListener;
import controller.upload.UploadManager;
import controller.validation.ExpeditionValidator;
import controller.validation.LocationValidator;
import controller.validation.PicValidator;
import controller.validation.TaxonValidator;

/**
 * Main Controller Singleton class. 
 * Acts as a link between database and GUI.
 *
 * <p><b>Modifications:</b>
 * <ul>
 * <li>30.09.2015: nicz - Creation</li>
 * </ul>
 */
public class Controller {
	
	public static final String appPath      = "/home/nicz/Documents/HerbierApp/";
	public static final String picturesPath = "/home/nicz/Pictures/eHerbier/";
	public static final String thumbsPath   = appPath + "thumbs/";
	public static final String mediumPath   = appPath + "medium/";
	public static final String exportPath   = appPath + "export/";
	public static final String resourcePath = appPath + "resources/";
	public static final String htmlPath     = exportPath + "html/";


	private static final Logger log = new Logger("Controller", true);
	
	/** The singleton instance */
	private static Controller instance;
	
	/** A list of listeners that are notified of updates in database */
	private Vector<DataListener> vecDataListeners;
	
	private final TaxonValidator      taxonValidator;
	private final PicValidator        picValidator;
	private final LocationValidator   locationValidator;
	private final ExpeditionValidator expeditionValidator;
	
	private final Importer importer;
	
	/** The default Location when scanning for new pictures */
	private Location defaultLocation;

	public static Controller getInstance() {
		if (instance == null) {
			instance = new Controller();
		}
		return instance;
	}
	
	public Vector<HerbierPic> getHerbierPics(eOrdering order, String filter) {
		Vector<HerbierPic> vecHPics = DataAccess.getInstance().getHerbierPics(order, filter);
		
		for (HerbierPic hpic : vecHPics) {
			hpic.setTaxon(TaxonCache.getInstance().getTaxon(hpic.getIdxTaxon()));
		}
		
		return vecHPics;
	}
	
	public Vector<HerbierPic> getLatestHerbierPics(int limit) {
		String where = " ORDER BY picShotAt DESC LIMIT " + limit;
		Vector<HerbierPic> vecHPics = DataAccess.getInstance().getHerbierPics(where, null, null);
		
		for (HerbierPic hpic : vecHPics) {
			hpic.setTaxon(TaxonCache.getInstance().getTaxon(hpic.getIdxTaxon()));
		}
		
		return vecHPics;
	}
	
	public Vector<Taxon> getTaxons(eOrdering order, String filter) {
		Vector<Taxon> vecTaxons = DataAccess.getInstance().getTaxons(null, order, filter);
		setTaxonParentInfo(vecTaxons);
		return vecTaxons;
	}
	
	public Vector<Taxon> getTaxons(TaxonRank rank) {
		Vector<Taxon> vecTaxons = DataAccess.getInstance().getTaxons(rank);
		setTaxonParentInfo(vecTaxons);
		return vecTaxons;
	}
	
	public Vector<Taxon> getLatestSpecies(int nLatest) {
		String where = "where taxRank = '" + TaxonRank.SPECIES.name() + "' order by idxTaxon desc limit " + nLatest;
		Vector<Taxon> vecTaxons = DataAccess.getInstance().getTaxons(where, null, null);
		//setTaxonParentInfo(vecTaxons);
		return vecTaxons;
	}
	
	public Vector<Location> getLocations(eOrdering order, String filter) {
		Vector<Location> vecLocations = DataAccess.getInstance().getLocations(null, order, filter);
		return vecLocations;
	}
	
	public Vector<Expedition> getExpeditions(eOrdering order, String filter) {
		Vector<Expedition> vecExpeditions = DataAccess.getInstance().getExpeditions(null, order, filter);
		return vecExpeditions;
	}
	
	/**
	 * Fetches the list of Expeditions for the specified location, sorted by date.
	 * Loads the pictures of the expedition.
	 * @param loc the location (may be null)
	 * @return  alist of Expeditions, or null if Location is null.
	 */
	public Vector<Expedition> getExpeditions(Location loc) {
		if (loc != null) {
			String where = " WHERE expLocation = " + loc.getIdx();
			Vector<Expedition> vecResult = DataAccess.getInstance().getExpeditions(where, eOrdering.BY_DATE, null);
			
			for (Expedition exp : vecResult) {
				ExpeditionManager.getInstance().setExpeditionPics(exp);
			}
			
			return vecResult;
		}
		return null;
	}
	
	public Vector<Location> getLatestLocations(int nLatest) {
		Vector<Location> vecLocations = new Vector<>();
		
		vecLocations.addAll(LocationCache.getInstance().getAll());
		for (Location loc : vecLocations) {
			loc.computeDateFirstPic();
		}
		
		Collections.sort(vecLocations, new Comparator<Location>() {
			@Override
			public int compare(Location loc1, Location loc2) {
				return loc1.getDateFirstPic().after(loc2.getDateFirstPic()) ? -1 : 1;
			}
		});
		
		Vector<Location> result = new Vector<>();
		result.addAll(vecLocations.subList(0, nLatest));
		
		return result;
	}
	

	/**
	 * Gets some facts about the picture collection.
	 * @return a list of StatItems.
	 */
	public Vector<StatItem> getStats() {
		Vector<StatItem> stats = new Vector<>();
		
		stats.add(new StatItem("Nombre de photos", PictureCache.getInstance().size()));
		stats.add(new StatItem("Nombre de taxons", TaxonCache.getInstance().size()));
		stats.add(new StatItem("Nombre de lieux",  LocationCache.getInstance().size()));
		
		// Total file size
		long fileSize = importer.computeTotalFileSize();
		stats.add(new StatItem("Taille totale des photos", new FileSizeNumberFormat().format(fileSize)));
		
		return stats;
	}

	/**
	 * Reloads the taxon and pictures caches.
	 */
	public void reloadCache() {
		LocationCache.getInstance().loadAll();
		TaxonCache.getInstance().loadAll();
		PictureCache.getInstance().loadAll();
		
		(new LocationChecker()).check();
	}
	
	/**
	 * Sets the default location to use when scanning for new pictures.
	 * 
	 * @param defaultLocation the default location (may be null)
	 */
	public void setDefaultLocation(Location defaultLocation) {
		log.info("Setting default location to " + defaultLocation);
		this.defaultLocation = defaultLocation;
	}
	
	/**
	 * Gets the default location for new pictures.
	 * @return the default location (may be null)
	 */
	public Location getDefaultLocation() {
		return defaultLocation;
	}

	/**
	 * Searches for new picture in picturesPath.
	 * Compares the list of pictures found on disk to the list of pictures in database.
	 * Saves any new pictures to database.
	 * 
	 * @throws ValidationException  if saving new pictures fails.
	 * @deprecated pics are imported one by one now.
	 */
	public void scanForNewPics() throws ValidationException {
		log.info("Scanning for new pictures with default location " + defaultLocation);
		
		Vector<HerbierPic> vecDbPics = getHerbierPics(null, null);
		Vector<File> vecFiles  = importer.getPicturesList();
		log.info("Scanned " + vecDbPics.size() + " pics in database, " + 
				vecFiles.size() + " pic files on disk");
		
		int nNewPics = vecFiles.size() - vecDbPics.size();
		if (nNewPics == 0) {
			MessageBox.info("Pas de nouvelles photos.");
		} else {
			Set<String> setFilenames = new HashSet<>();
			Vector<File> vecNewFiles = new Vector<>();
			for (HerbierPic dbPic : vecDbPics) {
				setFilenames.add(dbPic.getFileName());
			}
			for (File file : vecFiles) {
				if (!setFilenames.contains(file.getName())) {
					vecNewFiles.add(file);
				}
			}
			for (File file : vecNewFiles) {
				HerbierPic pic = importer.importOnePicture(file);
				addImportedPic(pic, file);
				if (pic.getTaxon() == null) {
					nNewPics--;
				}
			}
			String msg = String.valueOf(nNewPics) + (nNewPics == 1 ? " nouvelle photo." : " nouvelles photos.");
			MessageBox.info(msg);
		}
	}
	
	public void importNewPic(File file) throws AppException {
		log.info("Importing new photo: " + file.getAbsolutePath());
		HerbierPic pic = importer.copyAndImport(file);
		addImportedPic(pic, file);
	}
	
	private void addImportedPic(HerbierPic pic, File file) throws ValidationException {
		log.info("... new picture: " + pic);
		importer.checkImageSize(file);
		TaxonFactory.getInstance().setTaxons(pic);
		
		if (defaultLocation != null) {
			pic.setLocation(defaultLocation);
		}
		
		if (pic.getTaxon() == null) {
			log.error("Failed to set taxons for " + pic);
		} else {
			savePicture(pic);
		}
	}
	
	/**
	 * Searches an available filename for the specified picture to be moved to
	 * the specified taxon. Returns the first available filename.
	 * 
	 * <p>Taxon should be a species or genus, and different from the picture's
	 * current taxon. If that is not the case, the current filename is returned.
	 * 
	 * @param hpic   the picture to move to a new taxon
	 * @param taxon  the new picture taxon
	 * @return  the first available filename in the new taxon
	 * @throws AppException  if no available filename can be found (sequence is full)
	 */
	public String getAvailableFilename(HerbierPic hpic, Taxon taxon) throws AppException {
		return importer.getAvailableFilename(hpic, taxon);
	}
	
	/**
	 * Renames the specified picture and changes its taxon.
	 * 
	 * @param hpic      the picture to update
	 * @param taxon     the new picture taxon
	 * @param filename  the new filename
	 * @throws AppException if target file already exists, or saving is invalid
	 */
	public void renamePic(HerbierPic hpic, Taxon taxon, String filename) throws AppException {
		importer.renamePic(hpic, taxon, filename);
	}
	
	/**
	 * Sets the parent taxon of the specified child taxon.
	 * 
	 * @param idxChild  the database index of the child taxon
	 * @param idxParent the database index of the parent taxon
	 * @throws AppException  if the taxa are not found in cache, or operation is invalid
	 */
	public void setTaxonParent(int idxChild, int idxParent) throws AppException {
		if (idxChild == idxParent) {
			return;
		}
		
		// fetch taxa
		Taxon child = TaxonCache.getInstance().getTaxon(idxChild);
		if (child == null) {
			throw new AppException("Le taxon enfant n'est pas en cache: " + idxChild);
		}
		
		Taxon parent = TaxonCache.getInstance().getTaxon(idxParent);
		if (parent == null) {
			throw new AppException("Le taxon parent n'est pas en cache: " + idxParent);
		}
		
		log.info("Request to set " + child + " as child of " + parent);
		
		if (parent.getRank().getChildRank() == null) {
			throw new AppException("Le taxon " + parent.getName() + " ne peut pas avoir de sous-taxa");
		}
		
		if (parent.getRank().getChildRank() != child.getRank()) {
			throw new AppException("Le rang " + child.getRank().getGuiName() + 
					" ne peut pas avoir un parent de rang " + parent.getRank().getGuiName());
		}
		
		if (child.getParent() != parent) {
			child.setParent(parent);
			saveTaxon(child);
			TaxonCache.getInstance().loadAll();
		}
	}
	
	public void exportCharts() {
		ChartExporter.getInstance().exportCharts();
	}
	
	/**
	 * Saves the specified picture.
	 * 
	 * @param hpic  the picture to save
	 * @return the index of the saved picture
	 * @throws ValidationException  if saving the picture is invalid
	 */
	public int savePicture(HerbierPic hpic) throws ValidationException {
		log.info("Saving " + hpic);
		picValidator.validateSave(hpic);
		
		// save location if needed
		Location location = hpic.getLocation();
		if (location != null && location.getIdx() <= 0) {
			log.info("Saving new location " + location + ", for picture " + hpic);
			int idxLocation = saveLocation(location);
			location = LocationCache.getInstance().getLocation(idxLocation);
			hpic.setLocation(location);
		}
		
		int idx = DataAccess.getInstance().savePicture(hpic);
		notifyDataListeners(UpdateType.PICTURE, idx);
		return idx;
	}
	
	/**
	 * Saves the specified taxon to database.
	 * Refreshes the taxon cache.
	 * 
	 * @param taxon  the taxon to save
	 * @return the database index of the saved taxon
	 * @throws ValidationException  if saving is invalid
	 */
	public int saveTaxon(Taxon taxon) throws ValidationException {
		log.info("Saving " + taxon);

		taxonValidator.validateSave(taxon);

		int idx = DataAccess.getInstance().saveTaxon(taxon);

		TaxonCache.getInstance().refresh(idx);

		// replace refreshed taxon in pictures
		Taxon taxonUpd = TaxonCache.getInstance().getTaxon(idx);
		for (HerbierPic pic : taxon.getPics()) {
			pic.setTaxon(taxonUpd);
			taxonUpd.addPic(pic);
		}

		notifyDataListeners(UpdateType.TAXON, idx);
		return idx;
	}
	
	/**
	 * Deletes the specified picture from database and disk.
	 * 
	 * @param pic  the picture to delete
	 * @throws ValidationException  if it isn't allowed to delete the picture
	 */
	public void deletePicture(HerbierPic pic) throws ValidationException {
		log.info("Checking deletion of " + pic);
		picValidator.validateDelete(pic);
		
		importer.deletePic(pic);
		DataAccess.getInstance().deletePicture(pic);
		reloadCache();
		notifyDataListeners(UpdateType.PICTURE, 0);
	}
	
	/**
	 * Deletes the specified taxon from database.
	 * 
	 * @param taxon  the taxon to delete
	 * @throws ValidationException  if it isn't allowed to delete the taxon
	 */
	public void deleteTaxon(Taxon taxon) throws ValidationException {
		log.info("Checking deletion of " + taxon);
		taxonValidator.validateDelete(taxon);
		
		DataAccess.getInstance().deleteTaxon(taxon);
		TaxonCache.getInstance().loadAll();
		notifyDataListeners(UpdateType.TAXON, 0);
	}

	/**
	 * Saves the specified location to database.
	 * 
	 * @param location  the location to save
	 * @return  the database index
	 * @throws ValidationException  if saving is invalid
	 */
	public int saveLocation(Location location) throws ValidationException {
		log.info("Saving " + location);

		locationValidator.validateSave(location);

		int idx = DataAccess.getInstance().saveLocation(location);
		LocationCache.getInstance().refresh(idx);

		notifyDataListeners(UpdateType.LOCATION, idx);
		return idx;
	}

	/**
	 * Saves the specified expedition to database.
	 * 
	 * @param expedition  the expedition to save
	 * @return  the database index
	 * @throws ValidationException  if saving is invalid
	 */
	public int saveExpedition(Expedition expedition) throws ValidationException {
		log.info("Saving " + expedition);

		expeditionValidator.validateSave(expedition);

		int idx = DataAccess.getInstance().saveExpedition(expedition);

		notifyDataListeners(UpdateType.EXPEDITION, idx);
		return idx;
	}

	/**
	 * Gets the application parameter with the specified name from database.
	 * @param apName  the AppParam name
	 * @return  the fetched AppParam
	 */
	public AppParam getAppParam(AppParamName apName) {
		AppParam ap = DataAccess.getInstance().getAppParam(apName);
		log.info("Loaded " + ap);
		return ap;
	}
	
	/**
	 * Updates the specified application parameter in database.
	 * @param ap  the application parameter to update (may be null).
	 */
	public void saveAppParam(AppParam ap) {
		log.info("Saving " + ap);
		if (ap != null) {
			DataAccess.getInstance().saveAppParam(ap);
		}
	}
	
	/**
	 * Asks the {@link WebsiteExporter} to create a web site
	 */
	public void exportToHtml() {
		// make sure caches are up-to-date before exporting
		reloadCache();
		
		ExportManager.getInstance().export(TaxonCache.getInstance().getTopLevel());
	}
	
	public void uploadWebsite(ProgressListener progress, boolean bOnlyModified) {
		if (bOnlyModified) {
			UploadManager.getInstance().uploadModified(progress);
		} else {
			UploadManager.getInstance().upload(progress);
		}
	}
	
	/**
	 * Runs data quality checks. For example, checks that :
	 * <ul>
	 * <li>all pictures have a location
	 * <li>all locations have a description
	 * <li>all taxons have a french name
	 * </ul>
	 */
	public void checkDataQuality() {
		DataCheckManager.getInstance().checkData();
	}
	
	/**
	 * Gets the list of pictures to backup.
	 * @return list of pictures (may be empty, but never null)
	 */
	public Vector<HerbierPic> getPicsToBackup() {
		AppParam apLastAt = Controller.getInstance().getAppParam(AppParamName.BACKUP_MYBOOK);
		Date lastBackupAt = apLastAt.getDateValue();
		log.info("Last backup at " + lastBackupAt);

		Vector<HerbierPic> vecPics = DataAccess.getInstance().getHerbierPicsUpdatedAfter(lastBackupAt);
		return vecPics;
	}
	
	/**
	 * Gets the list of pictures to upload to website.
	 * @return list of pictures (may be empty, but never null)
	 */
	public Vector<HerbierPic> getPicsToUpload() {
		AppParam apLastAt = Controller.getInstance().getAppParam(AppParamName.WEB_UPLOAD);
		Date lastUploadAt = apLastAt.getDateValue();
		log.info("Last upload at " + lastUploadAt);

		Vector<HerbierPic> vecPics = DataAccess.getInstance().getHerbierPicsUpdatedAfter(lastUploadAt);
		return vecPics;
	}
	
	public void createThumbnailIfMissing(File orig, File thumb) {
		importer.createImageIfMissing(orig, thumb, 500, true);
	}
	
	/**
	 * Clears and closes everything
	 */
	public void terminate() {
		// try to backup
		try {
			BackupManager.getInstance().backupPictures();
		} catch (AppException e) {
			// can be ignored here
		}
		
		// save default location to app param
		if (defaultLocation != null) {
			AppParam apDefLocation = getAppParam(AppParamName.DEFAULT_LOCATION);
			if (apDefLocation != null) {
				apDefLocation.setIntValue(defaultLocation.getIdx());
				saveAppParam(apDefLocation);
			}
		}
		
		// cleanup and close
		TaxonCache.getInstance().clear();
		PictureCache.getInstance().clear();
		LocationCache.getInstance().clear();
		
		DataAccess.getInstance().terminate();
		log.info("Bye !");
	}
	
	public void addDataListener(DataListener listener) {
		vecDataListeners.add(listener);
		log.info("registered data listeners: " + vecDataListeners.size());
	}
	
	public void notifyDataListeners(final DatabaseTools.UpdateType updateType, final int idx) {
		if (vecDataListeners == null) return;

		log.info("notifying " + vecDataListeners.size() + 
				" data listeners of update in " + updateType);
		for (DataListener li : vecDataListeners) {
			switch(updateType) {
			case PICTURE:
				li.pictureUpdated(idx);
				break;
			case TAXON:
				li.taxonUpdated(idx);
				break;
			case LOCATION:
				li.locationUpdated(idx);
				break;
			case EXPEDITION:
				li.expeditionUpdated(idx);
				break;
			default:
			}
		}
	}

	
	private void setTaxonParentInfo(Vector<Taxon> taxa) {
		for (Taxon taxon : taxa) {
			int idxParent = taxon.getIdxParent();
			if (idxParent > 0) {
				Taxon parent = TaxonCache.getInstance().getTaxon(idxParent);
				if (parent != null) {
					taxon.setParent(parent);
					parent.addChild(taxon);
				}
			}
		}
	}


	private Controller() {
		vecDataListeners = new Vector<DataListener>();
		Logger.setGlobalDebug(true);
		
		reloadCache();
		
		// set default location from app param
		AppParam apDefLocation = getAppParam(AppParamName.DEFAULT_LOCATION);
		if (apDefLocation != null) {
			Location locDefault = LocationCache.getInstance().getLocation(apDefLocation.getIntValue());
			if (locDefault != null) {
				this.defaultLocation = locDefault;
			}
		}
		
		taxonValidator      = new TaxonValidator();
		picValidator        = new PicValidator();
		locationValidator   = new LocationValidator();
		expeditionValidator = new ExpeditionValidator();
		importer            = new Importer();
	}

//	private void updatePicShotAt(HerbierPic pic) {
//		Date tShotAt = FileManager.getInstance().getShotAt(new File(Controller.picturesPath + pic.getFileName()));
//		if (tShotAt == null) {
//			log.info("Failed to get picShotAt, skipping - " + pic);
//		} else {
//			if (tShotAt.after(pic.getShotAt())) {
//				log.info("Updating picShotAt from " + pic.getShotAt() + " to " + tShotAt + " for " + pic);
//				pic.setShotAt(tShotAt);
//				try {
//					savePicture(pic);
//				} catch (ValidationException e) {
//					log.error("Failed to update picShotAt: " + e.getMessage());
//				}
//			} else {
//				log.info("Pic has correct picShotAt - " + pic);
//			}
//		}
//	}

//	/**
//	 * @param args  unused
//	 */
//	public static void main(String[] args) {
//		//Controller.getInstance().createWebPage();
//		//Controller.getInstance().getFamilyByGenus();
//		for (HerbierPic pic : Controller.getInstance().getHerbierPics(null, null)) {
//			Controller.getInstance().updatePicShotAt(pic);
//		}
//	}

}
