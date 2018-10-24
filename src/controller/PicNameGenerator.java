package controller;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import model.HerbierPic;
import model.Taxon;

import common.base.Logger;
import common.exceptions.AppException;

/**
 * Helper class to find an available name for a picture and a taxon.
 * 
 * @author nicz
 *
 */
public class PicNameGenerator {

	private static final Logger log = new Logger("PicNameGenerator", true);
	public  static final String sExtension = ".jpg";
	
	protected final Taxon taxon;
	protected final Set<String> setNames;

	/**
	 * Constructor.
	 * 
	 * @param taxon  the taxon for which to generate a name
	 */
	public PicNameGenerator(Taxon taxon) {
		this.taxon = taxon;
		this.setNames = new HashSet<String>();
		reset();
	}
	
	/**
	 * Generates the next available filename.
	 * @param sPicNumber  the existing picture number (from the camera)
	 * @return the next available filename.
	 * @throws AppException if name generation failed
	 */
	public String generateName(String sPicNumber) throws AppException {
		String name = null;
		log.info("Generating pic name for " + taxon);
		
		if (taxon == null) {
			throw new AppException("Le taxon doit être défini!");
		}
		
		String baseName = getBaseName();
		if (baseName == null) {
			throw new AppException("Erreur interne lors de la génération de nom pour le taxon\n" + taxon);
		}
		
		boolean usePicNumber = usePicNumberForTaxon();
		
		if (usePicNumber && sPicNumber != null && !sPicNumber.isEmpty()) {
			name = baseName + sPicNumber + sExtension;
		} else {
			boolean isAvailable = false;
			for (int iSeq = 1; iSeq<1000; ++iSeq) {
				// TODO this will fill in holes in numbering, maybe better to generate max+1 ?
				String sequence = String.format("%03d", iSeq);
				name = baseName + sequence + sExtension;

				boolean isInUse = false;
				for (String sExistingName : setNames) {
					if (name.equals(sExistingName)) {
						isInUse = true;
					}
				}

				if (!isInUse) {
					log.info("Found available filename " + name);
					isAvailable = true;
					break;
				}
			}

			if (!isAvailable) {
				log.warn("No available filename found for taxon " + taxon);
				throw new AppException("Pas de noms de fichiers disponibles!");
			}
		}
		
		return name;
	}
	
	/**
	 * Adds the specified list of existing names.
	 * @param names names of files already existing
	 */
	public void addExistingNames(Collection<String> names) {
		setNames.addAll(names);
	}
	
	/**
	 * Resets the list of existing file names for our taxon.
	 */
	public void reset() {
		setNames.clear();
		for (HerbierPic pic : taxon.getPics()) {
			setNames.add(pic.getFileName());
		}
	}
	
	/**
	 * Gets the base filename for our taxon.
	 * @return the base filename for our taxon.
	 */
	public String getBaseName() {
		String name = taxon.getName().toLowerCase();
		
		switch(taxon.getRank()) {
		case SPECIES:
			name = name.replace(" ", "-");
			break;
		case GENUS:
			name += "-sp";
			break;
		default:
			break;
		}
		
		return name;
	}
	
	/**
	 * Gets the base filename for our taxon.
	 * @return the base filename for our taxon.
	 */
	public boolean usePicNumberForTaxon() {
		switch(taxon.getRank()) {
		case SPECIES:
		case GENUS:
			return false;
		default:
			return true;
		}
	}

}
