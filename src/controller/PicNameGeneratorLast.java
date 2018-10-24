package controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.base.Logger;
import common.exceptions.AppException;

import model.Taxon;

/**
 * Helper class to find an available name for a picture and a taxon.
 * 
 * <p>Generates names with number at the end of the sequences, leaving any holes open.
 * For example, if existing pictures are pic001.jpg and pic003.jpg,
 * the generated name is pic004.jpg, not pic002.jpg.
 *
 * <p><b>Modifications:</b>
 * <ul>
 * <li>12.08.2018: nicz - Creation</li>
 * </ul>
 */
public class PicNameGeneratorLast extends PicNameGenerator {

	private static final Logger log = new Logger("PicNameGeneratorLast", true);

	/**
	 * Constructor.
	 * 
	 * @param taxon  the taxon for which to generate a name
	 */
	public PicNameGeneratorLast(Taxon taxon) {
		super(taxon);
	}

	@Override
	public String generateName(String sPicNumber) throws AppException {
		String name = null;
		log.info("Generating pic name at end of sequence for " + taxon);
		
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
			Pattern patTaxonFileName = Pattern.compile("^" + getBaseName() + "([0-9]+)\\.jpg");
			int iSeq = 0;
			for (String sExistingName : setNames) {
				Matcher match = patTaxonFileName.matcher(sExistingName);
				if (match.matches()) {
					Integer picNumber = Integer.valueOf(match.group(1));
					if (picNumber != null && picNumber.intValue() > iSeq) {
						iSeq = picNumber.intValue();
					}
				}
			}
			
			String sequence = String.format("%03d", iSeq+1);
			name = baseName + sequence + sExtension;
		}
		
		return name;
	}	
	
}
