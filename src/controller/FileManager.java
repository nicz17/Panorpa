package controller;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Matcher;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;

import common.base.Logger;
import common.exceptions.AppException;
import controller.export.ExpeditionsExporter;
import model.HerbierPic;
import model.OriginalPic;

/**
 * Manages file operations:
 * <ul>
 * <li>Rename raw files
 * </ul>
 * 
 * @author nicz
 *
 */
public class FileManager {
	
	private static final Logger log = new Logger("FileManager", true);
	
	public static final String pathRaw = "/home/nicz/Pictures/";
	
	/**
	 * Renames a RAW image file.
	 * 
	 * @param picFileName  the HerbierPic file name (no path) to use as new name
	 * @param rawFile      the RAW image file to rename
	 * @throws AppException
	 */
	public void renameRawFile(String picFileName, File rawFile) throws AppException {
		
		if (!rawFile.exists()) {
			throw new AppException("Le fichier raw à renommer n'existe pas :\n" +
					rawFile.getAbsolutePath());
		}
		
		String oldFileName = rawFile.getAbsolutePath();
		String newFileName = rawFile.getParent() + "/" + picFileName.replaceFirst(".jpg", ".NEF");
		log.info("Renaming " + oldFileName + " to " + newFileName);
		
		File fileNew = new File(newFileName);
		if (fileNew.exists()) {
			throw new AppException("Le fichier à créer existe déjà :\n" +
					fileNew.getAbsolutePath());
		}
		
		try {
			Process proc = Runtime.getRuntime().exec("mv " + oldFileName + " " + newFileName);
			proc.waitFor();
		} catch (Exception e) {
			log.error("Rename failed: " + e.getMessage());
		}
	}
	
