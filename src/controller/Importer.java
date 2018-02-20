package controller;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import model.HerbierPic;
import model.Taxon;
import model.TaxonRank;

import common.base.Logger;
import common.exceptions.AppException;
import common.exceptions.ValidationException;

import controller.checks.PictureChecker;

/**
 * Import images from disk.
 * 
 * @author nicz
 *
 */
public class Importer {
	
	private static final Logger log = new Logger("Importer", true);
	
	/**
	 * Imports pictures from jpg files.
	 * 
	 * @return  the list of pictures in folder
	 */
	protected Vector<File> getPicturesList() {
		log.info("Importing pictures from " + Controller.picturesPath);
		
		File directory = new File(Controller.picturesPath);

		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jpg");
			}
		};
	    File[] files = directory.listFiles(filter);
	    log.info("Found " + files.length + " files");
	    
	    Vector<File> vecFiles = new Vector<>();
	    for (File file : files) vecFiles.add(file);
	    return vecFiles;
	}
	
	protected long computeTotalFileSize() {
		long result = 0;
		
		File directory = new File(Controller.picturesPath);
		File[] files = directory.listFiles();
		
		for (File file : files) {
			result += file.length();
		}
		log.info("Computing total size of " + files.length + " files in " + 
				directory.getName() + " : " + result + " bytes");
		
		return result;
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
	protected String getAvailableFilename(HerbierPic hpic, Taxon taxon) throws AppException {
		log.info("Searching new filename for " + hpic + ", with taxon " + taxon);
		String filename = hpic.getFileName();
		
		if (taxon != null) {
			TaxonRank rank = taxon.getRank();
			if (TaxonRank.SPECIES == rank || TaxonRank.GENUS == rank) {
				filename = taxon.getName().toLowerCase();
				if (TaxonRank.GENUS == rank) {
					filename += "-sp";
				} else {
					filename = filename.replace(" ", "-");
				}
				
				boolean isAvailable = false;
				String baseFilename = filename;
				for (int iSeq = 1; iSeq<1000; ++iSeq) {
					String sequence = String.format("%03d", iSeq);
					String extension = ".jpg";
					filename = baseFilename + sequence + extension;
					
					boolean isInUse = false;
					for (HerbierPic taxPic : taxon.getPicsCascade()) {
						if (filename.equals(taxPic.getFileName())) {
							isInUse = true;
						}
					}
					
					if (!isInUse) {
						log.info("Found available filename " + filename);
						isAvailable = true;
						break;
					}
				}
				
				if (!isAvailable) {
					log.warn("No available filename found for " + hpic + ", with new taxon " + taxon);
					throw new AppException("Pas de noms de fichiers disponibles!");
				}
			}
		}
		return filename;
	}
	
	/**
	 * Renames the specified picture and changes its taxon.
	 * 
	 * @param hpic      the picture to update
	 * @param taxon     the new picture taxon
	 * @param filename  the new filename
	 * @throws AppException if target file already exists, or saving is invalid
	 */
	protected void renamePic(HerbierPic hpic, Taxon taxon, String filename) throws AppException {
		log.info("Renaming " + hpic + " to " + filename + ", with new taxon " + taxon);
		
		if (hpic == null || taxon == null || filename == null || filename.isEmpty()) {
			log.error("Invalid renaming info!");
			throw new AppException("Infos de reclassification invalides!");
		}
		
		boolean isDiffTaxon = !(hpic.getTaxon().equals(taxon));
		boolean isDiffName  = !(hpic.getFileName().equals(filename));
		
		if (!(isDiffName || isDiffTaxon)) {
			log.info("Nothing to do.");
		} else {
			if (isDiffName) {
				File file = new File(Controller.picturesPath + filename);
				if (file.exists()) {
					log.error("Target file already exists! " + filename);
					throw new AppException("Le fichier " + filename + " existe déjà!");
				} else {
					// rename files
					try {
						Runtime.getRuntime().exec("mv " + Controller.picturesPath + hpic.getFileName() + " " + 
								Controller.picturesPath + filename);
						Runtime.getRuntime().exec("mv " + Controller.mediumPath + hpic.getFileName() + " " + 
								Controller.mediumPath + filename);
						Runtime.getRuntime().exec("mv " + Controller.thumbsPath + hpic.getFileName() + " " + 
								Controller.thumbsPath + filename);
						hpic.setFileName(filename);
						hpic.setUpdatedAt(new Date());
					} catch (IOException e) {
						log.error("Rename failed: " + e.getMessage());
					}
				}
			}
			
			if (isDiffTaxon) {
				hpic.setTaxon(taxon);
				hpic.setIdxTaxon(taxon.getIdx());
			}
			
			// if all went well, save the pic
			Controller.getInstance().savePicture(hpic);
			Controller.getInstance().reloadCache();
		}
	}
	
	/**
	 * Deletes the specified picture and its thumbnails from disk.
	 * 
	 * @param hpic  the picture to delete
	 */
	protected void deletePic(HerbierPic hpic) {
		if (hpic == null) {
			return;
		}
		
		try {
			Runtime.getRuntime().exec("rm " + Controller.picturesPath + hpic.getFileName());
			Runtime.getRuntime().exec("rm " + Controller.mediumPath + hpic.getFileName());
			Runtime.getRuntime().exec("rm " + Controller.thumbsPath + hpic.getFileName());
		} catch (IOException e) {
			log.error("Rename failed: " + e.getMessage());
		}
	}
	
	/**
	 * Creates a {@link HerbierPic} from the specified file.
	 * Sets the shot-at date from EXIF tags.
	 * Creates thumbnail images if needed.
	 * 
	 * @param file  the file to create picture from
	 * @return  the created pic
	 */
	protected HerbierPic importOnePicture(File file) {
		String fileName = file.getName();
		//log.info("Importing " + fileName);
		
		HerbierPic hpic = new HerbierPic(0, fileName);
		getShotAt(hpic, file);
		
		// create thumbnail if needed
		File thumb = new File(Controller.thumbsPath + fileName);
		createImageIfMissing(file, thumb, 180, false);
		File medium = new File(Controller.mediumPath + fileName);
		createImageIfMissing(file, medium, 500, false);
		
		return hpic;
	}
	
	/**
	 * Copy an external file to the photos directory and import it.
	 * 
	 * @param fileFrom  the external photo to import.
	 * @throws AppException
	 */
	protected HerbierPic copyAndImport(File fileFrom) throws AppException {
		if (!fileFrom.exists()) {
			throw new AppException("La photo à importer n'existe pas :\n" + fileFrom.getAbsolutePath());
		}
		
		File fileTo = new File(Controller.picturesPath + fileFrom.getName());
		if (fileTo.exists()) {
			throw new AppException("La photo à importer existe déjà :\n" + fileTo.getAbsolutePath());
		}
		
		log.info("Will copy from " + fileFrom.getAbsolutePath() + " to " + fileTo.getAbsolutePath());
		try {
			Process proc = Runtime.getRuntime().exec("cp " + fileFrom.getAbsolutePath() + " " + fileTo.getAbsolutePath());
			proc.waitFor();
		} catch (Exception e) {
			throw new AppException("La copie du fichier a échoué :\n" + e.getMessage());
		}
		
		HerbierPic pic = importOnePicture(fileTo);
		return pic;
	}
	
	/**
	 * Checks that the size (height or width) of the specified image
	 * does not exceed IMAGE_LARGE_SIZE_WARN.
	 * 
	 * @param file  the file to check
	 * @throws ValidationException  if image is too large
	 */
	protected void checkImageSize(File file) throws ValidationException {
		int size = PictureChecker.getImageSize(file);
		if (size > PictureChecker.IMAGE_LARGE_SIZE_WARN) {
			log.warn("Large image size : " + size + " for " + file.getName());
			throw new ValidationException("Image de grande taille : " + size + "px\n" + file.getName());
		}
	}

	protected void createImageIfMissing(File orig, File img, int size, boolean isWait) {
		if (!img.exists()) { 
		    log.info("Will create missing " + size + "px thumbnail for " + orig.getName());
		    
		    try {
				Process procConvert = Runtime.getRuntime().exec("convert -resize " + size + "x" + size + " " +
						orig.getAbsolutePath() + " " + img.getAbsolutePath());
				if (isWait) {
					try {
						procConvert.waitFor();
					} catch (InterruptedException e) {
						
					}
				}
			} catch (IOException e) {
				log.error("Failed to resize image: " + e.getMessage());
			}
		}		
	}
	
	/**
	 * Get shot-at date from EXIF tags
	 * @param hpic  the picture object to update
	 * @param file  the jpeg file to parse
	 */
	private void getShotAt(HerbierPic hpic, File file) {
		Date dShotAt = FileManager.getInstance().getShotAt(file);
		hpic.setShotAt(dShotAt);
	}

}
