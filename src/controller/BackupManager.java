package controller;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import model.AppParam;
import model.AppParamName;
import model.HerbierPic;

import common.base.Logger;
import common.exceptions.AppException;

/**
 * Manages backup of pictures to MyBook external hard drive.
 * 
 * @author nicz
 *
 */
public class BackupManager {
	
	private static final Logger log = new Logger("BackupManager", true);
	
	private static final String backupDevice = "/media/nicz/My Book/";
	private static final String backupPath = backupDevice + "Photos/eHerbier/";
	
	/**
	 * Performs backup of pictures to MyBook.
	 * Only pictures updated after last backup are copied.
	 * @throws AppException  if backup device is not mounted.
	 */
	public void backupPictures() throws AppException {
		if (isDeviceMounted()) {
			log.info("Backup device is mounted.");

			Vector<HerbierPic> vecPics = Controller.getInstance().getPicsToBackup();
			if (!vecPics.isEmpty()) {
				log.info("Fetched " + vecPics.size() + " pictures to backup");

				for (HerbierPic pic : vecPics) {
					String[] args = new String[]{"cp", Controller.picturesPath + pic.getFileName(), backupPath};
					try {
						Runtime.getRuntime().exec(args);
					} catch (IOException e) {
						log.error("Failed to run command " + args + " : " + e.getMessage());
					}
				}

				// Update last-backup param
				AppParam apLastAt = Controller.getInstance().getAppParam(AppParamName.BACKUP_MYBOOK);
				apLastAt.setDateValue(new Date());
				Controller.getInstance().saveAppParam(apLastAt);
			} else {
				log.info("No pictures to backup.");
			}

		} else {
			log.warn("Backup device is not mounted, cannot backup. Expected at " + backupDevice);
			throw new AppException("Le disque de sauvegarde n'est pas monté\n" + backupDevice);
		}
	}
	
	private boolean isDeviceMounted() {
		boolean isMounted = false;
		
		File device = new File(backupDevice);
		isMounted = device.exists();
		
		return isMounted;
	}
	
	/** the singleton instance */
	private static BackupManager _instance = null;
	
	/** Gets the singleton instance. */
	public static BackupManager getInstance() {
		if (_instance == null)
			_instance = new BackupManager();
		return _instance;
	}
	
	/** Private singleton constructor */
	private BackupManager() {
	}
	
	/**
	 * @param args  unused
	 */
	public static void main(String[] args) {
		try {
			BackupManager.getInstance().backupPictures();
		} catch (AppException e) {
			e.printStackTrace();
		}
	}

}
