package controller.upload;

import java.io.File;
import java.util.Vector;

import common.listeners.ProgressListener;

/**
 * Interface describing methods used to upload files to a FTP server.
 * 
 * @author nicz
 *
 */
public interface FtpUploader {
	
	/**
	 * Uploads the specified list of files to the specified directory using FTP.
	 * Progress may be notified to the specified progress listener.
	 * 
	 * @param dir       the directory on the FTP server
	 * @param vecFiles  the list of files to upload (not null)
	 * @param progress  the progress listener (may be null)
	 */
	public void uploadFiles(String dir, Vector<File> vecFiles, ProgressListener progress);
	
	/**
	 * Deletes the file specified by its path on the FTP server.
	 * 
	 * @param vecFiles  the list of the files to delete.
	 */
	public void deleteFiles(Vector<String> vecFiles);
	
	/**
	 * Lists the files found in the specified path.
	 * 
	 * @param dir       the directory on the FTP server
	 * @return  the list of files
	 */
	public Vector<String> listFiles(String dir);

}
