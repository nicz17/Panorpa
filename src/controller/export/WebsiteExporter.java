package controller.export;

import java.io.FileWriter;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import model.HerbierPic;
import model.Location;
import model.Taxon;
import model.TaxonRank;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import common.base.Logger;
import common.io.HtmlComposite;
import common.io.SpecialChars;

import controller.Controller;
import controller.TaxonCache;

/**
 * Exports the picture collection to html pages.
 * 
 * @author nicz
 *
 */
public class WebsiteExporter extends BaseExporter {

	private static final Logger log = new Logger("WebsiteExporter", true);
	
	private HtmlPage htmlPage;
	private HtmlComposite menuDiv;
	protected TaxonUrlProvider taxonUrlProvider;
	
	public WebsiteExporter() {
		taxonUrlProvider = new TaxonUrlProvider();
	}
	
	public void export(Set<Taxon> taxa) {
		log.info("Exporting " + taxa.size() + " top-level taxa");
		
		if (taxa == null || taxa.isEmpty()) {
			log.warn("Nothing to export, aborting");
			return;
		}
		
		htmlPage = new HtmlPage("Photos de nature");
		htmlPage.addCss(Controller.appPath + "style.css");
		
		HtmlComposite main = htmlPage.getMainDiv();
		main.addTitle(1, "Classification");
		
		menuDiv = htmlPage.getMenuDiv();
		menuDiv.addTitle(1, "Classification");
		
		for (Taxon taxon : taxa) {
			if (TaxonRank.KINGDOM.equals(taxon.getRank())) {
				
				// create menu
				addMenuLink(taxon);
				for (Taxon child : taxon.getChildren()) {
					addMenuLink(child);
				}
				
				exportTaxon(taxon, main);
			} else {
				log.warn("Skipping non-kingdom " + taxon);
			}
		}
		
		createLinksPage();
		
		htmlPage.saveAs(htmlPath + "classification.html");
		
		exportJsonTree();
	}
	
	private void exportTaxon(Taxon taxon, HtmlComposite html) {
		TaxonRank rank = taxon.getRank();
		TaxonRank rankChild = rank.getChildRank();
		String name = taxon.getName();
		
		html.addAnchor(name);
		html.addTitle(2, rank.getGuiName() + " " + taxon.getName() + taxonNameSeparator + taxon.getNameFr());
		
		int nPics = taxon.getPicsCascade().size();
		if (nPics > 0) {
			html.addSpan("pics-count", String.valueOf(nPics) + " photo" + (nPics == 1 ? "" : "s"));

			if (TaxonRank.ORDER.equals(rankChild) || TaxonRank.PHYLUM.equals(rankChild)) {
				HtmlComposite table = html.addFillTable(nColumns);
				table.setCssClass("table-thumbs");

				for (Taxon child : taxon.getChildren()) {
					nPics = child.getPicsCascade().size();
					if (nPics > 0) {
						HtmlComposite td = table.addTableData();
						HerbierPic hpic = child.getTypicalPic();

						// image with link to order page
						String orderName = child.getName();
						td.addAnchor(orderName);
						HtmlComposite link = td.addLink(orderName + ".html", "Aller à la page des " + orderName);
						link.addImage("thumbs/" + hpic.getFileName(), hpic.getName());
						link.addText("<br>" + orderName + " (" + nPics + ")");

						if (TaxonRank.PHYLUM.equals(rankChild)) {
							exportPhylum(child);
						} else if (TaxonRank.ORDER.equals(rankChild)) {
							exportOrder(child);
						}
					}
				}
			} else {
				for (Taxon child : taxon.getChildren()) {
					exportTaxon(child, html);
				}
			}
		}
	}
	
	private void exportPhylum(Taxon phylum) {
		String name = phylum.getName();
		
		HtmlPage orderPage = new HtmlPage("Nature - " + name);
		HtmlComposite main = orderPage.getMainDiv();
		HtmlComposite menu = orderPage.getMenuDiv();
		
		final TreeSet<HerbierPic> tsOrderPics = phylum.getPicsCascade();
		int nPics = tsOrderPics.size();

		main.addTitle(1, "Phylum " + name + taxonNameSeparator + phylum.getNameFr());
		main.addSpan("pics-count", String.valueOf(nPics) + " photo" + (nPics == 1 ? "" : "s"));
		
		menu.addTitle(1, "Classification");
		writeTaxonHierarchy(menu, phylum);
		menu.addTitle(2, "Classes");
		
		for (Taxon child : phylum.getChildren()) {
			addRankIcon(menu, child.getRank());
			menu.addLink("#" + child.getName(), "Aller aux " + child.getName(), child.getName());
			menu.addBr();
			
			exportTaxon(child, main);
		}
		
		main.addCenter().addLink("classification.html#" + name, "Retour").addImage("pages/home.gif", "Retour");
		
		orderPage.saveAs(htmlPath + name + ".html");
	}
	
