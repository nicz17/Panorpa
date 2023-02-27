package controller.export;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.base.Logger;
import common.html.HtmlTag;
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
		
		PanorpaHtmlPage page = new PanorpaHtmlPage("Projet de livre", Controller.exportPath + "book/index.html");
		page.getHead().addScript("https://cdnjs.cloudflare.com/ajax/libs/jquery/1.12.1/jquery.min.js");
		page.getHead().addScript("js/freewall.js");
		
		page.addTitle(1, "Projet de livre");
		page.addSpan("pics-count", String.valueOf(nPics) + " photo" + (nPics == 1 ? "" : "s"));
		
		HtmlTag divFreewall = page.addDiv("freewall");
		
		for (HerbierPic hpic : vecPics) {
			HerbierPic pic = PictureCache.getInstance().getPicture(hpic.getIdx());
			HtmlTag div = divFreewall.addDiv(null);
			div.setClass("item");
			div.addImage("../html/medium/" + pic.getFileName(), pic.getFileName());
			
			String nameFr = pic.getTaxon().getNameFr();
			String name = pic.getTaxon().getName();
			String namePar = nameFr;
			if (!name.equals(nameFr)) {
				namePar += " (" + name + ")";
			}
			div.addParagraph(namePar);
			
			Location location = hpic.getLocation();
			String sLocation = location.getName();
			int altitude = location.getAltitude();
			if (altitude >= 1000) {
				sLocation += " (" + altitude + "m)";
			}
			sLocation += ", " + location.getRegion();
			if (!"Suisse".equals(location.getState())) {
				sLocation += ", " + location.getState();
			}
			sLocation += ", " + dateFormat.format(pic.getShotAt());
			div.addParagraph(sLocation);
			
			String remarks = hpic.getRemarks();
			if (remarks != null && !remarks.isEmpty()) {
				remarks = replaceRemarkLinks(remarks);
				div.addParagraph(remarks);
			}
		}
		
		String script = "$(function() { var wall = new Freewall('#freewall'); ";
		script += "wall.reset({ selector: '.item', animate: true, cellW: 520, cellH: 610, " +
				"onResize: function() { wall.fitWidth(); } }); ";
		script += "wall.fitWidth(); });";
		page.addJavascript(script);
		
		page.save();
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
