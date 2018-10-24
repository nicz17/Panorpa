package controller.export;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import model.Taxon;
import model.TaxonRank;
import common.io.HtmlComposite;

/**
 * This class provides html links to external websites offering additional info about taxa,
 * usually at species level.
 * For example, insecte.org for insects, Vogelwarte.ch for birds etc.
 * 
 * @author nicz
 *
 */
public class TaxonUrlProvider {
	
	private final Set<String> setClassInGalerieInsecte;
	private final Set<String> setFamilyInHomoptera;
	private final Set<String> setPhylumInInfoFlora;
	private final Set<String> setPhylumInMycoDb;

	public TaxonUrlProvider() {
		setClassInGalerieInsecte = new HashSet<String>(Arrays.asList(
				new String[]{"Arachnida", "Chilopoda", "Crustacea", "Diplopoda", "Entognatha", "Insecta"}));
		
		setFamilyInHomoptera = new HashSet<String>(Arrays.asList(
				new String[]{"Aphididae", "Aphrophoridae", "Cercopidae", "Cicadellidae", 
						"Cicadidae", "Cixiidae", "Dictyopharidae", "Issidae"}));
		
		setPhylumInInfoFlora = new HashSet<String>(Arrays.asList(
				new String[]{"Lycopodiophyta", "Pteridophyta", "Pinophyta", "Magnoliophyta"}));
		
		setPhylumInMycoDb = new HashSet<String>(Arrays.asList(
				new String[]{"Ascomycota", "Basidiomycota"}));
	}
	
	/**
	 * Adds relevant html links for the specified taxon.
	 * Always adds a wikipedia and wikispecies link, sometimes more.
	 * 
	 * @param par    the html composite to add links to
	 * @param taxon  the taxon to find links for
	 */
	public void addLinks(HtmlComposite par, Taxon taxon) {
		par.addLinkExternal(getWikipediaUrl(taxon),   "Wikipedia",   "Wikipedia");
		par.addText(" | ");
		par.addLinkExternal(getWikispeciesUrl(taxon), "Wikispecies", "Wikispecies");
		
		// TODO use a switch on rank and class to avoid multiple taxon lookups
		if (TaxonRank.SPECIES == taxon.getRank()) {
			Taxon taxClass = taxon.getAncestor(TaxonRank.CLASS);
			
			// link to galerie-insecte ?
			if (setClassInGalerieInsecte.contains(taxClass.getName())) {
				addLink(par, taxon, getGalerieInsecteUrl(taxon), "Galerie insecte", "Galerie insecte");
			}
			
			if ("Insecta".equals(taxClass.getName())) {
				// link to Pyrgus.de ?
				addLink(par, taxon, getPyrgusUrl(taxon), "Pyrgus", "www.pyrgus.de");
				
				// link to BritishBugs ?
				addLink(par, taxon, getBritishBugsUrl(taxon), "British Bugs", "www.britishbugs.org.uk");
				
				// link to AntWiki ?
				addLink(par, taxon, getAntWikiUrl(taxon), "AntWiki", "www.antwiki.org");
				
			} else if ("Arachnida".equals(taxClass.getName())) {
				// link to Arages ?
				addLink(par, taxon, getAragesUrl(taxon), "Arages", "wiki.arages.de");
				
			} else if ("Aves".equals(taxClass.getName())) {
				// link to Vogelwarte ?
				addLink(par, taxon, getVogelwarteUrl(taxon), "Vogelwarte", "Oiseaux de Suisse");
				
			} else {
				// link to infoflora ?
				Taxon taxPhylum = taxClass.getAncestor(TaxonRank.PHYLUM);
				if (setPhylumInInfoFlora.contains(taxPhylum.getName())) {
					addLink(par, taxon, getInfoFloraUrl(taxon), "InfoFlora", "infoflora.ch");
					
				// link to mycoDB ?
				} else if (setPhylumInMycoDb.contains(taxPhylum.getName())) {
					addLink(par, taxon, getMycoDbUrl(taxon), "MycoDB", "mycodb.fr");
				}
			}
		}
	}
	
	/**
	 * Adds a possible external link for the specified taxon.
	 * 
	 * @param par     the html element to add to
	 * @param taxon   the taxon
	 * @param url     the external URL (may be null)
	 * @param text    the link text
	 * @param tooltip the link tooltip text
	 */
	private void addLink(HtmlComposite par, Taxon taxon, String url, String text, String tooltip) {
		if (url != null) {
			par.addText(" | ");
			par.addLinkExternal(url, taxon.getNameFr() + " chez " + tooltip, text);
		}
	}

	/**
	 * Get the URL for the specified taxon on http://galerie-insecte.org.
	 * Returns null if the taxon is not a species, or if its class is
	 * not in that gallery.
	 * 
	 * @param taxon  the taxon
	 * @return  the specie's URL, or null if not in gallery
	 */
	protected String getGalerieInsecteUrl(Taxon taxon) {
		String url = "http://galerie-insecte.org/galerie/" + taxon.getName().replace(' ', '_') + ".html";
		return url;
	}
	