	private void exportOrder(Taxon order) {
		String name = order.getName();
		
		HtmlPage orderPage = new HtmlPage("Nature - " + name);
		HtmlComposite main = orderPage.getMainDiv();
		HtmlComposite menu = orderPage.getMenuDiv();
		
		final TreeSet<HerbierPic> tsOrderPics = order.getPicsCascade();
		int nPics = tsOrderPics.size();

		main.addTitle(1, "Ordre des " + name + taxonNameSeparator + order.getNameFr());
		main.addSpan("pics-count", String.valueOf(nPics) + " photo" + (nPics == 1 ? "" : "s"));
		
		menu.addTitle(1, "Classification");
		writeTaxonHierarchy(menu, order);
		menu.addTitle(2, "Familles");
		
		for (Taxon family : order.getChildren()) {
			final TreeSet<HerbierPic> tsFamilyPics = family.getPicsCascade();
			nPics = tsFamilyPics.size();
			
			TreeSet<Taxon> tsTaxa = new TreeSet<>();
			
			main.addAnchor(family.getName());
			String title = family.getName();
			if (!name.equals(family.getNameFr())) {
				title += taxonNameSeparator + family.getNameFr();
			}
			main.addTitle(2, "Famille des " + title);
			main.addSpan("pics-count", String.valueOf(nPics) + " photo" + (nPics == 1 ? "" : "s"));
			
			addRankIcon(menu, TaxonRank.FAMILY);
			menu.addLink("#" + family.getName(), "Aller aux " + family.getName(), family.getName());
			menu.addBr();
			
			HtmlComposite table = main.addFillTable(nColumns);
			table.setCssClass("table-thumbs");
			
			// export each genus and species
			for (Taxon genus : family.getChildren()) {
				for (Taxon species : genus.getChildren()) {
					HerbierPic pic = species.getBestPic();
					if (pic != null) {
						exportPicture(pic, table, false, species.getPics().size());
						tsTaxa.add(species);
					}
				}
				if (!genus.getPics().isEmpty()) {
					HerbierPic pic = genus.getBestPic();
					exportPicture(pic, table, false, genus.getPics().size());
					tsTaxa.add(genus);
				}
			}
			if (!family.getPics().isEmpty()) {
				HerbierPic pic = family.getBestPic();
				exportPicture(pic, table, false, family.getPics().size());
				tsTaxa.add(family);
			}
			
			for (Taxon taxon : tsTaxa) {
				Taxon prevTaxon = tsTaxa.lower(taxon);
				if (prevTaxon == null) prevTaxon = tsTaxa.last();
				
				Taxon nextTaxon = tsTaxa.higher(taxon);
				if (nextTaxon == null) nextTaxon = tsTaxa.first();
				
				exportTaxon(taxon, prevTaxon, nextTaxon);
			}
		}
		
		// Go back to phylum
		Taxon phylum = order.getAncestor(TaxonRank.PHYLUM);
		main.addCenter().addLink(phylum.getName() + ".html#" + name, "Retour").addImage("pages/home.gif", "Retour");
		
		orderPage.saveAs(htmlPath + name + ".html");
	}
	
