package controller.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.Vector;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;

import common.base.Logger;
import common.listeners.ProgressListener;

/**
 * Implementation of {@link FtpUploader} interface using the
 * Apache Commons FTPClient.
 * 
 * @author nicz
 *
 */
public class ApacheFtpUploader implements FtpUploader {

	private static final Logger log = new Logger("ApacheFtpUploader", true);
	
	private final String userName;
	private final String ftpAddress;
	private final String passwd;
	
	private FTPClient ftpClient;

	public ApacheFtpUploader(String ftpAddress, String userName, String passwd) {
		this.userName = userName;
		this.ftpAddress = ftpAddress;
		this.passwd = passwd;
		
		ftpClient = new FTPClient();
		FTPClientConfig config = new FTPClientConfig();
	    //config.set; // change required options
	    // for example config.setServerTimeZoneId("Pacific/Pitcairn")
	    ftpClient.configure(config);
	}

	@Override
	public void uploadFiles(String dir, Vector<File> vecFiles, ProgressListener progress) {
		log.info("Uploading " + vecFiles.size() + " files with Apache FTPClient");
		if (vecFiles.isEmpty()) {
			return;
		}
		
		try {
			connect();

			if (dir != null && !dir.isEmpty()) {
		    	ftpClient.changeWorkingDirectory(dir);
		    }
			
		    for (File file : vecFiles) {
		    	log.info("    Uploading " + file.getName());
		    	
		    	// set file type...
		    	if (file.getName().endsWith(".jpg")) {
		    		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		    	} else {
		    		ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
		    	}
		    	
			    progress.info("Upload de " + vecFiles.size() + " fichiers : " + 
			    		(dir == null ? "" : dir) + file.getName());
		    	boolean isSuccess = ftpClient.storeFile(file.getName(), new FileInputStream(file));
		    	if (!isSuccess) {
		    		log.error("Failed to upload " + file.getName());
		    	}
		    	progress.taskProgress();
		    }
		} catch (IOException e) {
			log.error("Failed to upload files to FTP server", e);
		} finally {
			disconnect();
		}
	}

	@Override
	public void deleteFiles(Vector<String> vecFiles) {
		try {
			connect();

			for (String path : vecFiles) {
				boolean isDeleted = ftpClient.deleteFile(path);
				log.info("Deletion of " + path + " " + (isDeleted ? "successful" : "failed"));
			}
		} catch (IOException e) {
			log.error("Failed to delete files from FTP server", e);
		} finally {
			disconnect();
		}
	}

	@Override
	public Vector<String> listFiles(String dir) {
		log.info("Listing files in " + dir + " with Apache FTPClient");
		Vector<String> vecFiles = new Vector<>();
		
		try {
			connect();

			if (dir != null && !dir.isEmpty()) {
		    	ftpClient.changeWorkingDirectory(dir);
		    }
			
			FTPFile[] files = ftpClient.listFiles();
			for (FTPFile file : files) {
				if (file.isFile()) {
					vecFiles.add(file.getName());
				}
			}
		    
		} catch (IOException e) {
			log.error("Failed to list files in FTP server", e);
		} finally {
			disconnect();
		}
		return vecFiles;
	}
	
	private void connect() throws SocketException, IOException {
		ftpClient.connect(ftpAddress);
		log.info("Connected to FTP server " + ftpAddress);
		ftpClient.login(userName, passwd);
		log.info("Successfully logged in as " + userName);
	    ftpClient.changeWorkingDirectory("httpdocs/nature/");		
	}
	
	private void disconnect() {
		if (ftpClient.isConnected()) {
			try {
				ftpClient.disconnect();
			} catch (IOException e) {
				log.error("Failed to disconnect from FTP server", e);
			}
		}
	}

}
