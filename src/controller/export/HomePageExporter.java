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
import common.html.HtmlTag;
import common.html.HtmlTagFactory;
import common.html.ListHtmlTag;
import common.html.TableHtmlTag;
import controller.Controller;
import controller.DataAccess;
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
		PanorpaHtmlPage page = new PanorpaHtmlPage("Photos de nature", htmlPath + "index.html");
		TableHtmlTag table = page.addFillTable(2, "1420px");
		HtmlTag tdLeft  = table.addCell();
		HtmlTag tdRight = table.addCell();
		
		tdLeft.addTitle(1, "Photos de nature");
		
		// Sample pictures
		HtmlTag divSamplePics = HtmlTagFactory.blueBox("Quelques photos", "myBox myBox-wide");
		tdLeft.addTag(divSamplePics);
		TableHtmlTag tablePics = new TableHtmlTag(nColumns, true);
		tablePics.addAttribute("width", "100%");
		divSamplePics.addTag(tablePics);
		tablePics.setClass("table-thumbs");
		
		Vector<HerbierPic> vecSamplePics = getSamplePics();		
		for (HerbierPic hpic : vecSamplePics) {
			String name = hpic.getName();
			HtmlTag td = tablePics.addCell();
			String picFile = getTaxonHtmlFileName(hpic.getTaxon());
			String picAnchor = "#" + hpic.getFileName().replace(".jpg", "");
			HtmlTag link = HtmlTagFactory.imageLink("pages/" + picFile + picAnchor, getTooltiptext(hpic.getTaxon()),
					"thumbs/" + hpic.getFileName(), name);
			td.addTag(link);
		}
		
		// Categories
		HtmlTag divCategories = HtmlTagFactory.blueBox("Quelques catégories", "myBox myBox-wide");
		tdLeft.addTag(divCategories);
		TableHtmlTag tableCat = new TableHtmlTag(5, true);
		tableCat.addAttribute("width", "100%");
		divCategories.addTag(tableCat);
		
		ListHtmlTag ul = new ListHtmlTag();
		tableCat.addCell(ul);
		ul.addItem(HtmlTagFactory.link("Pteridophyta.html", "Fougères", "Fougères"));
		ul.addItem(HtmlTagFactory.link("Pinophyta.html", "Conifères", "Conifères"));
		ul.addItem(HtmlTagFactory.link("Magnoliophyta.html#Liliopsida", "Monocots", "Monocotylédones"));
		ul.addItem(HtmlTagFactory.link("Magnoliophyta.html#Magnoliopsida", "Dicots", "Dicotylédones"));
		
		ul = new ListHtmlTag();
		tableCat.addCell(ul);
		ul.addItem(HtmlTagFactory.link("Malpighiales.html#Euphorbiaceae", "Euphorbes", "Euphorbes"));
		ul.addItem(HtmlTagFactory.link("Saxifragales.html#Saxifragaceae", "Saxifrages", "Saxifrages"));
		ul.addItem(HtmlTagFactory.link("Lamiales.html#Lamiaceae", "Lamiacées", "Lamiacées"));
		ul.addItem(HtmlTagFactory.link("Asterales.html#Asteraceae", "Astéracées", "Astéracées"));
		
		ul = new ListHtmlTag();
		tableCat.addCell(ul);
		ul.addItem(HtmlTagFactory.link("Chordata.html#Aves", "Oiseaux", "Oiseaux"));
		ul.addItem(HtmlTagFactory.link("Chordata.html#Mammalia", "Mammifères", "Mammifères"));
		ul.addItem(HtmlTagFactory.link("Araneae.html", "Araignées", "Araignées"));
		ul.addItem(HtmlTagFactory.link("Opiliones.html", "Opilions", "Opilions ou faucheux"));
		
		ul = new ListHtmlTag();
		tableCat.addCell(ul);
		ul.addItem(HtmlTagFactory.link("Arthropoda.html#Insecta", "Insectes", "Insectes"));
		ul.addItem(HtmlTagFactory.link("Diptera.html", "Diptères", "Mouches, syrphes, tipules"));
		ul.addItem(HtmlTagFactory.link("Hymenoptera.html", "Hyménoptères", "Abeilles, fourmis, guêpes"));
		ul.addItem(HtmlTagFactory.link("Lepidoptera.html", "Papillons", "Papillons"));
		
		ul = new ListHtmlTag();
		tableCat.addCell(ul);
		ul.addItem(HtmlTagFactory.link("Odonata.html", "Libellules", "Libellules et demoiselles"));
		ul.addItem(HtmlTagFactory.link("Coleoptera.html", "Coléoptères", "Coléoptères"));
		ul.addItem(HtmlTagFactory.link("Hemiptera.html", "Punaises", "Punaises"));
		ul.addItem(HtmlTagFactory.link("Squamata.html", "Reptiles", "Reptiles"));
		
		// About
		HtmlTag divAbout = HtmlTagFactory.blueBox("A propos de cette galerie");
		tdLeft.addTag(divAbout);
		
		divAbout.addParagraph("Cette galerie de photos de nature me sert d'aide-mémoire pour retrouver les noms " +
				"des plantes et insectes que je croise en montagne, en voyage ou autour de chez moi.");
		
		Map<TaxonRank, Integer> mapRankStats = getRankStats();
		String sStats = "C'est aussi une collection de taxons qui compte actuellement <b>";
		sStats += PictureCache.getInstance().size() + "</b> photos dans <b>";
		sStats += mapRankStats.get(TaxonRank.SPECIES) + "</b> espèces, <b>";
		sStats += mapRankStats.get(TaxonRank.GENUS) + "</b> genres et <b>";
		sStats += mapRankStats.get(TaxonRank.FAMILY) + "</b> familles.";
		divAbout.addParagraph(sStats);
		
		// Latest species
		HtmlTag divSpecies = tdRight.addBox("Dernières espèces");
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
				HtmlTag li = ul.addItem();
				li.addLink("pages/" + picFile, species.getNameFr(), getTooltiptext(species), false);
				li.addGrayFont(" " + dateFormat.format(pic.getShotAt()));
			}
		}
		
		// Latest excursions
		HtmlTag divExpeditions = tdRight.addBox("Excursions récentes");
		List<Expedition> vecExpeditions = Controller.getInstance().getRecentExpeditions(nLatestLocations);
		ul = divExpeditions.addList();
		for (Expedition exp : vecExpeditions) {
			HtmlTag li = ul.addItem();
			String url = "excursion" + exp.getIdx() + ".html";
			li.addLink(url, exp.getTitle(), exp.getTitle(), false);
			li.addGrayFont(" " + dateFormat.format(exp.getDateFrom()));
		}
		
		// TODO restore createSearchForm(tdRight);
		
		// Photo hardware
		HtmlTag divMatos = tdRight.addBox("Matériel photo");
		ul = divMatos.addList();
		ul.addItem().addLink("https://fr.wikipedia.org/wiki/Nikon_D300", "Nikon D300", "Wikipedia : Nikon D300", true);
		HtmlTag li = ul.addItem();
		li.addLink("https://fr.wikipedia.org/wiki/Nikon_D800", "Nikon D800", "Wikipedia : Nikon D800", true);
		li.addGrayFont(" (depuis novembre 2017)");
		ul.addItem("AF-S Micro Nikkor 105mm 1:2.8");
		ul.addItem("AF-S Nikkor 80-400mm 1:4.5-5.6");
		//ul.addListItem().addText("Sony Nex 5T");
		
		// External links
		HtmlTag divLinks = tdRight.addBox("Liens externes");
		ul = divLinks.addList();
		ul.addItem().addLink("https://www.inaturalist.org/observations/nicz", "iNaturalist",
				"Mes observations sur iNaturalist", true);
		ul.addItem().addLink("http://www.insecte.org/forum/", "Le monde des insectes",
				"Le monde des insectes - forum", true);
		ul.addItem().addLink("http://www.quelestcetanimal.com/", "Quel est cet animal ?",
				"Quel est cet animal ?", true);
		//ul.addItem().addLink("http://www.visoflora.com/", "Visoflora");
		ul.addItem().addLink("https://www.infoflora.ch/fr/", "Infoflora", "Flore Suisse", true);
		HtmlTag link = ul.addItem().addLink("https://noc.social/@nicz", "Mastodon", "Mastodon @nicz@noc.social", true);
		link.addAttribute("rel", "me");
		
		page.save();
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
	private void createSearchForm(HtmlTag parent) {
		HtmlTag divSearch = parent.addBox("Chercher");
		// TODO search form
		//HtmlTag form = divSearch.addForm("get", "search.html");
		//form.addInput("text", "search");
		//form.addButton("Chercher");
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
