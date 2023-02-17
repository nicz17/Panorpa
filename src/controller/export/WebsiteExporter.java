package controller.export;

import java.io.FileWriter;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import model.HerbierPic;
import model.Taxon;
import model.TaxonRank;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import common.base.Logger;
import common.html.HtmlTag;
import common.html.HtmlTagFactory;
import common.html.ListHtmlTag;
import common.html.TableHtmlTag;
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
	
	private PanorpaHtmlPage htmlPage;
	//private HtmlTag menuDiv;
	private TaxonExporter taxonExporter;
	
	public WebsiteExporter() {
		//taxonUrlProvider = new TaxonUrlProvider();
		taxonExporter = new TaxonExporter();
	}
	
	public void export(Set<Taxon> taxa) {
		log.info("Exporting " + taxa.size() + " top-level taxa");
		
		if (taxa == null || taxa.isEmpty()) {
			log.warn("Nothing to export, aborting");
			return;
		}
		
		htmlPage = new PanorpaHtmlPage("Photos de nature", htmlPath + "classification.html");
		htmlPage.addTitle(1, "Classification");
		
		//menuDiv = htmlPage.getMenu();
		htmlPage.addMenuItem(1, "#", "Classification");
		
		for (Taxon taxon : taxa) {
			if (TaxonRank.KINGDOM.equals(taxon.getRank())) {
				
				// create menu
				addMenuLink(taxon);
				for (Taxon child : taxon.getChildren()) {
					addMenuLink(child);
				}
				
				exportTaxon(taxon, this.htmlPage);
			} else {
				log.warn("Skipping non-kingdom " + taxon);
			}
		}
		
		createLinksPage();
		
		htmlPage.save();
		
		exportJsonTree();
	}
	
	private void exportTaxon(Taxon taxon, PanorpaHtmlPage page) {
		TaxonRank rank = taxon.getRank();
		TaxonRank rankChild = rank.getChildRank();
		String name = taxon.getName();
		
		page.addAnchor(name);
		page.addTitle(2, rank.getGuiName() + " " + taxon.getName() + taxonNameSeparator + taxon.getNameFr());
		
		int nPics = taxon.getPicsCascade().size();
		if (nPics > 0) {
			page.addSpan("pics-count", String.valueOf(nPics) + " photo" + (nPics == 1 ? "" : "s"));

			if (TaxonRank.ORDER.equals(rankChild) || TaxonRank.PHYLUM.equals(rankChild)) {
				TableHtmlTag table = page.addFillTable(nColumns, "100%");
				table.setClass("table-thumbs");

				for (Taxon child : taxon.getChildren()) {
					nPics = child.getPicsCascade().size();
					if (nPics > 0) {
						HtmlTag td = table.addCell();
						HerbierPic hpic = child.getTypicalPic();

						// image with link to order page
						String orderName = child.getName();
						td.addTag(HtmlTagFactory.anchor(orderName));
						HtmlTag link = HtmlTagFactory.imageLink(orderName + ".html", "Aller à la page des " + orderName, 
								"thumbs/" + hpic.getFileName(), hpic.getName());
						link.addTag(new HtmlTag("span", "<br>" + orderName + " (" + nPics + ")"));
						td.addTag(link);

						if (TaxonRank.PHYLUM.equals(rankChild)) {
							exportPhylum(child);
						} else if (TaxonRank.ORDER.equals(rankChild)) {
							exportOrder(child);
						}
					}
				}
			} else {
				for (Taxon child : taxon.getChildren()) {
					exportTaxon(child, page);
				}
			}
		}
	}
	
	private void exportPhylum(Taxon phylum) {
		String name = phylum.getName();
		PanorpaHtmlPage orderPage = new PanorpaHtmlPage("Nature - " + name, htmlPath + name + ".html");
		
		final TreeSet<HerbierPic> tsOrderPics = phylum.getPicsCascade();
		int nPics = tsOrderPics.size();

		orderPage.addTitle(1, "Phylum " + name + taxonNameSeparator + phylum.getNameFr());
		orderPage.addSpan("pics-count", String.valueOf(nPics) + " photo" + (nPics == 1 ? "" : "s"));
		
		orderPage.addMenuItem(1, "#", "Classification");
		writeTaxonHierarchy(orderPage, phylum);
		orderPage.addMenuItem(2, "#", "Classes");
		
		for (Taxon child : phylum.getChildren()) {
			//addRankIcon(orderPage, child.getRank());
			orderPage.addMenuItem(3, "#" + child.getName(), child.getName());
			exportTaxon(child, orderPage);
		}
		
		HtmlTag tCenter = new HtmlTag("center");
		tCenter.addTag(HtmlTagFactory.imageLink("classification.html#" + name, "Retour", "pages/home.gif", "Retour"));
		orderPage.add(tCenter);
		orderPage.save();
	}
	
	private void exportOrder(Taxon order) {
		String name = order.getName();
		PanorpaHtmlPage orderPage = new PanorpaHtmlPage("Nature - " + name, htmlPath + name + ".html");
		
		final TreeSet<HerbierPic> tsOrderPics = order.getPicsCascade();
		int nPics = tsOrderPics.size();

		orderPage.addTitle(1, "Ordre des " + name + taxonNameSeparator + order.getNameFr());
		orderPage.addSpan("pics-count", String.valueOf(nPics) + " photo" + (nPics == 1 ? "" : "s"));
		
		orderPage.addMenuItem(1, "#", "Classification");
		writeTaxonHierarchy(orderPage, order);
		orderPage.addMenuItem(2, "#", "Familles");
		
		for (Taxon family : order.getChildren()) {
			final TreeSet<HerbierPic> tsFamilyPics = family.getPicsCascade();
			nPics = tsFamilyPics.size();
			
			TreeSet<Taxon> tsTaxa = new TreeSet<>();
			
			orderPage.addAnchor(family.getName());
			String title = family.getName();
			if (!name.equals(family.getNameFr())) {
				title += taxonNameSeparator + family.getNameFr();
			}
			orderPage.addTitle(2, "Famille des " + title);
			orderPage.addSpan("pics-count", String.valueOf(nPics) + " photo" + (nPics == 1 ? "" : "s"));
			
			//addRankIcon(menu, TaxonRank.FAMILY);
			orderPage.addMenuItem(3, "#" + family.getName(), family.getName());
			
			TableHtmlTag table = orderPage.addFillTable(nColumns, "100%");
			table.setClass("table-thumbs");
			
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
				
				//exportTaxon(taxon, prevTaxon, nextTaxon);
				taxonExporter.exportTaxon(taxon, prevTaxon, nextTaxon);
			}
		}
		
		// Go back to phylum
		Taxon phylum = order.getAncestor(TaxonRank.PHYLUM);
		HtmlTag tCenter = new HtmlTag("center");
		tCenter.addTag(HtmlTagFactory.imageLink(phylum.getName() + ".html#" + name, "Retour", "pages/home.gif", "Retour"));
		orderPage.add(tCenter);
		orderPage.save();
	}
	
	/**
	 * Creates a HTML page with web links and a bibliography.
	 */
	private void createLinksPage() {
		PanorpaHtmlPage linksPage = new PanorpaHtmlPage("Nature - Liens", htmlPath + "liens.html", "");
		linksPage.addTitle(1, "Liens externes");
		
		linksPage.addTitle(2, "Insectes");
		ListHtmlTag ul = linksPage.addList();
		ul.addItem(HtmlTagFactory.link("http://www.insecte.org/forum/", "Le monde des insectes - forum", true));
		ul.addItem(HtmlTagFactory.link("http://www.galerie-insecte.org/galerie/fichier.php", 
				"Le monde des insectes - galerie", true));
		ul.addItem(HtmlTagFactory.link("http://www.galerie-insecte.org/galerie/auteur.php?aut=6169", 
				"Le monde des insectes - mes photos", true));
		ul.addItem(HtmlTagFactory.link("http://www.quelestcetanimal.com/", "Quel est cet animal ?", true));
		ul.addItem(HtmlTagFactory.link("http://spipoll.snv.jussieu.fr/mkey/mkey-spipoll.html", 
				"SpiPoll - Suivi photographique des insectes pollinisateurs", true));
		ul.addItem(HtmlTagFactory.link("http://lepus.unine.ch/carto/", "InfoFauna UniNe", true));
		ul.addItem(HtmlTagFactory.link("http://www.lepido.ch/", "Papillons diurnes de Suisse", true));
		ul.addItem(HtmlTagFactory.link("http://cle.fourmis.free.fr/castes-fourmis.html", "Fourmis de France", true));
		ul.addItem(HtmlTagFactory.link("http://home.hccnet.nl/mp.van.veen/conopidae/ConGenera.html", "Clé des Conopidae", true));
		
		linksPage.addTitle(2, "Plantes");
		ul = linksPage.addList();
		ul.addItem(HtmlTagFactory.link("http://www.visoflora.com/", "Visoflora", true));
		ul.addItem(HtmlTagFactory.link("https://www.infoflora.ch/fr/", "Infoflora - flore de Suisse", true));
		ul.addItem(HtmlTagFactory.link("http://www.tela-botanica.org/bdtfx-nn-60585-synthese", "Tela botanica", true));
		ul.addItem(HtmlTagFactory.link("http://abiris.snv.jussieu.fr/flore/flore.php", 
				"Identification assistée par ordinateur", true));
		
		linksPage.addTitle(2, "Autres");
		ul = linksPage.addList();
		ul.addItem(HtmlTagFactory.link("http://www.inaturalist.org/observations/nicz", "iNaturalist", true));
		ul.addItem(HtmlTagFactory.link("http://www.salamandre.net/", "La Salamandre - la revue des curieux de nature", true));
		ul.addItem(HtmlTagFactory.link("http://www.pronatura-vd.ch/nos_reserves", "Réserves Pro Natura Vaud", true));
		ul.addItem(HtmlTagFactory.link("http://www.arocha.ch/fr/projects/aide-entretien-pps/", "A Rocha - entretien de prairies sèches", true));
		ul.addItem(HtmlTagFactory.link("http://www.ornitho.ch/index.php?m_id=1", "Plate-forme ornithologique suisse", true));
		ul.addItem(HtmlTagFactory.link("https://www.thunderforest.com/", "Cartes par Thunderforest et OpenLayers", true));
		
		// Bibliographie
		linksPage.addTitle(1, "Bibliographie");
		ListHtmlTag biblio = linksPage.addList();
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
		
		linksPage.save();
	}
	
	/**
	 * Adds a bibliographical reference to the specified list.
	 * @param biblio    the HTML list
	 * @param sAuthors  the author name(s)
	 * @param sTitle    the publication title
	 * @param sEditor   the editor
	 * @param sYear     the publication year
	 */
	private void addBiblioRef(ListHtmlTag biblio, String sAuthors, String sTitle, String sEditor, String sYear) {
		String sRef = sAuthors + " : <b>" + sTitle + "</b>, " + sEditor;
		if (sYear != null && !sYear.isEmpty()) {
			sRef += ", <font color='grey'>" + sYear + "</font>";
		}
		biblio.addItem(sRef);
	}
	
	/* TODO remove (unused)
	protected void addRankIcon(HtmlComposite html, TaxonRank rank) {
		html.addImage("rank" + rank.getOrder() + ".svg", rank.getGuiName(), null, rank.getGuiName());
		//html.addSvgSquare("16", rank.getColor(), rank.getGuiName());
	} */
	
	/**
	 * Writes the hierarchy of the specified taxon 
	 * on one line on the specified html composite.
	 * @param html  the html composite to write to
	 * @param taxon the taxon
	 */
	private void writeTaxonHierarchy(PanorpaHtmlPage html, Taxon taxon) {
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
				//addRankIcon(html, t.getRank());
				html.addMenuItem(3, getTaxonLink(t, null), name);
			}
		}
	}
	
	private void addMenuLink(Taxon taxon) {
		//addRankIcon(menuDiv, taxon.getRank());
		htmlPage.addMenuItem(3, "#" + taxon.getName(), taxon.getName());
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
	
	
	/**
	 * @param args unused
	 */
	public static void main(String[] args) {
		// run website export
		SpecialChars.init();
		Controller.getInstance().exportToHtml();
	}
	
}