	/**
	 * Lists RAW image files from the specified directory.
	 * 
	 * @param dir          the directory to get files from
	 * @param regexFilter  a regex file name filter (may be null)
	 * @return a list of matching files
	 * @throws AppException
	 */
	public Vector<File> getRawFiles(String dir, String regexFilter) throws AppException {
		Vector<File> result = new Vector<>();
		
		File directory = new File(dir);
		if (!(directory.exists() && directory.isDirectory())) {
			throw new AppException("Le répertoire n'existe pas : " + dir);
		}
		
		if (regexFilter == null || regexFilter.isEmpty()) {
			regexFilter = ".*\\.NEF";
		}
		final String sRegexFilter = regexFilter;

		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches(sRegexFilter);
			}
		};
	    File[] files = directory.listFiles(filter);
	    log.info("Found " + files.length + " files");
	    
	    for (File file : files) {
	    	result.add(file);
	    }
	    Collections.sort(result);
		return result;
	}
	
	/**
	 * Lists JPG image files from the specified directory.
	 * 
	 * @param dir          the directory to get files from
	 * @param regexFilter  a regex file name filter (may be null)
	 * @return a list of matching files
	 * @throws AppException
	 */
	public Vector<OriginalPic> getOrigFiles(File directory, String regexFilter) throws AppException {
		Vector<OriginalPic> result = new Vector<>();
		if (directory == null) {
			throw new AppException("Le répertoire orig/ n'est pas défini");
		}
		
		log.info("Loading orig JPG files from " + directory.getAbsolutePath());
		if (!(directory.exists() && directory.isDirectory())) {
			throw new AppException("Le répertoire n'existe pas : " + directory.getName());
		}
		
		if (regexFilter == null || regexFilter.isEmpty()) {
			regexFilter = ".*\\.JPG";
		}
		//final String sRegexFilter = regexFilter;

		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				//return name.matches(sRegexFilter);
				return true;
			}
		};
	    File[] files = directory.listFiles(filter);
	    log.info("Found " + files.length + " orig files");
	    
	    int idx = 1;
	    for (File file : files) {
	    	Matcher match = OriginalPic.patOrigFileName.matcher(file.getName());
			if (match.matches()) {
				String sPicNumber = match.group(1);
				idx = Integer.parseInt(sPicNumber);
			} else {
				idx = 0;
				log.error("Unexpected orig file name, missing serial number: " + file.getName());
			}
	    	result.add(new OriginalPic(idx, file));
	    }
	    Collections.sort(result);
		return result;
	}
	
	public Vector<File> getSelectedFiles(File directory, String sFilter) throws AppException {
		Vector<File> result = new Vector<>();
		
		log.info("Loading selected .jpg files from " + directory.getAbsolutePath());
		if (!(directory.exists() && directory.isDirectory())) {
			throw new AppException("Le répertoire n'existe pas : " + directory.getName());
		}
		
		if (sFilter == null || sFilter.isEmpty()) {
			sFilter = ".jpg";
		}
		final String sRegexFilter = sFilter;

		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.contains(sRegexFilter);
				//return true;
			}
		};
	    File[] files = directory.listFiles(filter);
	    log.info("Found " + files.length + " selected files");
	    
	    for (File file : files) {
	    	result.add(file);
	    }
	    Collections.sort(result);
		return result;
	}
	
	/**
	 * Copies an orig/ JPG file to the photos/ dir with the specified name.
	 * 
	 * @param fileOrig  the original JPEG file to preselect
	 * @param name      the name of the file to create
	 * @throws AppException
	 */
	public void preselectFile(File fileOrig, String name) throws AppException {
		
		if (fileOrig == null) {
			throw new AppException("Le fichier original à renommer n'est pas défini.");
		}
		
		log.info("Preselecting orig file " + fileOrig.getName() + " as " + name);
		
		if (!fileOrig.exists()) {
			throw new AppException("Le fichier original à renommer n'existe pas :\n" +
					fileOrig.getAbsolutePath());
		}
		
		if (name == null || name.isEmpty()) {
			throw new AppException("Le nom de fichier ne peut pas être vide.");
		}
		
		String oldFileName = fileOrig.getAbsolutePath();
		String newFileName = fileOrig.getParent().replace("/orig", "/photos") + "/" + name;
		String rawFileName = oldFileName.replace("/orig", "/raw").replace(".JPG", ".NEF");
		log.info("Copying " + oldFileName + " to " + newFileName);
		
		File fileNew = new File(newFileName);
		if (fileNew.exists()) {
			throw new AppException("Le fichier à créer existe déjà :\n" + fileNew.getAbsolutePath());
		}
		
		try {
			Process proc = Runtime.getRuntime().exec("cp " + oldFileName + " " + newFileName);
			proc.waitFor();
		} catch (Exception e) {
			log.error("Rename failed: " + e.getMessage());
			throw new AppException(e);
		}
		
		// rename raw file too (ignore failures)
		try {
			renameRawFile(name, new File(rawFileName));
		} catch (AppException exc) {
			log.error("Failed to rename raw file: " + exc.getMessage());
		}
	}
	
	/**
	 * Gets the date when the picture was shot,
	 * using the 'DateTime-Original' metadata tag.
	 * 
	 * @param file  file holding an image with metadata.
	 * @return  the picture shot-at date, or null if not found.
	 */
	public Date getShotAt(File file) {
		Date dShotAt = null;
		if (file != null && file.exists()) {
			try {
				Metadata metadata = ImageMetadataReader.readMetadata(file);
				
				// obtain the Exif directory
				ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

				// query the tag's value
				//dShotAt = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL); + 1h shift
				dShotAt = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getDefault());
			} catch (Exception e) {
				log.error("Failed to read image date", e);
			}
		}
		return dShotAt;
	}
	
	/**
	 * Sets GPS longitude and latitude to the specified photo.
	 * Uses ImageMetadataReader GpsDirectory.
	 * @param pic  the photo to update (not null)
	 */
	public void addGPSCoords(HerbierPic pic) {
		String filename = Controller.picturesPath + pic.getFileName();
		File file = new File(filename);
		if (file.exists()) {
			try {
				Metadata metadata = ImageMetadataReader.readMetadata(file);
				GpsDirectory directory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
				if (directory != null) {
					GeoLocation location = directory.getGeoLocation();
					if (location != null) {
						pic.setLatitude(location.getLatitude());
						pic.setLongitude(location.getLongitude());
					}
				}
			} catch (Exception e) {
				log.error("Failed to read image GPS location", e);
			}
		} else {
			log.error("File does not exist: " + file);
		}
	}
	
	/**
	 * Moves .NEF raw image files from orig/ to raw/ directories.
	 * Useful after downloading all files from camera to the orig/ dir.
	 * 
	 * @param dirRaw  the directory where to move raw files.
	 * @throws AppException
	 */
	public void moveRawFiles(String dirRaw) throws AppException {
		File directoryRaw = new File(dirRaw);
		if (!(directoryRaw.exists() && directoryRaw.isDirectory())) {
			throw new AppException("Le répertoire raw n'existe pas : " + dirRaw);
		}
		
		String dirOrig = dirRaw.replace("/raw", "/orig");
		File directoryOrig = new File(dirOrig);
		if (!(directoryOrig.exists() && directoryOrig.isDirectory())) {
			throw new AppException("Le répertoire orig n'existe pas : " + dirOrig);
		}
		
		String cmd = "mv " + dirOrig + "*.NEF " + dirRaw;
		log.info("Will execute command: " + cmd);
		
		try {
			Process proc = Runtime.getRuntime().exec(cmd);
			proc.waitFor();
		} catch (Exception e) {
			log.error("Moving raw files failed: " + e.getMessage());
		}
	}
	
	/**
	 * Copy new GeoTrack files (*.gpx) from Dropbox
	 * to the current month directory.
	 * @throws AppException  if failed to find Dropbox dir
	 */
	public void getNewGeoTracks() throws AppException {
		final String path = Controller.geoTrackPath;
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyMM");
		final String sRegexFilter = ".*" + dateFormat.format(new Date()) + "..\\.gpx";
		log.info("Upload new GeoTracks: looking for files like " + path + sRegexFilter);

		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches(sRegexFilter);
			}
		};
		
		File dir = new File(path);
		if (!dir.exists()) {
			throw new AppException("Le répertoire Dropbox GeoTrack n'existe pas : " + path);
		}
		
	    File[] files = dir.listFiles(filter);
	    if (files != null) {
	    	log.info("Found " + files.length + " GPX files");
	    	for (File file : files) {
	    		log.info("New GPX track: " + file.getAbsolutePath());
	    		String cmd = "cp " + file.getAbsolutePath() + " " + getCurrentBaseDir() + "geotracker/";
	    		
	    		//log.info("Will execute command: " + cmd);
	    		try {
	    			Process proc = Runtime.getRuntime().exec(cmd);
	    			proc.waitFor();
	    		} catch (Exception e) {
	    			log.error("Copying GeoTrack file failed: " + e.getMessage());
	    		}
	    	}
	    } else {
	    	log.info("Found nothing.");
	    }
	}
	
	/**
	 * Copies the specified file to geotrack/ dir for upload
	 * @param fileTrack  the file to store
	 * @throws AppException  if file does not exist
	 */
	public void storeGeoTrack(File fileTrack) throws AppException {
		log.info("Storing GeoTrack file " + fileTrack);
		if (!fileTrack.exists()) {
			throw new AppException("Le fichier n'existe pas : " + fileTrack);
		}

		String dirGeoTrack = Controller.htmlPath + ExpeditionsExporter.dirTrack;
		String cmd = "cp " + fileTrack.getAbsolutePath() + " " + dirGeoTrack;
		log.info("Will execute command: " + cmd);
		
		try {
			Process proc = Runtime.getRuntime().exec(cmd);
			proc.waitFor();
		} catch (Exception e) {
			log.error("Storing GeoTrack file failed: " + e.getMessage());
		}
	}
	
	/**
	 * Gets the current base pictures directory, based on the year and month.
	 * For example, /home/nicz/Pictures/Nature-2017-04/.
	 * 
	 * @return  the default base directory for pic files
	 */
	public String getCurrentBaseDir() {
		String result = pathRaw;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
		result += "Nature-" + dateFormat.format(new Date()) + "/"; 
		
		return result;
	}
	
	/**
	 * Gets the current default RAW pictures directory, based on the year and month.
	 * For example, /home/nicz/Pictures/Nature-2017-04/raw/.
	 * 
	 * @return  the default directory for RAW files
	 */
	public String getCurrentRawDir() {
		String result = getCurrentBaseDir() + "raw/"; 
		return result;
	}
	
	/** the singleton instance */
	private static FileManager _instance = null;
	
	/** Gets the singleton instance. */
	public static FileManager getInstance() {
		if (_instance == null)
			_instance = new FileManager();
		return _instance;
	}
	
	/** Private singleton constructor */
	private FileManager() {
	}
	
	/**
	 * @param args  unused
	 */
	public static void main(String[] args) {
		try {
			//FileManager.getInstance().getRawFiles(pathRaw + "ChampPittet/raw", null);
			FileManager.getInstance().getNewGeoTracks();
		} catch (AppException e) {
			e.printStackTrace();
		}
	}

}
