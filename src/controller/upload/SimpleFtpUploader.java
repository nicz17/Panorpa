package controller.upload;

import java.io.File;
import java.util.Vector;

import org.jibble.simpleftp.SimpleFTP;

import common.base.Logger;
import common.listeners.ProgressListener;

/**
 * Implementation of {@link FtpUploader} interface
 * using SimpleFTP.
 * 
 * <p>Implementation is limited, because listing files and 
 * deleting files is not supported by SimpleFTP.
 * 
 * <p>To be deprecated once {@link ApacheFtpUploader} is fully tested.
 * 
 * @author nicz
 *
 */
public class SimpleFtpUploader implements FtpUploader {

	private static final Logger log = new Logger("SimpleFtpUploader", true);
	
	private final String userName;
	private final String ftpAddress;
	private final String passwd;

	public SimpleFtpUploader(String ftpAddress, String userName, String passwd) {
		this.userName = userName;
		this.ftpAddress = ftpAddress;
		this.passwd = passwd;
	}

	@Override
	public void uploadFiles(String dir, Vector<File> vecFiles, ProgressListener progress) {
		// upload file to web server
		log.info("Uploading " + vecFiles.size() + " files with simpleFTP");
		if (vecFiles.isEmpty()) {
			return;
		}
		
		try {
		    SimpleFTP ftp = new SimpleFTP();
		    // Connect to an FTP server on port 21
		    ftp.connect(ftpAddress, 21, userName, passwd);
		    //ftp.cwd("subdomains/nature/httpdocs/");  // change working directory on FTP server
		    ftp.cwd("httpdocs/nature/");  // change working directory on FTP server
		    if (dir != null && !dir.isEmpty()) {
		    	ftp.cwd(dir);
		    }
		    
		    for (File file : vecFiles) {
		    	log.info("    Uploading " + file.getName());
			    progress.info("Upload de " + vecFiles.size() + " fichiers : " + 
			    		(dir == null ? "" : dir) + file.getName());
		    	ftp.stor(file);
		    	progress.taskProgress();
		    	Thread.sleep(10);
		    }
		    
		    ftp.disconnect();	// Quit from the FTP server.
		}
		catch (Exception e) {
		    log.error("FTP error: " + e.getMessage());
		    //MessageBox.error("Erreur FTP :\n" + e.getMessage());
		}

	}

	@Override
	public void deleteFiles(Vector<String> vecFiles) {
		// not supported
	}

	@Override
	public Vector<String> listFiles(String dir) {
		// not supported
		return null;
	}

}
