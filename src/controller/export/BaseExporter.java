package controller.export;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.HerbierPic;
import model.Location;
import model.Taxon;
import model.TaxonRank;
import common.base.Logger;
import common.html.HtmlTag;
import common.html.JavascriptHtmlTag;
import common.html.TableHtmlTag;

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

	private static final Logger log = new Logger("BaseExporter", true);
	
	/** Path of created html pages */
	protected static final String htmlPath = Controller.htmlPath;
	//protected static final String htmlPath = Controller.exportPath + "tests/";
	
	/** Number of columns in html thumbnail tables */
	protected static final int nColumns = 4;
	
	/** Separator between latin and french taxon names */
	protected static final String taxonNameSeparator = " &mdash; ";
	
	
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
	protected void exportPicture(final HerbierPic hpic, final TableHtmlTag table) {
		exportPicture(hpic, table, false);
	}
	
	/**
	 * Exports the specified picture to the specified html table.
	 * 
	 * @param hpic       the picture to export
	 * @param table      the html table
	 * @param bWithDate  flag to write picture date or not
	 */
	protected void exportPicture(final HerbierPic hpic, final TableHtmlTag table, boolean bWithDate) {
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
	protected void exportPicture(final HerbierPic hpic, final TableHtmlTag table, boolean bWithDate, Integer nPics) {
		String name = hpic.getName();
		HtmlTag td = table.addCell();
		
		// image with link
		String picFile = getTaxonHtmlFileName(hpic.getTaxon());
		String picAnchor = "#" + hpic.getFileName().replace(".jpg", "");
		HtmlTag link = td.addImageLink("pages/" + picFile + picAnchor, name, 
				"thumbs/" + hpic.getFileName(), name);
		TaxonRank rank = hpic.getTaxon().getRank();
		if (rank == TaxonRank.SPECIES || rank == TaxonRank.GENUS) {
			link.addTag(new HtmlTag("i", "<br>" + name));
		} else {
			link.addGrayFont("<br>Genre indéterminé");
		}

		td.addAnchor(name);
		td.addSpan("<br>" + hpic.getFrenchName());
		
		if (nPics != null) {
			td.addGrayFont("<br>" + nPics.intValue() + " photo" + 
					(nPics.intValue() > 1 ? "s" : ""));
		} else {
			td.addSpan("<br>" + hpic.getFamily());
		}
		
		
		if (bWithDate) {
			td.addGrayFont("<br>" + dateFormat.format(hpic.getShotAt()));
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
	 * Returns the specified string with the first character in upper-case.
	 * @param str the input string
	 * @return the upper-cased string
	 */
	protected String upperCaseFirst(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
	
	/**
	 * Adds a geographical map for the specified location if possible.
	 * To enable the map, the location must have valid coordinates.
	 * 
	 * @param loc   the location to display on the map
	 * @param page  the HTML page to which to add a map
	 * @param parent  the TD where to display the map
	 */
	protected void addOpenStreetMap(Location loc, PanorpaHtmlPage page, HtmlTag parent, 
			List<Location> listNeighbors, List<HerbierPic> listPics, String sGpxFile) {
		// Check the location has valid map coords
		Location locNullIsland = new Location(0, "Null Island");
		locNullIsland.setLongitude(0.0);
		locNullIsland.setLatitude(0.0);
		locNullIsland.setMapZoom(5);
		Double dDistance = loc.getDistance(locNullIsland);
		boolean bValidCoords = (dDistance != null && dDistance.doubleValue() > 0.1);
		
		if (bValidCoords) {
			// Add headers and a map div
			addOpenLayersHeaders(page);
			HtmlTag divMap = parent.addDiv("ol-map");
			divMap.setClass("ol-map");
			divMap.addDiv("ol-popup");
			
			// Call map rendering Javascript code
			final JavascriptHtmlTag jsRenderMap = new JavascriptHtmlTag();
			jsRenderMap.addLine("var oVectorSource, oIconStyle;");
			jsRenderMap.addLine(String.format("renderMap(%.6f, %.6f, %d);", 
					loc.getLongitude().doubleValue(), loc.getLatitude().doubleValue(), loc.getMapZoom()));
			jsRenderMap.addLine(String.format("addMapMarker(%.6f, %.6f, \"%s\");", 
					loc.getLongitude().doubleValue(), loc.getLatitude().doubleValue(), loc.getName()));

			// Add markers for neighbor locations, with links
			if (listNeighbors != null) {
				for (Location locNeighbor : listNeighbors) {
					String sUrl = "lieu" + locNeighbor.getIdx() + ".html";
					jsRenderMap.addLine(String.format("addMapMarker(%.6f, %.6f, \"%s\", '%s');", 
						locNeighbor.getLongitude().doubleValue(), locNeighbor.getLatitude().doubleValue(), 
						locNeighbor.getName(), sUrl));
				}
			}
			
			// Add map track
			if (sGpxFile != null) {
				jsRenderMap.addLine("addMapTrack(\"" + sGpxFile + "\");");
			}
			
			// Add photo markers
			if (listPics != null) {
				for (HerbierPic pic : listPics) {
					if (pic.getLatitude() != null && pic.getLongitude() != null) {
						String picFile = getTaxonHtmlFileName(pic.getTaxon());
						String picAnchor = "#" + pic.getFileName().replace(".jpg", "");
						String sUrl = "pages/" + picFile + picAnchor;
						String sText = "<img src='thumbs/" + pic.getFileName() + "'>";
						jsRenderMap.addLine(String.format("addPicMarker(%.6f, %.6f, \"%s\", '%s');", 
								pic.getLongitude().doubleValue(), pic.getLatitude().doubleValue(), 
								sText, sUrl));
					}
				}
			}
			page.add(jsRenderMap);
		} else {
			log.info("Can't add map for location " + loc + ": distance to Null Island is " + dDistance);
		}
	}
	
	/**
	 * Adds OpenLayers scripts to the header of the specified page.
	 * @param page  the HTML page
	 */
	protected void addOpenLayersHeaders(final PanorpaHtmlPage page) {
		page.getHead().addScript("js/OpenLayers-v5.3.0.js");
		page.getHead().addScript("js/panorpa-maps.js");
		page.getHead().addCss("css/OpenLayers-v5.3.0.css");
	}
}
