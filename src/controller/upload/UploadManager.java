package controller.upload;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import model.AppParam;
import model.AppParamName;
import model.HerbierPic;
import model.Taxon;
import model.TaxonRank;

import common.base.Logger;
import common.listeners.ProgressListener;

import controller.Controller;
import controller.TaxonCache;

/**
 * Manages upload of pictures and files to public website.
 * 
 * @author nicz
 *
 */
public class UploadManager {
	
	private static final Logger log = new Logger("UploadManager", true);
	
	private static final String userName = "nzwahlen";
	private static final String ftpAddress = "ftp.tf79.ch";
	private static final String htmlPath = Controller.htmlPath;
	private final String passwd;

	/** FTP client used to upload pictures */
	private final FtpUploader ftpUploader;
	
	/** FTP client used to list pictures and delete them */
	private final FtpUploader ftpUploaderCleanup;
	
	/**
	 * Uploads html pages and images to website using FTP.
	 * 
	 * @param progress  the progress listener to notify of upload progress.
	 */
	public void upload(ProgressListener progress) {
		log.info("Uploading website");
		
		uploadBaseFiles(progress);
		uploadAllPages(progress);
		uploadPictures(progress);

		// Update last-upload param
		AppParam apLastAt = Controller.getInstance().getAppParam(AppParamName.WEB_UPLOAD);
		apLastAt.setDateValue(new Date());
		Controller.getInstance().saveAppParam(apLastAt);
	}
	
	/**
	 * Uploads only modified html pages and images to website using FTP.
	 * 
	 * @param progress  the progress listener to notify of upload progress.
	 */
	public void uploadModified(ProgressListener progress) {
		log.info("Uploading website");
		
		uploadBaseFiles(progress);
		uploadModifiedPages(progress);
		uploadPictures(progress);

		// Update last-upload param
		AppParam apLastAt = Controller.getInstance().getAppParam(AppParamName.WEB_UPLOAD);
		apLastAt.setDateValue(new Date());
		Controller.getInstance().saveAppParam(apLastAt);
	}
	
	public int cleanup() {
		int nFilesDeleted = 0;
		
		log.info("Cleanup of FTP server");
		Vector<String> vecFilesToDelete = getFilesToCleanup();
	    
	    log.info("Found " + vecFilesToDelete.size() + " files to delete");
		
	    if (!vecFilesToDelete.isEmpty()) {
	    	ftpUploaderCleanup.deleteFiles(vecFilesToDelete);
	    	nFilesDeleted = vecFilesToDelete.size();
	    }
		
		return nFilesDeleted;
	}
	
	public Vector<String> checkFilesToCleanup() {
		log.info("Checking for files to cleanup on FTP server");
		Vector<String> vecFilesToDelete = getFilesToCleanup();
	    
	    log.info("Found " + vecFilesToDelete.size() + " files to delete");
	    return vecFilesToDelete;
	}
	
	public String getTaxonHtmlFileName(Taxon taxon) {
		String name = taxon.getName();
		name = name.replaceAll(" ", "-");
		name = name.toLowerCase();
		name += ".html";
		return name;
	}

	
	private Vector<String> getFilesToCleanup() {
		Vector<String> vecFilesToDelete = new Vector<>();
		
		vecFilesToDelete.addAll(getFilesToCleanup("pages/"));
		vecFilesToDelete.addAll(getFilesToCleanup("photos/"));
		vecFilesToDelete.addAll(getFilesToCleanup("medium/"));
		vecFilesToDelete.addAll(getFilesToCleanup("thumbs/"));
	    
	    return vecFilesToDelete;
	}
	
	private Vector<String> getFilesToCleanup(String dir) {
		log.info("Checking for files to cleanup on FTP server in " + dir);
		Vector<String> vecFilesToDelete = new Vector<>();
		
		File directory = new File(htmlPath + dir);
		File[] files = directory.listFiles();
	    Vector<String> vecLocFiles = new Vector<>();
	    for (File file : files) {
	    	vecLocFiles.add(file.getName());
	    }
	    
	    Vector<String> vecFtpFiles = ftpUploaderCleanup.listFiles(dir);
	    log.info("Found " + vecFtpFiles.size() + " files on FTP server");
	    log.info("Found " + vecLocFiles.size() + " files locally");
	    
	    for (String filename : vecFtpFiles) {
	    	if (!vecLocFiles.contains(filename)) {
	    		log.info("... file to cleanup: " + filename);
	    		vecFilesToDelete.add(dir + filename);
	    	}
	    }
	    
	    log.info("Found " + vecFilesToDelete.size() + " files to delete in " + dir);
	    return vecFilesToDelete;
	}
	
