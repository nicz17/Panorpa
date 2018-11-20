package controller.export;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import model.Expedition;
import model.HerbierPic;
import model.NamedValue;
import model.Taxon;
import model.TaxonRank;

import common.base.Logger;
import common.io.HtmlComposite;

import controller.Controller;
import controller.DataAccess;
import controller.ExpeditionManager;
import controller.PictureCache;
import controller.TaxonCache;

/**
 * Subclass of Exporter to generate the home page of the website.
 * 
 * @author nicz
 *
 */
public class HomePageExporter extends BaseExporter {
	
	
	private static final Logger log = new Logger("HomePageExporter", true);
	
	private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.FRANCE);
	
	private static final int nSamplePics = 8;
	private static final int nLatestSpecies = 8;
	private static final int nLatestLocations = 6;

	public HomePageExporter() {
		
	}
	
	/**
	 * Creates the website home page.
	 */
	public void export() {
		HtmlPage page = new HtmlPage("Photos de nature");
		HtmlComposite main = page.getMainDiv();
		HtmlComposite table = main.addFillTable(2, "1420px");
		HtmlComposite tdLeft  = table.addTableData();
		HtmlComposite tdRight = table.addTableData();
		
		tdLeft.addTitle(1, "Photos de nature");
		
		// Sample pictures
		HtmlComposite divSamplePics = addBoxDiv(tdLeft, "Quelques photos", "myBox myBox-wide");
		HtmlComposite tablePics = divSamplePics.addFillTable(nColumns);
		tablePics.setCssClass("table-thumbs");
		
		Vector<HerbierPic> vecSamplePics = getSamplePics();		
		for (HerbierPic hpic : vecSamplePics) {
			String name = hpic.getName();
			HtmlComposite td = tablePics.addTableData();
			String picFile = getTaxonHtmlFileName(hpic.getTaxon());
			String picAnchor = "#" + hpic.getFileName().replace(".jpg", "");
			HtmlComposite link = td.addLink("pages/" + picFile + picAnchor, getTooltiptext(hpic.getTaxon()));
			link.addImage("thumbs/" + hpic.getFileName(), name);
		}
		
		// Categories
		HtmlComposite divCategories = addBoxDiv(tdLeft, "Quelques catégories", "myBox myBox-wide");
		HtmlComposite tableCat = divCategories.addFillTable(5);
		
		HtmlComposite ul = tableCat.addTableData().addList();
		ul.addListItem().addLink("Pteridophyta.html", "Fougères", "Fougères");
		ul.addListItem().addLink("Pinophyta.html", "Conifères", "Conifères");
		ul.addListItem().addLink("Magnoliophyta.html#Liliopsida", "Monocotylédones", "Monocots");
		ul.addListItem().addLink("Magnoliophyta.html#Magnoliopsida", "Dicotylédones", "Dicots");
		
		ul = tableCat.addTableData().addList();
		ul.addListItem().addLink("Malpighiales.html#Euphorbiaceae", "Euphorbes", "Euphorbes");
		ul.addListItem().addLink("Saxifragales.html#Saxifragaceae", "Saxifrages", "Saxifrages");
		ul.addListItem().addLink("Lamiales.html#Lamiaceae", "Lamiacées", "Lamiacées");
		ul.addListItem().addLink("Asterales.html#Asteraceae", "Astéracées", "Astéracées");
		
		ul = tableCat.addTableData().addList();
		ul.addListItem().addLink("Chordata.html#Aves", "Oiseaux", "Oiseaux");
		ul.addListItem().addLink("Chordata.html#Mammalia", "Mammifères", "Mammifères");
		ul.addListItem().addLink("Araneae.html", "Araignées", "Araignées");
		ul.addListItem().addLink("Opiliones.html", "Opilions ou faucheux", "Opilions");
		
		ul = tableCat.addTableData().addList();
		ul.addListItem().addLink("Arthropoda.html#Insecta", "Insectes", "Insectes");
		ul.addListItem().addLink("Diptera.html", "Mouches, syrphes, tipules", "Diptères");
		ul.addListItem().addLink("Hymenoptera.html", "Abeilles, fourmis, guêpes", "Hyménoptères");
		ul.addListItem().addLink("Lepidoptera.html", "Papillons", "Papillons");
		
		ul = tableCat.addTableData().addList();
		ul.addListItem().addLink("Odonata.html", "Libellules et demoiselles", "Libellules");
		ul.addListItem().addLink("Coleoptera.html", "Coléoptères", "Coléoptères");
		ul.addListItem().addLink("Hemiptera.html", "Punaises", "Punaises");
		ul.addListItem().addLink("Squamata.html", "Reptiles", "Reptiles");
		
		// About
		HtmlComposite divAbout = addBoxDiv(tdLeft, "A propos de cette galerie");
		
		divAbout.addPar("Cette galerie de photos de nature me sert d'aide-mémoire pour retrouver les noms " +
				"des plantes et insectes que je croise en montagne, en voyage ou autour de chez moi.");
		
		Map<TaxonRank, Integer> mapRankStats = getRankStats();
		HtmlComposite stats = divAbout.addPar("C'est aussi une collection de taxons qui compte actuellement <b>");
		stats.addText(PictureCache.getInstance().size() + "</b> photos dans <b>");
		stats.addText(mapRankStats.get(TaxonRank.SPECIES) + "</b> espèces, <b>");
		stats.addText(mapRankStats.get(TaxonRank.GENUS) + "</b> genres et <b>");
		stats.addText(mapRankStats.get(TaxonRank.FAMILY) + "</b> familles.");
		
		// Latest species
		HtmlComposite divSpecies = addBoxDiv(tdRight, "Dernières espèces");
		Vector<Taxon> vecSpeciesRaw = Controller.getInstance().getLatestSpecies(nLatestSpecies + 4);
		Vector<Taxon> vecSpecies = new Vector<>();
		for (Taxon species : vecSpeciesRaw) {
			// get from cache to have associated pics etc.
			Taxon speciesCached = TaxonCache.getInstance().getTaxon(species.getIdx());
			if (speciesCached.getTypicalPic() != null) {
				vecSpecies.add(speciesCached);
			}
		}
		
		// sort by latest pic date
		Collections.sort(vecSpecies, new Comparator<Taxon>() {
			@Override
			public int compare(Taxon tax1, Taxon tax2) {
				return tax2.getTypicalPic().getShotAt().compareTo(tax1.getTypicalPic().getShotAt());
			}
		});
		
		// remove tails
		while (vecSpecies.size() > nLatestSpecies) {
			vecSpecies.remove(vecSpecies.size() - 1);
		}
		
		ul = divSpecies.addList();
		for (Taxon species : vecSpecies) {
			HerbierPic pic = species.getTypicalPic();
			if (pic != null) {
				//String picFile = pic.getFileName().replace(".jpg", ".html");
				String picFile = getTaxonHtmlFileName(species);
				HtmlComposite li = ul.addListItem();
				li.addLink("pages/" + picFile, getTooltiptext(species), species.getName());
				li.addText(" <font color='gray'>" + dateFormat.format(pic.getShotAt()) + "</font>");
			}
		}
		
		// Latest expeditions
		HtmlComposite divExpeditions = addBoxDiv(tdRight, "Expéditions récentes");
		List<Expedition> vecExpeditions = ExpeditionManager.getInstance().getRecentExpeditions(nLatestLocations);
		ul = divExpeditions.addList();
		for (Expedition exp : vecExpeditions) {
			HtmlComposite li = ul.addListItem();
			String filename = "lieu" + exp.getLocation().getIdx() + ".html";
			li.addLink(filename, exp.getLocation().getName(), exp.getLocation().getName());
			li.addText(" <font color='gray'>" + dateFormat.format(exp.getDate()) + "</font>");
		}
		
		createSearchForm(tdRight);
		
		// Photo hardware
		HtmlComposite divMatos = addBoxDiv(tdRight, "Matériel photo");
		ul = divMatos.addList();
		ul.addListItem().addLinkExternal("https://fr.wikipedia.org/wiki/Nikon_D300", "Nikon D300");
		HtmlComposite li = ul.addListItem();
		li.addLinkExternal("https://fr.wikipedia.org/wiki/Nikon_D800", "Nikon D800");
		li.addText(" (depuis novembre 2017)");
		ul.addListItem().addText("AF-S Micro Nikkor 105mm 1:2.8");
		ul.addListItem().addText("AF-S Nikkor 80-400mm 1:4.5-5.6");
		//ul.addListItem().addText("Sony Nex 5T");
		
		// External links
		HtmlComposite divLinks = addBoxDiv(tdRight, "Liens externes");
		ul = divLinks.addList();
		ul.addListItem().addLinkExternal("https://www.inaturalist.org/observations/nicz", "iNaturalist");
		ul.addListItem().addLinkExternal("http://www.insecte.org/forum/", "Le monde des insectes");
		ul.addListItem().addLinkExternal("http://www.quelestcetanimal.com/", "Quel est cet animal ?");
		ul.addListItem().addLinkExternal("http://www.visoflora.com/", "Visoflora");
		ul.addListItem().addLinkExternal("https://www.infoflora.ch/fr/", "Infoflora");
		
		page.saveAs(htmlPath + "index.html");
	}
	
	/**
	 * Gets a small random list of pictures with a 5-star rating.
	 * 
	 * @return a randomized list of 5-star pictures.
	 */
	private Vector<HerbierPic> getSamplePics() {
		Vector<HerbierPic> vecBestPics = 
				DataAccess.getInstance().getHerbierPics("WHERE picRating = 5", null, null);
		Collections.shuffle(vecBestPics);
		Vector<HerbierPic> vecSamplePics = new Vector<>(nSamplePics);
		
		int nPics = 0;
		while (nPics < vecBestPics.size() && nPics < nSamplePics) {
			HerbierPic pic = PictureCache.getInstance().getPicture(vecBestPics.get(nPics).getIdx());
			vecSamplePics.add(pic);
			nPics++;
		}
		return vecSamplePics;
	}
	
	/**
	 * Adds a search form, with redirection to dedicated search page.
	 * @param parent  the parent html composite
	 */
	private void createSearchForm(HtmlComposite parent) {
		HtmlComposite divSearch = addBoxDiv(parent, "Chercher");
		HtmlComposite form = divSearch.addForm("get", "search.html");
		form.addInput("text", "search");
		form.addButton("Chercher");
	}

	private Map<TaxonRank, Integer> getRankStats() {
		Vector<NamedValue> vecDbValues = DataAccess.getInstance().getGroupedCount("Taxon", "taxRank", null);
		log.info("Got " + vecDbValues.size() + " rank counts");
		
		Map<TaxonRank, Integer> mapRankStats = new HashMap<>();
		for (NamedValue nv : vecDbValues) {
			TaxonRank rank = TaxonRank.valueOf(nv.getName());
			mapRankStats.put(rank, nv.getValue());
		}
		return mapRankStats;
	}


}
