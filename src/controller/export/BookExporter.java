package controller.export;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.base.Logger;
import common.io.HtmlComposite;
import common.io.SpecialChars;

import controller.Controller;
import controller.DataAccess;
import controller.DatabaseTools.eOrdering;
import controller.PictureCache;
import controller.TaxonCache;

import model.HerbierPic;
import model.Location;
import model.Taxon;

public class BookExporter extends BaseExporter {

	private static final Logger log = new Logger("BookExporter", true);
	
	public BookExporter() {
		
	}
	
	public void export() {
		eOrdering order = eOrdering.BY_DEFAULT;
		Vector<HerbierPic> vecPics = DataAccess.getInstance().getHerbierPics("WHERE picRating >= 4", order, null);
		int nPics = vecPics.size();
		log.info("Exporting " + nPics + " images for a book.");
		
		HtmlPage page = new HtmlPage("Projet de livre");
		page.getHead().addScript("https://cdnjs.cloudflare.com/ajax/libs/jquery/1.12.1/jquery.min.js");
		page.getHead().addScript("js/freewall.js");
		
		HtmlComposite main = page.getMainDiv();
		main.addTitle(1, "Projet de livre");
		main.addSpan("pics-count", String.valueOf(nPics) + " photo" + (nPics == 1 ? "" : "s"));
		
		HtmlComposite divFreewall = main.addDiv("freewall");
		
		for (HerbierPic hpic : vecPics) {
			HerbierPic pic = PictureCache.getInstance().getPicture(hpic.getIdx());
			HtmlComposite div = divFreewall.addDiv();
			div.setCssClass("item");
			div.addImage("../html/medium/" + pic.getFileName(), pic.getFileName());
			
			String nameFr = pic.getTaxon().getNameFr();
			String name = pic.getTaxon().getName();
			String namePar = nameFr;
			if (!name.equals(nameFr)) {
				namePar += " (" + name + ")";
			}
			div.addPar(namePar);
			
			Location location = hpic.getLocation();
			HtmlComposite par = div.addPar();
			par.addText(location.getName());
			int altitude = location.getAltitude();
			if (altitude >= 1000) {
				par.addText(" (" + altitude + "m)");
			}
			par.addText(", " + location.getRegion());
			if (!"Suisse".equals(location.getState())) {
				par.addText(", " + location.getState());
			}
			par.addText(", " + dateFormat.format(pic.getShotAt()));
			
			String remarks = hpic.getRemarks();
			if (remarks != null && !remarks.isEmpty()) {
				remarks = replaceRemarkLinks(remarks);
				div.addPar(remarks);
			}
		}
		
		String script = "$(function() { var wall = new Freewall('#freewall'); ";
		script += "wall.reset({ selector: '.item', animate: true, cellW: 520, cellH: 610, " +
				"onResize: function() { wall.fitWidth(); } }); ";
		script += "wall.fitWidth(); });";
		main.addTag("script", script);
		
		page.saveAs(Controller.exportPath + "book/index.html");
	}
	
	@Override
	protected String replaceRemarkLinks(String remark) {
		String result = remark;
		
		Pattern pat = Pattern.compile(".+\\[\\[(.+)\\]\\].*");
		Matcher mat = pat.matcher(remark);
		
		Taxon taxon = null;
		String replacement = null;
		String name = null;
		if (mat.matches()) {
			name = mat.group(1);
			//log.info("Remark " + remark + " matches with " + name);
			replacement = name;
			taxon = TaxonCache.getInstance().getTaxon(name);
		}
		
		if (taxon != null) {
			replacement = taxon.getNameFr();
		}
		
		if (replacement != null && name != null) {
			result = remark.replace("[[" + name + "]]", replacement);
		}
		
		return result;
	}

	/**
	 * @param args  unused
	 */
	public static void main(String[] args) {
		Controller.getInstance();
		SpecialChars.init();
		BookExporter exporter = new BookExporter();
		exporter.export();
	}
	
}
