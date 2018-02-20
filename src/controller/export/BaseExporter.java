package controller.export;

import java.text.DateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.HerbierPic;
import model.Taxon;
import model.TaxonRank;

import common.io.HtmlComposite;

import controller.Controller;
import controller.TaxonCache;
import controller.upload.UploadManager;

/**
 * Base class for exporting pictures as html documents.
 * 
 * @author nicz
 *
 */
public class BaseExporter {
	
	protected static final String htmlPath = Controller.htmlPath;
	//protected static final String htmlPath = Controller.exportPath + "tests/";
	
	protected static final int nColumns = 4;
	
	protected DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.FRANCE);
	

	public BaseExporter() {
		
	}
	
	/**
	 * Returns the filename of exported html documents for the specified taxon.
	 * 
	 * @param taxon  the taxon to export
	 * @return the html file name
	 */
	protected String getTaxonHtmlFileName(Taxon taxon) {
		return UploadManager.getInstance().getTaxonHtmlFileName(taxon);
	}
	
	/**
	 * Gets the text to display in tooltips for the specified taxon.
	 * 
	 * @param taxon  the taxon to export
	 * @return  the tooltip text (possibly empty, never null)
	 */
	protected String getTooltiptext(Taxon taxon) {
		if (taxon == null) {
			return "";
		}
		
		String tooltip = "";
		
		String name = taxon.getName();
		String namefr = taxon.getNameFr();
		
		if (namefr != null && !namefr.isEmpty()) {
			tooltip = namefr;
		} else {
			tooltip = name;
		}
		
		if (TaxonRank.SPECIES == taxon.getRank() || TaxonRank.GENUS == taxon.getRank()) {
			Taxon family = taxon.getAncestor(TaxonRank.FAMILY);
			if (family != null) {
				tooltip += " (" + family.getNameFr() + ")";
			}
		}
		
		return tooltip;
	}
	
	/**
	 * Exports the specified picture to the specified html table.
	 * 
	 * @param hpic       the picture to export
	 * @param table      the html table
	 */
	protected void exportPicture(final HerbierPic hpic, final HtmlComposite table) {
		exportPicture(hpic, table, false);
	}
	
	/**
	 * Exports the specified picture to the specified html table.
	 * 
	 * @param hpic       the picture to export
	 * @param table      the html table
	 * @param bWithDate  flag to write picture date or not
	 */
	protected void exportPicture(final HerbierPic hpic, final HtmlComposite table, boolean bWithDate) {
		exportPicture(hpic, table, bWithDate, null);
	}
	
	/**
	 * Exports the specified picture to the specified html table.
	 * 
	 * @param hpic       the picture to export
	 * @param table      the html table
	 * @param bWithDate  flag to write picture date or not
	 * @param nPics      number of pictures represented, may be null
	 */
	protected void exportPicture(final HerbierPic hpic, final HtmlComposite table, boolean bWithDate, Integer nPics) {
		String name = hpic.getName();
		HtmlComposite td = table.addTableData();
		
		// image with link
		String picFile = getTaxonHtmlFileName(hpic.getTaxon());
		//String picFile = hpic.getFileName().replace(".jpg", ".html");
		String picAnchor = "#" + hpic.getFileName().replace(".jpg", "");
		HtmlComposite link = td.addLink("pages/" + picFile + picAnchor, name);
		link.addImage("thumbs/" + hpic.getFileName(), name);
		TaxonRank rank = hpic.getTaxon().getRank();
		if (rank == TaxonRank.SPECIES || rank == TaxonRank.GENUS) {
			link.addText("<br><i>" + name + "</i>");
		} else {
			link.addText("<br><font color='gray'>Genre indéterminé</font>");
		}

		td.addAnchor(name);
		td.addText("<br>" + hpic.getFrenchName());
		if (nPics != null) {
			td.addText("<br><font color='gray'>" + nPics.intValue() + " photo" + 
					(nPics.intValue() > 1 ? "s" : "") + "</font>");
		} else {
			td.addText("<br>" + hpic.getFamily());
		}
		
		if (bWithDate) {
			td.addText("<br><font color='gray'>" + dateFormat.format(hpic.getShotAt()) + "</font>");
		}
	}
	
	/**
	 * Adds html links to known taxa in the specified picture remark text.
	 * Links are indicated using '[[...]]', for example '[[Rosa canina]]'.
	 * 
	 * @param  remark  the remark text to update
	 * @return the updated remark text.
	 */
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
			String url = getTaxonHtmlFileName(taxon);

			if (url != null) {
				replacement = "<a href=\"" + url + "\">" + taxon.getName() + "</a>";
			}
		}
		
		if (replacement != null && name != null) {
			result = remark.replace("[[" + name + "]]", replacement);
		}
		
		return result;
	}
	
	/**
	 * Creates an html box div with the specified title text and default 'myBox' CSS class.
	 * 
	 * @param parent    the parent html element where to add the box
	 * @param title     the box title
	 * @return  the created div.
	 */
	protected HtmlComposite addBoxDiv(HtmlComposite parent, String title) {
		return addBoxDiv(parent, title, "myBox");
	}
	
	/**
	 * Creates an html box div with the specified title text and CSS class.
	 * 
	 * @param parent    the parent html element where to add the box
	 * @param title     the box title
	 * @param cssClass  the box CSS class
	 * @return  the created div.
	 */
	protected HtmlComposite addBoxDiv(HtmlComposite parent, String title, String cssClass) {
		HtmlComposite box = parent.addDiv();
		box.setCssClass(cssClass);
		box.addTitle(2, title);
		return box;
	}
	
	
}
