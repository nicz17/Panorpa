package controller.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.Expedition;
import model.HerbierPic;
import model.Location;

import common.io.HtmlComposite;

import controller.ExpeditionManager;

/**
 * Creates HTML pages for Expeditions.
 * 
 * @author nicz
 *
 */
public class ExpeditionsExporter extends BaseExporter {

	public ExpeditionsExporter() {
	}
	
	public void export() {
		createExpeditionsPage();
	}
	
	private void createExpeditionsPage() {
		HtmlPage page = new HtmlPage("Nature - Expéditions récentes");
		HtmlComposite main = page.getMainDiv();
		
		main.addTitle(1, "Expéditions récentes");
		
		List<Expedition> vecExpeditions = ExpeditionManager.getInstance().getRecentExpeditions(8);
		
		for (Expedition exp : vecExpeditions) {
			exportExpedition(exp, main);
		}
		
		page.saveAs(htmlPath + "journal.html");
	}
	
	private void exportExpedition(Expedition exp, HtmlComposite parent) {
		Location loc = exp.getLocation();
		HtmlComposite div = addBoxDiv(parent, dateFormat.format(exp.getDate()), "myBox myBox-wide");
		HtmlComposite par = div.addPar();
		String filename = "lieu" + loc.getIdx() + ".html";
		par.addLink(filename, loc.getName(), loc.getName());
		par.addText(" <font color='gray'>&mdash; " + exp.getPics().size() + " photos</font>");
		//div.addSpan("pics-count", String.valueOf(exp.getPics().size()) + " photos");
		
		HtmlComposite tablePics = div.addFillTable(nColumns);
		tablePics.setCssClass("table-thumbs");
		
		List<HerbierPic> listPics = new ArrayList<>();
		listPics.addAll(exp.getPics());
		Collections.sort(listPics);
		
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
