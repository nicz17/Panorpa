package controller.export;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import common.html.HtmlTag;
import common.html.ListHtmlTag;
import common.html.ParHtmlTag;
import common.html.TableHtmlTag;
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
			exportExcursion(exp, page);
			createExcursionPage(exp);
		}
		
		page.save();
	}
	
	private void createExcursionPage(Expedition exp) {
		String filename = "excursion" + exp.getIdx() + ".html";
		PanorpaHtmlPage page = new PanorpaHtmlPage("Excursion &mdash; " + exp.getTitle(), htmlPath + filename, "");
		page.addTitle(1, exp.getTitle());
		
		TableHtmlTag tableTop = page.addTable(2, "1440px");
		tableTop.setClass("align-top");
		HtmlTag tdLeft  = tableTop.addCell();
		HtmlTag tdRight = tableTop.addCell();
		
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
		HtmlTag div = tdRight.addBox("Excursion");
		Location loc = exp.getLocation();
		String filenameLoc = "lieu" + loc.getIdx() + ".html";
		ParHtmlTag par = new ParHtmlTag(null);
		par.addLink(filenameLoc, loc.getName(), loc.getName(), false);
		div.addTag(par);
		par = new ParHtmlTag(null);
		par.addGrayFont(dateFormat.format(exp.getDateFrom()) + 
				" &mdash; " + exp.getPics().size() + " photos");
		div.addTag(par);
		div.addTag(new ParHtmlTag(exp.getNotes()));
		div.addTag(new ParHtmlTag("Durée " + durationFormat.format(exp.getDuration(), false)));
		
		// Photos table
		page.addTitle(2, "Photos");
		page.addSpan("pics-count", String.valueOf(nPics) + " photo" + (nPics == 1 ? "" : "s"));
		TableHtmlTag tablePics = page.addTable(nColumns, "100%");
		tablePics.setClass("table-thumbs");
		
		for (HerbierPic hpic : listPics) {
			exportPicture(hpic, tablePics, false);
		}
		
		page.save();
	}
	
	private void exportExcursion(Expedition exp, PanorpaHtmlPage parent) {
		// add month and year to menu if needed
		String sYear = upperCaseFirst(dateFormatMenu.format(exp.getDateFrom()));
		if (!sYear.equals(sLastMonthYear)) {
			sLastMonthYear = sYear;
			String sAnchor = dateFormatAnchor.format(exp.getDateFrom());
			parent.addMenuItem(3, "#" + sAnchor, sYear); //, "Voir les observations de " + sYear.toLowerCase()));
			HtmlTag tAnchor = parent.addAnchor(sAnchor);
			tAnchor.addTitle(2, sYear);
			ul = parent.addList();
		}
		
		HtmlTag li = ul.addItem();
		String filename = "excursion" + exp.getIdx() + ".html";
		li.addLink(filename, exp.getTitle(), "Voir le journal", false);
		li.addGrayFont(" &mdash; " + dateFormat.format(exp.getDateFrom()));
	}
}