	/*
	 * Creates a HTML page for the specified image.
	 * The page contains a medium-size image, image details,
	 * and a classification table.
	 * @param hpic  the picture to export
	 *
	private void createPicturePage(HerbierPic hpic, final HerbierPic hpicPrev, final HerbierPic hpicNext) {
		String name = hpic.getName();
		String htmlFile = hpic.getFileName().replace(".jpg", ".html");
		
		HtmlPage picPage = new HtmlPage("Nature - " + name, 1);
		HtmlComposite main = picPage.getMainDiv();
		main.addTitle(1, name);

		// image with link
		HtmlComposite link = main.addLink("../photos/" + hpic.getFileName(), name);
		link.addImage("../medium/" + hpic.getFileName(), name);
		
		// image details
		HtmlComposite divPicDesc = addBoxDiv(main, hpic.getFrenchName());
		
		String remarks = hpic.getRemarks();
		if (remarks != null && !remarks.isEmpty()) {
			remarks = replaceRemarkLinks(remarks);
			divPicDesc.addPar(remarks);
		}
		
		Location location = hpic.getLocation();
		HtmlComposite par = divPicDesc.addPar();
		if (location != null) {
			String filename = "../lieu" + location.getIdx() + ".html";
			par.addLink(filename, location.getName(), location.getName());
			int altitude = location.getAltitude();
			if (altitude >= 1000) {
				par.addText(" (" + altitude + "m)");
			}
			par.addText(", " + location.getRegion());
			if (!"Suisse".equals(location.getState())) {
				par.addText(", " + location.getState());
			}
			par.addText(", ");
		}
		
		par.addText(dateFormat.format(hpic.getShotAt()));
		//divPicDesc.addPar(dateFormat.format(hpic.getShotAt()));

		par = divPicDesc.addPar();
		par.addText("Pages ");
		par.addLinkExternal(getWikipediaUrl(hpic),   "Wikipedia",   "Wikipedia");
		par.addText(" | ");
		par.addLinkExternal(getWikispeciesUrl(hpic), "Wikispecies", "Wikispecies");
		
		// link to galerie-insecte ?
		String urlGalerieInsecte = getGalerieInsecteUrl(hpic.getTaxon());
		if (urlGalerieInsecte != null) {
			par.addText(" | ");
			par.addLinkExternal(urlGalerieInsecte, "Galerie insectes", "Galerie insecte");
		}
		
		// classification
		HtmlComposite divClassif = addBoxDiv(main, "Classification");
		HtmlComposite table = divClassif.addFillTable(3, "500px");
		
		Taxon taxon = hpic.getTaxon();
		Vector<Taxon> vecTaxa = new Vector<>();
		vecTaxa.add(taxon);
		Taxon parent = taxon.getParent();
		while (parent != null) {
			vecTaxa.add(0, parent);
			parent = parent.getParent();
		}
		
		for (Taxon t : vecTaxa) {
			HtmlComposite td = table.addTableData();
			TaxonRank rank = t.getRank();
			addRankIcon(td, rank);
			td.addText(rank.getGuiName());
			//table.addTableData(t.getName());
			addClassificationTableData(table, t);
			table.addTableData(t.getNameFr());
		}
		
		// navigation links
		Taxon family = hpic.getTaxon().getAncestor(TaxonRank.FAMILY);
		Taxon order  = family.getParent();
		Taxon taxClass = order.getParent();
		Taxon phylum = taxClass.getParent();
		Taxon kingdom = phylum.getParent();
		
		HtmlComposite tableNav = main.addFillTable(3, "500px");
		tableNav.setCssClass("table-nav");
		tableNav.addTableData().addLink(hpicPrev.getFileName().replace(".jpg", ".html"), hpicPrev.getName())
			.addImage("prev.gif", "Précédante");
		tableNav.addTableData().addLink("../" + order.getName() + ".html#" + family.getName(), 
				"Retour à la page des " + order.getName())
			.addImage("home.gif", "Retour");
		tableNav.addTableData().addLink(hpicNext.getFileName().replace(".jpg", ".html"), hpicNext.getName())
			.addImage("next.gif", "Suivante");
		
		// menu items
		HtmlComposite menuDiv = picPage.getMenuDiv();
		menuDiv.addTitle(1, "Classification");
		
		addRankIcon(menuDiv, TaxonRank.KINGDOM);
		menuDiv.addLink("../classification.html#" + kingdom.getName(), kingdom.getNameFr(), kingdom.getName());
		menuDiv.addBr();
		
		addRankIcon(menuDiv, TaxonRank.CLASS);
		menuDiv.addLink("../" + phylum.getName() + ".html#" + taxClass.getName(), taxClass.getNameFr(), taxClass.getName());
		menuDiv.addBr();
		
		addRankIcon(menuDiv, TaxonRank.ORDER);
		menuDiv.addLink("../" + order.getName() + ".html", order.getNameFr(), order.getName());
		menuDiv.addBr();
		
		addRankIcon(menuDiv, TaxonRank.FAMILY);
		menuDiv.addLink("../" + order.getName() + ".html#" + family.getName(), family.getNameFr(), family.getName());
			
		if (TaxonRank.FAMILY != taxon.getRank()) {
			menuDiv.addBr();
			addRankIcon(menuDiv, taxon.getRank());
			menuDiv.addText(name);
		}
		
		picPage.saveAs(htmlPath + "pages/" + htmlFile);
	}
	*/
	