	/**
	 * Get the URL for the specified taxon on http://www.pyrgus.de,
	 * a Lepidoptera reference site.
	 * Returns null if the taxon is not a species, or if its order is
	 * not in that gallery.
	 * 
	 * @param taxon  the taxon
	 * @return the specie's URL, or null if not in gallery
	 */
	protected String getPyrgusUrl(Taxon taxon) {
		String url = null;
		Taxon taxOrder = taxon.getAncestor(TaxonRank.ORDER);
		if (taxOrder != null && "Lepidoptera".equals(taxOrder.getName())) {
			String sUrlName = taxon.getName().replace(' ', '_');
			// ex: http://www.pyrgus.de/Vanessa_atalanta_en.html
			url = "http://www.pyrgus.de/" + sUrlName + "_en.html";
		}
		return url;
	}
	
	protected String getBritishBugsUrl(Taxon taxon) {
		//https://www.britishbugs.org.uk/homoptera/Cercopidae/Cercopis_vulnerata.html
		//https://www.britishbugs.org.uk/heteroptera/Pentatomidae/aelia_acuminata.html
		String url = null;
		Taxon taxOrder = taxon.getAncestor(TaxonRank.ORDER);
		if (taxOrder != null && "Hemiptera".equals(taxOrder.getName())) {
			Taxon taxFamily = taxon.getAncestor(TaxonRank.FAMILY);
			if (setFamilyInHomoptera.contains(taxFamily.getName())) {
				// Homoptera
				String sUrlName = taxon.getName().replace(' ', '_');
				url = "https://www.britishbugs.org.uk/homoptera/" + taxFamily.getName() + "/" + sUrlName + ".html";
			} else {
				// Heteroptera
				String sUrlName = taxon.getName().replace(' ', '_').toLowerCase();
				url = "https://www.britishbugs.org.uk/heteroptera/" + taxFamily.getName() + "/" + sUrlName + ".html";
			}
		}
		return url;
	}
	
	/**
	 * Get the URL for the specified taxon on http://www.antwiki.org,
	 * a Formicidae reference site.
	 * Works for species and genera.
	 * 
	 * @param taxon  the taxon
	 * @return the taxon's URL
	 */
	protected String getAntWikiUrl(Taxon taxon) {
		String url = null;
		Taxon taxFamily = taxon.getAncestor(TaxonRank.FAMILY);
		// http://www.antwiki.org/wiki/Formica_rufa
		if ("Formicidae".equals(taxFamily.getName())) {
			String sUrlName = taxon.getName().replace(' ', '_');
			url = "http://www.antwiki.org/wiki/" + sUrlName;
		}
		return url;
	}
	
	/**
	 * Get the URL for the specified taxon on http://wiki.arages.de,
	 * a spider reference site.
	 * Returns null if the taxon is not a species, or if its class is
	 * not in that gallery.
	 * 
	 * @param taxon  the taxon
	 * @return the specie's URL, or null if not in gallery
	 */
	protected String getAragesUrl(Taxon taxon) {
		String url = null;
		String sUrlName = taxon.getName().replace(' ', '_');
		// ex: https://wiki.arages.de/index.php?title=Callobius_claustrarius
		url = "https://wiki.arages.de/index.php?title=" + sUrlName;
		return url;
	}

	/**
	 * Get the URL for the specified taxon on http://www.vogelwarte.ch.
	 * Returns null if the taxon is not a species, or if its class is
	 * not in that gallery.
	 * 
	 * @param taxon  the taxon
	 * @return the specie's URL, or null if not in gallery
	 */
	protected String getVogelwarteUrl(Taxon taxon) {
		String url = null;
		String sFrenchName = taxon.getNameFr().toLowerCase();
		sFrenchName = sFrenchName.replace(' ', '-');
		url = "http://www.vogelwarte.ch/fr/oiseaux/les-oiseaux-de-suisse/" + sFrenchName;
		return url;
	}
	
	protected String getInfoFloraUrl(Taxon taxon) {
		String url = null;
		String sUrlName = taxon.getName().toLowerCase().replace(' ', '-');
		// ex: https://www.infoflora.ch/fr/flore/hieracium-pilosella.html
		url = "https://www.infoflora.ch/fr/flore/" + sUrlName + ".html";
		return url;
	}
	
	protected String getMycoDbUrl(Taxon taxon) {
		String url = null;
		String sUrlName = taxon.getName().replace(" ", "&espece=");
		// ex: https://www.mycodb.fr/fiche.php?genre=Lycoperdon&espece=marginatum
		url = "https://www.mycodb.fr/fiche.php?genre=" + sUrlName;
		return url;
	}
	
	private String getWikipediaUrl(Taxon taxon) {
		String url = "http://fr.wikipedia.org/wiki/" + taxon.getName();
		return url;
	}
	
	private String getWikispeciesUrl(Taxon taxon) {
		String url = "http://species.wikimedia.org/wiki/" + taxon.getName();
		return url;
	}
}
