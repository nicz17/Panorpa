package controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.HerbierPic;
import model.Taxon;
import model.TaxonRank;

import common.base.Logger;
import common.exceptions.ValidationException;


/**
 * Creates new taxons from species names using properties files.
 * 
 * @author nicz
 *
 */
public class TaxonFactory {
	
	private static final Logger log = new Logger("TaxonFactory", true);
	
	private static final String regexFileName   = "([a-z]+)-([a-z\\-]+)([0-9]+)\\.jpg";
	private static final String regexFamilyName = "([a-z]+[ci][de]ae)([0-9]+)\\.jpg";  // match -idae or -ceae
	private final Pattern patFileName;
	private final Pattern patFamilyName;
		
	private static Properties propFrenchnames;
	private static Properties propFamilies;
	
	
	/**
	 * Sets base taxa (species, genus, family) to the specified picture
	 * using its filename and properties files.
	 * Creates taxa if they are not found in cache.
	 * 
	 * <p>If species epithet is 'sp', does not set species, only genus.
	 * 
	 * @param pic  the picture to update with base taxa
	 * @throws ValidationException 
	 */
	public void setTaxons(HerbierPic pic) throws ValidationException {
		log.info("Setting base taxa to picture " + pic);
		
		if (pic == null) {
			log.error("Cannot set taxa to undefined picture!");
			return;
		}
		
		String fileName = pic.getFileName();
		Matcher matcher = patFileName.matcher(fileName);
		if (matcher.matches()) {
			String genus   = matcher.group(1);
			String epithet = matcher.group(2);
			genus = genus.substring(0, 1).toUpperCase() + genus.substring(1);
			
			String species = genus + " " + epithet;
			log.info("   Guessing species: " + species);

			Taxon taxon = null;
			
			if ("sp".equals(epithet)) {
				// look for genus in cache
				taxon = TaxonCache.getInstance().getTaxon(genus);
				if (taxon == null) {
					log.warn("Could not find genus " + genus + " in cache, creating it.");
					taxon = createGenus(genus);
				} 
			} else {
				// look for species in cache
				taxon = TaxonCache.getInstance().getTaxon(species);
				if (taxon == null) {
					log.warn("Could not find species " + species + " in cache, creating it.");
					taxon = createSpecies(species, genus);
				} 
			}
			
			if (TaxonRank.SPECIES.equals(taxon.getRank()) || TaxonRank.GENUS.equals(taxon.getRank()) ) {
				pic.setTaxon(taxon);
				pic.setIdxTaxon(taxon.getIdx());
			} else {
				log.error("Taxon " + taxon + " is not a species or genus!");
			}
		} else {
			// try to set a family
			matcher = patFamilyName.matcher(fileName);
			if (matcher.matches()) {
				String sFamily = matcher.group(1);
				sFamily = sFamily.substring(0, 1).toUpperCase() + sFamily.substring(1);
				Taxon taxFamily = TaxonCache.getInstance().getTaxon(sFamily);
				if (taxFamily != null && TaxonRank.FAMILY.equals(taxFamily.getRank())) {
					pic.setTaxon(taxFamily);
					pic.setIdxTaxon(taxFamily.getIdx());
				} else {
					log.warn("Could not find family " + sFamily + " in cache for " + pic);
				}
			} else {
				log.warn("No match for " + fileName);
			}
		}
	}
	
	private Taxon createSpecies(String species, String genus) throws ValidationException {
		Taxon taxSpecies = new Taxon(0, species, TaxonRank.SPECIES);
		String nameFr = propFrenchnames.getProperty(species);
		if (nameFr == null || nameFr.isEmpty()) {
			nameFr = species;
		}
		taxSpecies.setNameFr(nameFr);
		
		// look for genus in cache
		Taxon taxGenus = TaxonCache.getInstance().getTaxon(genus);
		if (taxGenus == null) {
			log.warn("Could not find genus " + genus + " in cache, creating it.");
			taxGenus = createGenus(genus);
		}
		
		if (TaxonRank.GENUS.equals(taxGenus.getRank())) {
			taxSpecies.setParent(taxGenus);
			taxSpecies.setIdxParent(taxGenus.getIdx());
		} else {
			log.error("Taxon " + taxGenus + " is not a genus!");
		}
		
		// save it
		int idx = Controller.getInstance().saveTaxon(taxSpecies);
		taxSpecies = TaxonCache.getInstance().getTaxon(idx);
		
		return taxSpecies;
	}
	
	private Taxon createGenus(String genus) throws ValidationException {
		Taxon taxGenus = new Taxon(0, genus, TaxonRank.GENUS);
		taxGenus.setNameFr(genus);
		
		// look for family in cache
		String family = propFamilies.getProperty(genus);
		if (family == null) {
			log.warn("Unknown family for genus " + genus + " in properties file, skipping family.");
		} else {
			log.info("Genus " + genus + " is in family " + family);
			Taxon taxFamily = TaxonCache.getInstance().getTaxon(family);
			if (taxFamily == null) {
				log.warn("Could not find family " + family + " in cache, creating it.");
				taxFamily = createFamily(family);
			} else {
				log.info("Family already exists: " + taxFamily);
			}

			if (TaxonRank.FAMILY.equals(taxFamily.getRank())) {
				taxGenus.setParent(taxFamily);
				taxGenus.setIdxParent(taxFamily.getIdx());
			} else {
				log.error("Taxon " + taxFamily + " is not a family!");
			}
		}
		
		// save it
		int idx = Controller.getInstance().saveTaxon(taxGenus);
		taxGenus = TaxonCache.getInstance().getTaxon(idx);
		
		return taxGenus;
	}
	
	private Taxon createFamily(String family) throws ValidationException {
		Taxon taxFamily = new Taxon(0, family, TaxonRank.FAMILY);
		taxFamily.setNameFr(family);
		
		// guess French name
		String nameFr = null;
		if (family.endsWith("eae")) {
			nameFr = family.replace("eae", "ées");
		}
		taxFamily.setNameFr(nameFr);

		// save and reload
		int idx = Controller.getInstance().saveTaxon(taxFamily);
		taxFamily = TaxonCache.getInstance().getTaxon(idx);
		return taxFamily;
	}

	private void loadProperties() {
		//log.debug("Loading properties");
	    try {
	    	propFamilies = new Properties();
	        propFamilies.load(new FileInputStream(Controller.resourcePath + "familyByGenus.properties"));
	    	propFrenchnames = new Properties();
	        propFrenchnames.load(new FileInputStream(Controller.resourcePath + "frenchNames.properties"));
	    } catch (IOException e) {
	    	log.error("Error loading properties: " + e.getMessage());
	    }
	}

	/** the singleton instance */
	private static TaxonFactory _instance = null;
	
	/** Gets the singleton instance. */
	public static TaxonFactory getInstance() {
		if (_instance == null)
			_instance = new TaxonFactory();
		return _instance;
	}
	
	/** Private singleton constructor */
	private TaxonFactory() {
		patFileName   = Pattern.compile(regexFileName);
		patFamilyName = Pattern.compile(regexFamilyName);
		loadProperties();
	}

}