	protected void addClassificationTableData(HtmlComposite table, Taxon taxon) {
		switch(taxon.getRank()) {
		case KINGDOM:
			table.addTableData().addLink("../classification.html#" + taxon.getName(), 
					"Aller aux " + taxon.getNameFr(), taxon.getName());
			break;
		case PHYLUM:
			table.addTableData().addLink("../" + taxon.getName() + ".html", 
					"Aller au phylum des " + taxon.getNameFr(), taxon.getName());
			break;
		case CLASS:
			Taxon phylum = taxon.getParent();
			table.addTableData().addLink("../" + phylum.getName() + ".html#" + taxon.getName(), 
					"Aller à la classe des " + taxon.getNameFr(), taxon.getName());
			break;
		case ORDER:
			table.addTableData().addLink("../" + taxon.getName() + ".html", 
					"Aller à l'ordre des " + taxon.getNameFr(), taxon.getName());
			break;
		case FAMILY:
			Taxon order = taxon.getParent();
			table.addTableData().addLink("../" + order.getName() + ".html#" + taxon.getName(), 
					"Aller à la famille des " + taxon.getNameFr(), taxon.getName());
			break;
		case GENUS:
		case SPECIES:
			table.addTableData("<i>" + taxon.getName() + "</i>");
			break;
		default:
			table.addTableData(taxon.getName());
			break;
		}
	}
	
	private void createLinksPage() {
		HtmlPage linksPage = new HtmlPage("Nature - Liens");
		linksPage.addCss("style.css");
		HtmlComposite main = linksPage.getMainDiv();
		
		main.addTitle(1, "Liens externes");
		
		// TODO method addExternalLinkToCheck(url, text) and check with WebProbe in ExternalLinkChecker singleton
		
		main.addTitle(2, "Insectes");
		HtmlComposite ul = main.addList();
		ul.addListItem().addLinkExternal("http://www.insecte.org/forum/", "Le monde des insectes - forum");
		ul.addListItem().addLinkExternal("http://www.galerie-insecte.org/galerie/fichier.php", 
				"Le monde des insectes - galerie");
		ul.addListItem().addLinkExternal("http://www.galerie-insecte.org/galerie/auteur.php?aut=6169", 
				"Le monde des insectes - mes photos");
		ul.addListItem().addLinkExternal("http://www.quelestcetanimal.com/", "Quel est cet animal ?");
		ul.addListItem().addLinkExternal("http://spipoll.snv.jussieu.fr/mkey/mkey-spipoll.html", 
				"SpiPoll - Suivi photographique des insectes pollinisateurs");
		ul.addListItem().addLinkExternal("http://lepus.unine.ch/carto/", "InfoFauna UniNe");
		ul.addListItem().addLinkExternal("http://www.lepido.ch/", "Papillons diurnes de Suisse");
		ul.addListItem().addLinkExternal("http://cle.fourmis.free.fr/castes-fourmis.html", "Fourmis de France");
		ul.addListItem().addLinkExternal("http://home.hccnet.nl/mp.van.veen/conopidae/ConGenera.html", "Clé des Conopidae");
		
		main.addTitle(2, "Plantes");
		ul = main.addList();
		ul.addListItem().addLinkExternal("http://www.visoflora.com/", "Visoflora");
		ul.addListItem().addLinkExternal("https://www.infoflora.ch/fr/", "Infoflora - flore de Suisse");
		ul.addListItem().addLinkExternal("http://www.tela-botanica.org/bdtfx-nn-60585-synthese", "Tela botanica");
		ul.addListItem().addLinkExternal("http://abiris.snv.jussieu.fr/flore/flore.php", 
				"Identification assistée par ordinateur");
		
		main.addTitle(2, "Autres");
		ul = main.addList();
		ul.addListItem().addLinkExternal("http://www.inaturalist.org/observations/nicz", "iNaturalist");
		ul.addListItem().addLinkExternal("http://www.salamandre.net/", "La Salamandre - la revue des curieux de nature");
		ul.addListItem().addLinkExternal("http://www.pronatura-vd.ch/nos_reserves", "Réserves Pro Natura Vaud");
		ul.addListItem().addLinkExternal("http://www.arocha.ch/fr/projects/aide-entretien-pps/", "A Rocha - entretien de prairies sèches");
		ul.addListItem().addLinkExternal("http://www.ornitho.ch/index.php?m_id=1", "Plate-forme ornithologique suisse");
		ul.addListItem().addLinkExternal("https://www.thunderforest.com/", "Cartes par Thunderforest et OpenLayers");
		
		// Bibliographie
		main.addTitle(1, "Bibliographie");
		HtmlComposite biblio = main.addList();
		addBiblioRef(biblio, "P. Leraut, P. Blanchot", "Le guide entomologique", "Delachaux et Niestlé", "2012");
		addBiblioRef(biblio, "R. Dajoz", "Dictionnaire d'entomologie", "Lavoisier", "2010");
		addBiblioRef(biblio, "D. Martiré", "Guide des plus beaux coléoptères", "Belin", "2017");
		addBiblioRef(biblio, "K. Dijkstra", "Guide des libellules", "Delachaux et Niestlé", "2015");
		addBiblioRef(biblio, "T. Haahtela <i>et al</i>", "Guide photo des papillons d'Europe", "Delachaux et Niestlé", "2017");
		addBiblioRef(biblio, "R. Garrouste", "Hémiptères de France", "Delachaux et Niestlé", "2015");
		addBiblioRef(biblio, "A. Canard, C. Rollard", "A la découverte des araignées", "Dunod", "2015");
		addBiblioRef(biblio, "K. Lauber, G. Wagner, A. Gygax", "Flora Helvetica", "4e édition, Haupt", "2012");
		addBiblioRef(biblio, "E. Gerber, G. Kozlowski, A.-S. Mariéthoz", "La flore des Préalpes", "Rossolis", "2010");
		addBiblioRef(biblio, "F. Dupont, J.-L. Guignard", "Botanique, les familles de plantes", "15e édition, Elsevier Masson", "2012");
		addBiblioRef(biblio, "Collectif", "Les guides Salamandre", "Editions de la Salamandre, Neuchâtel", null);
		
		linksPage.saveAs(htmlPath + "liens.html");
	}
	