	private void uploadBaseFiles(ProgressListener progress) {
		log.info("Uploading base files from " + htmlPath);
		
		File directory = new File(htmlPath);

		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".html") || name.endsWith(".css") || name.endsWith(".json");
			}
		};
	    File[] files = directory.listFiles(filter);
	    log.info("Found " + files.length + " base html files");
	    
	    Vector<File> vecFiles = new Vector<>();
	    for (File file : files) {
	    	vecFiles.add(file);
	    }
	    
	    progress.taskStarted(vecFiles.size());
	    progress.info("Upload de " + vecFiles.size() + " pages html de base");
	    ftpUploader.uploadFiles(null, vecFiles, progress);
	    progress.taskFinished();
	}
	
	private void uploadAllPages(ProgressListener progress) {
		log.info("Uploading all html pages from " + htmlPath);
		
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".html") || name.endsWith(".css") || name.endsWith(".json");
			}
		};
	    
	    // individual pages
	    File directory = new File(htmlPath + "pages/");
	    File[] files = directory.listFiles(filter);
	    log.info("Found " + files.length + " individual html files");
	    
	    Vector<File> vecFiles = new Vector<>();
	    for (File file : files) {
	    	vecFiles.add(file);
	    }
	    
	    uploadPages(vecFiles, progress);
	}
	
	private void uploadModifiedPages(ProgressListener progress) {
		log.info("Uploading modified html pages from " + htmlPath);
		
		Vector<HerbierPic> vecPics = Controller.getInstance().getPicsToUpload();
		// get all orders with modified pics
		Set<Taxon> setModifiedOrders = new HashSet<>();
		for (HerbierPic pic : vecPics) {
			Taxon taxon = TaxonCache.getInstance().getTaxon(pic.getIdxTaxon());
			Taxon order = taxon.getAncestor(TaxonRank.ORDER);
			if (order != null) {
				setModifiedOrders.add(order);
			}
		}
		
		log.info("Found " + setModifiedOrders.size() + " orders with modified pics:");
		for (Taxon order : setModifiedOrders) {
			log.info("... " + order);
		}
		
		// get the list of html files to upload for these orders
		Set<File> setFiles = new HashSet<>();
		for (Taxon order : setModifiedOrders) {
			for (HerbierPic pic : order.getPicsCascade()) {
				//String filename = htmlPath + "pages/" + pic.getFileName().replace(".jpg", ".html");
				String filename = htmlPath + "pages/" + getTaxonHtmlFileName(pic.getTaxon());
				File file = new File(filename);
				if (file.exists()) {
					setFiles.add(file);
				} else {
					log.error("Unknown file to upload: " + filename);
				}
			}
		}
		Vector<File> vecFiles = new Vector<>();
		vecFiles.addAll(setFiles);
		
		log.info("Found " + vecFiles.size() + " modified pages to upload");
		uploadPages(vecFiles, progress);
	}
	
	private void uploadPages(Vector<File> vecFiles, ProgressListener progress) {
	    progress.taskStarted(vecFiles.size());
	    progress.info("Upload de " + vecFiles.size() + " pages html individuelles");
	    ftpUploader.uploadFiles("pages/", vecFiles, progress);
	    progress.taskFinished();
	}

	private void uploadPictures(ProgressListener progress) {
		Vector<HerbierPic> vecPics = Controller.getInstance().getPicsToUpload();
		log.info("Found " + vecPics.size() + " to upload");
		
		if (!vecPics.isEmpty()) {
			uploadPictures(vecPics, "thumbs/", progress);
			uploadPictures(vecPics, "medium/", progress);
			uploadPictures(vecPics, "photos/", progress);
		}
	}
	
	private void uploadPictures(Vector<HerbierPic> vecPics, String dir, ProgressListener progress) {
		Vector<File> vecFiles = new Vector<>();
	    for (HerbierPic pic : vecPics) {
	    	vecFiles.add(new File(htmlPath + dir + pic.getFileName()));
	    }
	    
	    progress.taskStarted(vecFiles.size());
	    progress.info("Upload de " + vecFiles.size() + " photos dans " + dir);
	    ftpUploader.uploadFiles(dir, vecFiles, progress);
	    progress.taskFinished();
	}


	/** the singleton instance */
	private static UploadManager _instance = null;
	
	/** Gets the singleton instance. */
	public static UploadManager getInstance() {
		if (_instance == null)
			_instance = new UploadManager();
		return _instance;
	}
	
	/** Private singleton constructor */
	private UploadManager() {
		passwd = Controller.getInstance().getAppParam(AppParamName.FTP_PWD).getStrValue();
		ftpUploader = new SimpleFtpUploader(ftpAddress, userName, passwd);
		ftpUploaderCleanup = new ApacheFtpUploader(ftpAddress, userName, passwd);
	}

}
