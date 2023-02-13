package controller.export;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import common.html.HtmlTag;
import common.html.HtmlTagFactory;
import common.html.ListHtmlTag;
import common.io.HtmlComposite;
import common.text.DurationFormat;
import controller.Controller;
import controller.DatabaseTools.eOrdering;
import controller.ExpeditionManager;
import controller.FileManager;
import model.Expedition;
import model.HerbierPic;
import model.Location;

/**
 * Creates HTML pages for Excursions.
 * 
 * @author nicz
 *
 */
public class ExpeditionsExporter extends BaseExporter {

	public static final DateFormat dateFormatMenu   = new SimpleDateFormat("yyyy", Locale.FRENCH);
	public static final DateFormat dateFormatAnchor = new SimpleDateFormat("yyyy");
	public static final DurationFormat durationFormat = new DurationFormat();
	public static final String dirTrack = "geotrack/";
	
	private String sLastMonthYear;
	private ListHtmlTag ul;

	public ExpeditionsExporter() {
		sLastMonthYear = null;
		ul = null;
	}
	
	public void export() {
		sLastMonthYear = null;
		createExpeditionsPage();
	}
	
	private void createExpeditionsPage() {
		PanorpaHtmlPage page = new PanorpaHtmlPage("Nature - Excursions récentes", htmlPath + "journal.html", "");
		
		page.addTitle(1, "Excursions récentes");
		page.getMenu().addTag(new HtmlTag("h1", "Excursions"));
		
		// List<Expedition> vecExpeditions = ExpeditionManager.getInstance().getRecentExpeditions(8);
		List<Expedition> vecExpeditions = Controller.getInstance().getExpeditions(eOrdering.BY_DATE, null);
		for (Expedition exp : vecExpeditions) {
			ExpeditionManager.getInstance().setExpeditionPics(exp);
			exportExpedition(exp, page);
			createExcursionPage(exp);
		}
		
		page.save();
	}
	
	private void createExcursionPage(Expedition exp) {
		HtmlPage page = new HtmlPage("Excursion &mdash; " + exp.getTitle());
		HtmlComposite main = page.getMainDiv();
		
		main.addTitle(1, exp.getTitle());
		
		HtmlComposite tableTop = main.addFillTable(2, "1440px");
		tableTop.setCssClass("align-top");
		HtmlComposite tdLeft = tableTop.addTableData();
		HtmlComposite tdRight = tableTop.addTableData();
		
		// Photos
		List<HerbierPic> listPics = new ArrayList<>();
		listPics.addAll(exp.getPics());
		int nPics = listPics.size();
		Collections.sort(listPics, HerbierPic.comparatorByShotAt);
		
		for (HerbierPic pic : listPics) {
			FileManager.getInstance().addGPSCoords(pic);
		}
		
		// OpenStreetMap
		String sGpxFile = null;
		if (exp.getTrack() != null && !exp.getTrack().isEmpty()) {
			sGpxFile = dirTrack + exp.getTrack() + ".gpx";
		}
		addOpenStreetMap(exp.getLocation(), page, tdLeft, null, listPics, sGpxFile);

		// Description
		HtmlComposite div = addBoxDiv(tdRight, "Excursion", "myBox");
		Location loc = exp.getLocation();
		String filenameLoc = "lieu" + loc.getIdx() + ".html";
		div.addPar().addLink(filenameLoc, loc.getName(), loc.getName());
		HtmlComposite par = div.addPar();
		par.addText("<font color='gray'>");
		par.addText(dateFormat.format(exp.getDateFrom()));
		par.addText(" &mdash; " + exp.getPics().size() + " photos</font>");
		div.addPar(exp.getNotes());
		div.addPar("Durée " + durationFormat.format(exp.getDuration(), false));
		
		// Photos table
		main.addTitle(2, "Photos");
		main.addSpan("pics-count", String.valueOf(nPics) + " photo" + (nPics == 1 ? "" : "s"));
		HtmlComposite tablePics = main.addFillTable(nColumns);
		tablePics.setCssClass("table-thumbs");
		
		for (HerbierPic hpic : listPics) {
			String name = hpic.getName();
			HtmlComposite td = tablePics.addTableData();
			String picFile = getTaxonHtmlFileName(hpic.getTaxon());
			String picAnchor = "#" + hpic.getFileName().replace(".jpg", "");
			HtmlComposite link = td.addLink("pages/" + picFile + picAnchor, getTooltiptext(hpic.getTaxon()));
			link.addImage("thumbs/" + hpic.getFileName(), name);
		}
		
		String filename = "excursion" + exp.getIdx() + ".html";
		page.saveAs(htmlPath + filename);
	}
	
	private void exportExpedition(Expedition exp, PanorpaHtmlPage parent) {
		// add month and year to menu if needed
		String sYear = upperCaseFirst(dateFormatMenu.format(exp.getDateFrom()));
		if (!sYear.equals(sLastMonthYear)) {
			sLastMonthYear = sYear;
			String sAnchor = dateFormatAnchor.format(exp.getDateFrom());
			parent.addMenuItem(3, "#" + sAnchor, sYear); //, "Voir les observations de " + sYear.toLowerCase()));
			HtmlTag tAnchor = HtmlTagFactory.anchor(sAnchor);
			tAnchor.addTag(HtmlTagFactory.title(2, sYear));
			parent.add(tAnchor);
			ul = parent.addList();
		}
		
		HtmlTag li = ul.addItem();
		String filename = "excursion" + exp.getIdx() + ".html";
		li.addTag(HtmlTagFactory.link(filename, exp.getTitle(), "Voir le journal"));
		li.addTag(HtmlTagFactory.grayFont(" &mdash; " + dateFormat.format(exp.getDateFrom())));
	}
}