	private void addBiblioRef(HtmlComposite biblio, String sAuthors, String sTitle, String sEditor, String sYear) {
		String sRef = sAuthors + " : <b>" + sTitle + "</b>, " + sEditor;
		if (sYear != null && !sYear.isEmpty()) {
			sRef += ", <font color='grey'>" + sYear + "</font>";
		}
		biblio.addListItem().addText(sRef);
	}
	
	
	protected void addRankIcon(HtmlComposite html, TaxonRank rank) {
		html.addImage("rank" + rank.getOrder() + ".svg", rank.getGuiName(), null, rank.getGuiName());
		//html.addSvgSquare("16", rank.getColor(), rank.getGuiName());
	}
	
	/**
	 * Writes the hierarchy of the specified taxon 
	 * on one line on the specified html composite.
	 * @param html  the html composite to write to
	 * @param taxon the taxon
	 */
	private void writeTaxonHierarchy(HtmlComposite html, Taxon taxon) {
		if (taxon != null) {
			Vector<Taxon> vecTaxa = new Vector<>();
			vecTaxa.add(taxon);
			Taxon parent = taxon.getParent();
			while (parent != null) {
				vecTaxa.add(0, parent);
				parent = parent.getParent();
			}
			
			for (Taxon t : vecTaxa) {
				String name = t.getName();
				addRankIcon(html, t.getRank());
				html.addLink(getTaxonLink(t, null), "Retour aux " + name + " (" + t.getNameFr() + ")", name);
				//html.addLink("classification.html#" + name, "Retour aux " + name + " (" + t.getNameFr() + ")", name);
				html.addBr();
			}
		}
	}
	
	private void addMenuLink(Taxon taxon) {
		addRankIcon(menuDiv, taxon.getRank());
		menuDiv.addLink("#" + taxon.getName(), taxon.getName() + " (" + taxon.getNameFr() + ")", 
				taxon.getName());
		menuDiv.addBr();
	}
	
