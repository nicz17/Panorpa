package controller;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import common.base.Logger;

/**
 * Helper class to easily find the RAW image file matching
 * an edited Jpeg file.
 * 
 * Uses the shot-at metadata tag for matching.
 * 
 * @author nicz
 *
 */
public class RawFileMatcher {
	
	private static final Logger log = new Logger("RawFileMatcher", true);
	private static final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.FRANCE);
	
	private Vector<File> vecRawFiles;
	private Map<File, Date> mapShotAt;
	
	private boolean isUpdating = false;
	private boolean isCancelUpdate = false;

	/**
	 * Constructor
	 */
	public RawFileMatcher() {
		mapShotAt = new HashMap<File, Date>();
	}
	
	/**
	 * Get a matching raw image file for the specified edited jpg file.
	 * 
	 * @param from the edited jpg file
	 * @return the matching raw file, or null if not found.
	 */
	public synchronized File getMatchingRaw(File from) {
		Date dFrom = FileManager.getInstance().getShotAt(from);
		log.info("Searching raw file for " + from.getName() + " shot at " + dateFormat.format(dFrom));
		
		for (File file : vecRawFiles) {
			Date dShotAt = mapShotAt.get(file);
			if (dShotAt != null && dShotAt.equals(dFrom)) {
				log.info("Raw file " + file.getName() + " matches.");
				return file;
			}
		}
		
		log.info("No match found.");
		return null;
	}

	/**
	 * Sets the list of raw files to work with.
	 * 
	 * @param vecRawFiles a list of raw image files
	 */
	public void setRawFiles(Vector<File> vecRawFiles) {
		this.vecRawFiles = vecRawFiles;
		updateCache();
	}
	
	/**
	 * Updates the internal cache of shot-at dates by file.
	 * Done in a separate thread as operation is slow with large files.
	 */
	private void updateCache() {
		if (isUpdating) {
			log.warn("Cache update already in progress, aborting");
			isCancelUpdate = true;
			
			while (isUpdating) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
			}
		}
		
		// run in a thread as getShotAt is slow with large files
		new Thread(new Runnable() {
			@Override 
			public void run() {
				isUpdating = true;
				log.info("Updating cache of raw files from " + vecRawFiles.size() + " files");
				for (File file : vecRawFiles) {
					try {
						Thread.sleep(10);  // let the other threads work
						
						// handle interruption requests
						if (isCancelUpdate) {
							log.info("Aborting cache update");
							isUpdating = false;
							isCancelUpdate = false;
							return;
						}
						
						if (!mapShotAt.containsKey(file)) {
							Date dShotAt = FileManager.getInstance().getShotAt(file);
							mapShotAt.put(file, dShotAt);
						}
						
						// TODO (07.2017) remove non-existing files from map, if any
						
					} catch (InterruptedException e) {
						log.error("Cache update thread interrupted");
					}
				}
				isUpdating = false;
				isCancelUpdate = false;
				log.info("Updated cache has " + mapShotAt.size() + " raw files.");
			}
		}).start();
	}

}
