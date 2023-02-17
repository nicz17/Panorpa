package controller.export;

import java.util.Date;
import java.util.Vector;

import common.html.HtmlPage;
import common.html.HtmlTag;
import common.html.HtmlTagFactory;
import common.html.TableHtmlTag;
import controller.Controller;
import view.Panorpa;

/**
 * An HTML page generator for http://www.tf79.ch/nature/
 *
 * <p><b>Modifications:</b>
 * <ul>
 * <li>04.02.2023: nicz - Creation</li>
 * </ul>
 */
public class PanorpaHtmlPage extends HtmlPage {
	
	private HtmlTag menu;

	/**
	 * Constructor.
	 * @param sTitle     the page title
	 * @param sFilename  the filename for saving the page
	 * @param sPath      the relative path for links
	 */
	public PanorpaHtmlPage(String sTitle, String sFilename) {
		this(sTitle, sFilename, "");
	}

	/**
	 * Constructor.
	 * @param sTitle     the page title
	 * @param sFilename  the filename for saving the page
	 * @param sPath      the relative path for links
	 */
	public PanorpaHtmlPage(String sTitle, String sFilename, String sPath) {
		super(sTitle, sFilename, sPath, "style.css", 
				Panorpa.getInstance().getAppName() + " v" + Panorpa.getInstance().getAppVersion());
	}
	
	@Override
	protected void buildHeader() {
		HtmlTag header = HtmlTagFactory.div("header");
		header.addTag(new HtmlTag("small", "Photos de nature &mdash; par Nicolas Zwahlen"));
		body.addTag(header);
	}
	
	@Override
	protected void buildMenu() {
		this.menu = HtmlTagFactory.div("menu");
		body.addTag(menu);
		addMenuItem(1, sPath + "index.html", "Accueil");
		addMenuItem(3, sPath + "tree.html", "Classification");
		addMenuItem(3, sPath + "latest.html", "Dernières photos");
		addMenuItem(3, sPath + "lieux.html", "Lieux");
		addMenuItem(3, sPath + "noms-latins.html", "Noms latins");
		addMenuItem(3, sPath + "noms-verna.html", "Noms communs");
		addMenuItem(3, sPath + "journal.html", "Excursions");
		addMenuItem(3, sPath + "search.html", "Chercher");
		addMenuItem(3, sPath + "liens.html", "Liens");
		addMenuItem(3, "http://www.tf79.ch", "TF79.ch");
	}
	
	@Override
	protected void buildFooter() {
		HtmlTag footer = HtmlTagFactory.div("footer");
		String sFooter = "Copyleft Nicolas Zwahlen &mdash; " + 
				dateFormatFr.format(new Date()) + 
				" &mdash; Panorpa v" + Panorpa.getInstance().getAppVersion();
		footer.addTag(new HtmlTag("p", sFooter));
		body.addTag(footer);
	}
	
	public HtmlTag getMenu() {
		return this.menu;
	}
	
	/**
	 * Adds a menu item to the menu on the left.
	 * @param iLevel  the menu item level (1 to 3)
	 * @param url     the link url
	 * @param text    the link text
	 */
	public void addMenuItem(int iLevel, String url, String text) {
		HtmlTag item = new HtmlTag("h" + iLevel);
		item.addTag(HtmlTagFactory.link(url, text));
		menu.addTag(item);
	}
	
	private static void test() {
		String sFilename = Controller.htmlPath + "test.html";
		String sSpecies = "Idaea aureolaria";
		PanorpaHtmlPage page = new PanorpaHtmlPage("Nature - Test", sFilename, "");
		page.add(HtmlTagFactory.title(1, "PanorpaHtmlPage Test -- " + sSpecies));
		page.addMenuItem(3, "#", sSpecies);
		
		TableHtmlTag tablePhotos = new TableHtmlTag(2);
		tablePhotos.addAttribute("width", "1040px");
		tablePhotos.setClass("table-medium");
		for (int i=1; i<3; ++i) {
			HtmlTag linkImage = new HtmlTag("a");
			linkImage.addAttribute("href", "photos/idaea-aureolaria00" + i + ".jpg");
			linkImage.addTag(HtmlTagFactory.image("medium/idaea-aureolaria00" + i + ".jpg", sSpecies, sSpecies));
			Vector<HtmlTag> vecTags = new Vector<>();
			vecTags.add(HtmlTagFactory.anchor("Idaea_auro" + i));
			vecTags.add(linkImage);
			vecTags.add(new HtmlTag("p", page.getLink("lieu65.html", "Lieu", false) + ", date"));
			tablePhotos.addCell(vecTags);
		}
		page.add(tablePhotos);
		
		String sPages = "Pages ";
		sPages += page.getLink("http://fr.wikipedia.org/wiki/" + sSpecies, "Wikipedia [fr]", true) + " | ";
		sPages += page.getLink("http://en.wikipedia.org/wiki/" + sSpecies, "Wikipedia [en]", true);
		page.add(new HtmlTag("p", sPages));
		
		HtmlTag boxClassif = HtmlTagFactory.blueBox("Classification");
		page.add(boxClassif);
		TableHtmlTag tableClassif = new TableHtmlTag(3);
		tableClassif.addAttribute("width", "500px");
		boxClassif.addTag(tableClassif);
		tableClassif.addCell("Règne");
		tableClassif.addCell(HtmlTagFactory.link("classification.html#Animalia", "Animalia"));
		tableClassif.addCell("Animaux");
		tableClassif.addCell("Espèce");
		tableClassif.addCell(new HtmlTag("i", sSpecies));
		tableClassif.addCell("Acidalie des alpages");
		
		page.save();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		test();
	}

}
