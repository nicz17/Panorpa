package controller.export;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import model.Expedition;
import model.HerbierPic;
import model.Location;

import common.io.HtmlComposite;

import controller.Controller;
import controller.ExpeditionManager;
import controller.DatabaseTools.eOrdering;

/**
 * Creates HTML pages for Excursions.
 * 
 * @author nicz
 *
 */
public class ExpeditionsExporter extends BaseExporter {

	public static final DateFormat dateFormatMenu   = new SimpleDateFormat("yyyy", Locale.FRENCH);
	public static final DateFormat dateFormatAnchor = new SimpleDateFormat("yyyy");
	
	private String sLastMonthYear;
	private HtmlComposite ul;

	public ExpeditionsExporter() {
		sLastMonthYear = null;
		ul = null;
	}
	
	public void export() {
		sLastMonthYear = null;
		createExpeditionsPage();
	}
	
	private void createExpeditionsPage() {
		HtmlPage page = new HtmlPage("Nature - Excursions récentes");
		HtmlComposite main = page.getMainDiv();
		HtmlComposite menu = page.getMenuDiv();
		
		main.addTitle(1, "Excursions récentes");
		menu.addTitle(1, "Excursions");
		
		// List<Expedition> vecExpeditions = ExpeditionManager.getInstance().getRecentExpeditions(8);
		List<Expedition> vecExpeditions = Controller.getInstance().getExpeditions(eOrdering.BY_DATE, null);
		for (Expedition exp : vecExpeditions) {
			ExpeditionManager.getInstance().setExpeditionPics(exp);
			exportExpedition(exp, main, menu);
			createExcursionPage(exp);
		}
		
		page.saveAs(htmlPath + "journal.html");
	}
	
	private void createExcursionPage(Expedition exp) {
		HtmlPage page = new HtmlPage("Excursion &mdash; " + exp.getTitle());
		HtmlComposite main = page.getMainDiv();
		
		main.addTitle(1, exp.getTitle());
		
		HtmlComposite tableTop = main.addFillTable(2, "1440px");
		tableTop.setCssClass("align-top");
		HtmlComposite tdLeft = tableTop.addTableData();
		HtmlComposite tdRight = tableTop.addTableData();
		
		// OpenStreetMap
		addOpenStreetMap(exp.getLocation(), page, tdLeft, null);

		// Description
		HtmlComposite div = addBoxDiv(tdRight, "Description", "myBox");
		Location loc = exp.getLocation();
		String filenameLoc = "lieu" + loc.getIdx() + ".html";
		div.addPar().addLink(filenameLoc, loc.getName(), loc.getName());
		HtmlComposite par = div.addPar();
		par.addText("<font color='gray'>");
		par.addText(dateFormat.format(exp.getDateFrom()));
		par.addText(" &mdash; " + exp.getPics().size() + " photos</font>");
		div.addPar(exp.getNotes());
		
		// Photos
		List<HerbierPic> listPics = new ArrayList<>();
		listPics.addAll(exp.getPics());
		int nPics = listPics.size();
		
		main.addTitle(2, "Photos");
		main.addSpan("pics-count", String.valueOf(nPics) + " photo" + (nPics == 1 ? "" : "s"));
		HtmlComposite tablePics = main.addFillTable(nColumns);
		tablePics.setCssClass("table-thumbs");
		
		Collections.sort(listPics, new Comparator<HerbierPic>() {
			@Override
			public int compare(HerbierPic pic1, HerbierPic pic2) {
				return (pic1.getShotAt().before(pic2.getShotAt()) ? -1 : 1);
			}
		});
		
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
	
	private void exportExpedition(Expedition exp, HtmlComposite parent, HtmlComposite menu) {
		// add month and year to menu if needed
		String sYear = upperCaseFirst(dateFormatMenu.format(exp.getDateFrom()));
		if (!sYear.equals(sLastMonthYear)) {
			sLastMonthYear = sYear;
			String sAnchor = dateFormatAnchor.format(exp.getDateFrom());
			menu.addLink("#" + sAnchor, "Voir les observations de " + sYear.toLowerCase(), sYear);
			menu.addBr();
			parent.addAnchor(sAnchor).addTitle(2, sYear);
			ul = parent.addList();
		}
		
		HtmlComposite li = ul.addListItem();
		String filename = "excursion" + exp.getIdx() + ".html";
		li.addLink(filename, "Voir le journal", exp.getTitle());
		li.addText(" <font color='gray'>&mdash; ");
		li.addText(dateFormat.format(exp.getDateFrom()));
		//li.addText(" &mdash; " + exp.getPics().size() + " photos");
		li.addText("</font>");
	}
	
	private void exportExpeditionOld(Expedition exp, HtmlComposite parent, HtmlComposite menu) {
		
		// add month and year to menu if needed
		String sMonthYear = upperCaseFirst(dateFormatMenu.format(exp.getDateFrom()));
		if (!sMonthYear.equals(sLastMonthYear)) {
			sLastMonthYear = sMonthYear;
			String sAnchor = dateFormatAnchor.format(exp.getDateFrom());
			menu.addLink("#" + sAnchor, "Voir les observations de " + sMonthYear.toLowerCase(), sMonthYear);
			menu.addBr();
			parent.addAnchor(sAnchor);
		}
		
		// write the expedition to a myBox div
		Location loc = exp.getLocation();
		String sAnchor = "excursion" + exp.getIdx();
		HtmlComposite anchor = parent.addAnchor(sAnchor);
		HtmlComposite div = addBoxDiv(anchor, exp.getTitle(), "myBox myBox-wide");
		HtmlComposite par = div.addPar();
		String filename = "lieu" + loc.getIdx() + ".html";
		par.addLink(filename, loc.getName(), loc.getName());
		par.addText(" <font color='gray'>&mdash; ");
		par.addText(dateFormat.format(exp.getDateFrom()));
		par.addText(" &mdash; " + exp.getPics().size() + " photos</font>");
		filename = "excursion" + exp.getIdx() + ".html";
		par.addLink(filename, " link", exp.getTitle());
		
		div.addPar(exp.getNotes());
		
		HtmlComposite tablePics = div.addFillTable(nColumns);
		tablePics.setCssClass("table-thumbs");
		
		List<HerbierPic> listPics = new ArrayList<>();
		listPics.addAll(exp.getPics());
		Collections.sort(listPics, new Comparator<HerbierPic>() {
			@Override
			public int compare(HerbierPic pic1, HerbierPic pic2) {
				return (pic1.getShotAt().before(pic2.getShotAt()) ? -1 : 1);
			}
		});
		
		for (HerbierPic hpic : listPics) {
			String name = hpic.getName();
			HtmlComposite td = tablePics.addTableData();
			String picFile = getTaxonHtmlFileName(hpic.getTaxon());
			String picAnchor = "#" + hpic.getFileName().replace(".jpg", "");
			HtmlComposite link = td.addLink("pages/" + picFile + picAnchor, getTooltiptext(hpic.getTaxon()));
			link.addImage("thumbs/" + hpic.getFileName(), name);
		}
	}
}