	private void exportJsonTree() {
		//Collection<Taxon> taxa = TaxonCache.getInstance().getAll();
		Set<Taxon> taxa = TaxonCache.getInstance().getTopLevel();
		log.info("Exporting " + taxa.size() + " top-level taxa as JSON tree");
		
		if (taxa == null || taxa.isEmpty()) {
			log.warn("Nothing to export, aborting");
			return;
		}
		
		JSONArray jsonTree = new JSONArray();
		for (Taxon taxon : taxa) {
			exportJsonTree(taxon, jsonTree);
		}
		
		String filename = htmlPath + "taxa.json";
		try {
			FileWriter fw = new FileWriter(filename);
			fw.write(jsonTree.toJSONString());
			fw.flush();
			fw.close();
		} catch (Exception exc) {
			log.error("Saving JSON tree failed", exc);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void exportJsonTree(Taxon taxon, JSONArray jsonTree) {
		JSONObject json = exportToJson(taxon);
		jsonTree.add(json);
		
		// export child taxa
		for (Taxon child : taxon.getChildren()) {
			exportJsonTree(child, jsonTree);
		}
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject exportToJson(Taxon taxon) {
		JSONObject json = new JSONObject();
		Taxon parent = taxon.getParent();
		TaxonRank rank = taxon.getRank();
		
		json.put("id", String.valueOf(taxon.getIdx()));
		json.put("parent", parent == null ? "#" : String.valueOf(parent.getIdx()));
		json.put("text", taxon.getName());
		HerbierPic pic = taxon.getTypicalPic();
		if (pic != null) {
			JSONObject jsonAAttr = new JSONObject();
			json.put("a_attr", jsonAAttr);
			jsonAAttr.put("href", getTaxonLink(taxon, pic));
			jsonAAttr.put("title", taxon.getNameFr());
			jsonAAttr.put("link", pic.getFileName().replace(".jpg", ""));
			jsonAAttr.put("pics", taxon.getPicsCascade().size());
		} else {
			log.error("No typical pic for " + taxon);
		}
		json.put("icon", "rank" + rank.getOrder() + ".svg");

		JSONObject jsonState = new JSONObject();
		json.put("state", jsonState);
		jsonState.put("opened", parent == null);
		//jsonState.put("selected", false);
		return json;
	}
	
	protected String getTaxonLink(Taxon taxon, HerbierPic typicalPic) {
		String url = "#";
		
		switch(taxon.getRank()) {
		case SPECIES:
			url = "pages/" + getTaxonHtmlFileName(taxon);
			break;
		case GENUS:
			// url = "pages/" + getTaxonHtmlFileName(taxon);  // does not always exist!
//			if (typicalPic != null) {
//				url = "pages/" + typicalPic.getFileName().replace(".jpg", ".html");
//			}
			return getTaxonLink(taxon.getParent(), typicalPic);
		case FAMILY:
		case CLASS:
			url = taxon.getParent().getName() + ".html#" + taxon.getName();
			break;
		case ORDER:
		case PHYLUM:
			url = taxon.getName() + ".html";
			break;
		case KINGDOM:
			url = "classification.html#" + taxon.getName();
			break;
		default:
			break;
		}
		
		return url;
	}
	
	
	// TODO refactor code (moved from TaxonPicsExporter...)
	private void exportTaxon(Taxon taxon, Taxon prevTaxon, Taxon nextTaxon) {
		Set<HerbierPic> pics = taxon.getPics();
		if (pics.isEmpty()) {
			return;
		}
		
		TaxonRank rank = taxon.getRank();
		String name = taxon.getName();
		String filename = getTaxonHtmlFileName(taxon);
		//log.info("Creating html page for " + name + " with " + pics.size() + " pics as " + filename);
		
		HtmlPage picPage = new HtmlPage("Nature - " + name, 1);
		HtmlComposite main = picPage.getMainDiv();
		
		String title = name;
		if (!name.equals(taxon.getNameFr())) {
			title += taxonNameSeparator + taxon.getNameFr();
		}
		main.addTitle(1, title);
		
		if (rank != TaxonRank.SPECIES && rank != TaxonRank.GENUS) {
			main.addTitle(2, "<font color='gray'>Genre indéterminé</font>");
		}

		// table for medium images
		HtmlComposite table = main.addFillTable(2, "1040px");
		table.setCssClass("table-medium");
		
		for (HerbierPic hpic : pics) {
			// image with link
			HtmlComposite td = table.addTableData();
			td.addAnchor(hpic.getFileName().replace(".jpg", ""));
			HtmlComposite link = td.addLink("../photos/" + hpic.getFileName(), name);
			link.addImage("../medium/" + hpic.getFileName(), name);
			
			// image details
			Location location = hpic.getLocation();
			HtmlComposite par = td.addPar();
			if (location != null) {
				String locFilename = "../lieu" + location.getIdx() + ".html";
				par.addLink(locFilename, location.getName(), location.getName());
				int altitude = location.getAltitude();
				if (altitude >= 1000) {
					par.addText(" (" + altitude + "m)");
				}
				par.addText(", " + location.getRegion());
				if (!"Suisse".equals(location.getState())) {
					par.addText(", " + location.getState());
				}
				par.addText(", ");
			}
			par.addText(dateFormat.format(hpic.getShotAt()));
			
			String remarks = hpic.getRemarks();
			if (remarks != null && !remarks.isEmpty()) {
				remarks = replaceRemarkLinks(remarks);
				td.addPar(remarks);
			}
		}
		
		// External links
		HtmlComposite par = main.addPar();
		par.addText("Pages ");
		taxonUrlProvider.addLinks(par, taxon);
		
		// classification
		HtmlComposite divClassif = addBoxDiv(main, "Classification");
		table = divClassif.addFillTable(3, "500px");
		
		Vector<Taxon> vecTaxa = new Vector<>();
		vecTaxa.add(taxon);
		Taxon parent = taxon.getParent();
		while (parent != null) {
			vecTaxa.add(0, parent);
			parent = parent.getParent();
		}
		
		for (Taxon t : vecTaxa) {
			HtmlComposite td = table.addTableData();
			TaxonRank rank2 = t.getRank();
			addRankIcon(td, rank2);
			td.addText(rank2.getGuiName());
			addClassificationTableData(table, t);
			table.addTableData(t.getNameFr());
		}
		
		
		// navigation links
		Taxon family = taxon.getAncestor(TaxonRank.FAMILY);
		Taxon order  = family.getParent();
		Taxon taxClass = order.getParent();
		Taxon phylum = taxClass.getParent();
		Taxon kingdom = phylum.getParent();
		
		HtmlComposite tableNav = main.addFillTable(3, "500px");
		tableNav.setCssClass("table-nav");
		tableNav.addTableData().addLink(getTaxonHtmlFileName(prevTaxon), prevTaxon.getName()).addImage("prev.gif", "Précédante");
		tableNav.addTableData().addLink("../" + order.getName() + ".html#" + family.getName(), 
				"Retour à la page des " + order.getName())
			.addImage("home.gif", "Retour");
		tableNav.addTableData().addLink(getTaxonHtmlFileName(nextTaxon), nextTaxon.getName()).addImage("next.gif", "Suivante");
		
		// menu items
		HtmlComposite menuDiv = picPage.getMenuDiv();
		menuDiv.addTitle(1, "Classification");
		
		addRankIcon(menuDiv, TaxonRank.KINGDOM);
		menuDiv.addLink("../classification.html#" + kingdom.getName(), kingdom.getNameFr(), kingdom.getName());
		menuDiv.addBr();
		
		addRankIcon(menuDiv, TaxonRank.CLASS);
		menuDiv.addLink("../" + phylum.getName() + ".html#" + taxClass.getName(), taxClass.getNameFr(), taxClass.getName());
		menuDiv.addBr();
		
		addRankIcon(menuDiv, TaxonRank.ORDER);
		menuDiv.addLink("../" + order.getName() + ".html", order.getNameFr(), order.getName());
		menuDiv.addBr();
		
		addRankIcon(menuDiv, TaxonRank.FAMILY);
		menuDiv.addLink("../" + order.getName() + ".html#" + family.getName(), family.getNameFr(), family.getName());
			
		if (TaxonRank.FAMILY != taxon.getRank()) {
			menuDiv.addBr();
			addRankIcon(menuDiv, taxon.getRank());
			menuDiv.addText(name);
		}
		
		picPage.saveAs(htmlPath + "pages/" + filename);
	}

	
	/**
	 * @param args unused
	 */
	public static void main(String[] args) {
		// run website export
		SpecialChars.init();
		Controller.getInstance().exportToHtml();
	}
	
}
