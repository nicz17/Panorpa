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
 * Creates HTML pages for Expeditions.
 * 
 * @author nicz
 *
 */
public class ExpeditionsExporter extends BaseExporter {

	public static final DateFormat dateFormatMenu   = new SimpleDateFormat("MMMM yyyy", Locale.FRENCH);
	public static final DateFormat dateFormatAnchor = new SimpleDateFormat("MM-yyyy");
	
	private String sLastMonthYear;

	public ExpeditionsExporter() {
		sLastMonthYear = null;
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
		}
		
		page.saveAs(htmlPath + "journal.html");
	}
	
	private void exportExpedition(Expedition exp, HtmlComposite parent, HtmlComposite menu) {
		
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
