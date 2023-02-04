package controller.export;

import java.util.Date;
import java.util.Vector;

import common.html.DivHtmlTag;
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
	 * @param sTitle
	 * @param sFilename
	 */
	public PanorpaHtmlPage(String sTitle, String sFilename) {
		super(sTitle, sFilename, "style.css", Panorpa.getInstance().getAppName());
	}
	
	@Override
	protected void buildHeader() {
		HtmlTag header = new DivHtmlTag("header");
		header.addTag(new HtmlTag("small", "Photos de nature"));
		body.addTag(header);
	}
	
	@Override
	protected void buildMenu() {
		this.menu = new DivHtmlTag("menu");
		body.addTag(menu);
		addMenuItem(1, "index.html", "Accueil");
		addMenuItem(3, "tree.html", "Classification");
		addMenuItem(3, "latest.html", "Dernières photos");
		addMenuItem(3, "lieux.html", "Lieux");
		addMenuItem(3, "journal.html", "Excursions");
		addMenuItem(3, "liens.html", "Liens");
		addMenuItem(3, "http://www.tf79.ch", "TF79.ch");
		addMenuItem(1, "#", "Classification");
	}
	
	@Override
	protected void buildFooter() {
		HtmlTag footer = new DivHtmlTag("footer");
		String sFooter = "Copyleft Nicolas Zwahlen &mdash; " + 
				dateFormat.format(new Date()) + 
				" &mdash; Panorpa v" + Panorpa.getInstance().getAppVersion();
		footer.addTag(new HtmlTag("p", sFooter));
		body.addTag(footer);
	}
	
	public void addMenuItem(int iLevel, String url, String text) {
		HtmlTag item = new HtmlTag("h" + iLevel);
		item.addTag(HtmlTagFactory.link(url, text));
		menu.addTag(item);
	}
	
	private static void test() {
		String sFilename = Controller.htmlPath + "test.html";
		String sSpecies = "Idaea aureolaria";
		PanorpaHtmlPage page = new PanorpaHtmlPage("Nature - Test", sFilename);
		page.add(HtmlTagFactory.title(1, "PanorpaHtmlPage Test -- " + sSpecies));
		page.addMenuItem(3, "#", sSpecies);
		
		TableHtmlTag tablePhotos = new TableHtmlTag(2);
		tablePhotos.addAttribute("width", "1040px");
		tablePhotos.addAttribute("class", "table-medium");
		for (int i=1; i<3; ++i) {
			HtmlTag linkImage = new HtmlTag("a");
			linkImage.addAttribute("href", "photos/idaea-aureolaria00" + i + ".jpg");
			linkImage.addTag(HtmlTagFactory.image("medium/idaea-aureolaria00" + i + ".jpg", sSpecies));
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
