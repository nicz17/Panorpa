package controller.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import model.AltitudeLevel;
import model.Expedition;
import model.HerbierPic;
import model.Location;

import common.base.Logger;
import common.io.HtmlComposite;

import controller.Controller;
import controller.ExpeditionManager;
import controller.LocationCache;

/**
 * Subclass of Exporter to create location pages.
 * 
 * @author nicz
 *
 */
public class LocationExporter extends BaseExporter {

	private static final Logger log = new Logger("LocationExporter", true);
	
	public LocationExporter() {
	}
	
	public void export() {
		ExpeditionManager.getInstance().clearRecentExpeditions();
		createLocationsPage();
	}
	
	/**
	 * Creates an HTML page with a map showing all locations, 
	 * and links to all locations.
	 */
	private void createLocationsPage() {
		HtmlPage page = new HtmlPage("Nature - Lieux");
		page.getHead().addScript("https://cdn.rawgit.com/openlayers/openlayers.github.io/master/en/v5.3.0/build/ol.js");
		page.getHead().addScript("js/panorpa-maps.js");
		page.getHead().addCss("https://openlayers.org/en/v5.3.0/css/ol.css");
		
		HtmlComposite main = page.getMainDiv();	
		main.addTitle(1, "Lieux");
		main.addDiv("ol-map").setCssClass("ol-map");
		
		// Call map rendering Javascript code
		String sRenderMap = "var oVectorSource, oIconStyle;\n" +
			"renderMap(6.7607, 46.6334, 9);\n";
		
		HtmlComposite table = main.addFillTable(2, "800px");
		table.setCssClass("align-top");
		HtmlComposite td = table.addTableData();
		
		List<Location> locations = new ArrayList<>(LocationCache.getInstance().getAll());
		Collections.sort(locations, new Comparator<Location>() {
			@Override
			public int compare(Location loc1, Location loc2) {
				int alt1 = loc1.getAltitudeLevel().getMinAltitude();
				int alt2 = loc2.getAltitudeLevel().getMinAltitude();
				if (alt1 != alt2) {
					return alt1 - alt2;
				} else {
					return loc1.getName().compareTo(loc2.getName());
				}
			}
			
		});
		log.info("Exporting " + locations.size() + " locations");
		
		HashMap<AltitudeLevel, HtmlComposite> mapLevels = new HashMap<>();
		int nLevels = 0;
		
		for (Location location : locations) {
			if (location.getPics().isEmpty()) {
				log.warn("Skipping location without pictures: " + location);
			} else {
				AltitudeLevel level = location.getAltitudeLevel();
				
				HtmlComposite ul = mapLevels.get(level);
				if (ul == null) {
					nLevels++;
					if (nLevels == 2) {
						td = table.addTableData();
					}
					td.addTitle(2, level.getLabel());
					ul = td.addList();
					mapLevels.put(level, ul);
				}
				
				HtmlComposite li = ul.addListItem();
				String filename = "lieu" + location.getIdx() + ".html";
				li.addLink(filename, location.getName(), location.getName());
				
				sRenderMap += String.format("addMapMarker(%.6f, %.6f, \"%s\");\n", 
						location.getLongitude().doubleValue(), location.getLatitude().doubleValue(), location.getName());
				
				createLocationPage(location);
			}
		}

		main.addJavascript(sRenderMap);
		page.saveAs(htmlPath + "lieux.html");
	}
	
	/**
	 * Creates an HTML page for the specified location.
	 * <p>The page displays the location description and details,
	 * and its observations.
	 * 
	 * @param location  the location to export as HTML
	 */
	private void createLocationPage(Location location) {
		HtmlPage page = new HtmlPage("Nature &mdash; " + location.getName());
		HtmlComposite main = page.getMainDiv();
		
		main.addTitle(1, location.getName());
		
		HtmlComposite tableTop = main.addFillTable(2, "1440px");
		tableTop.setCssClass("align-top");
		HtmlComposite tdLeft = tableTop.addTableData();
		HtmlComposite tdRight = tableTop.addTableData();
		
		// OpenStreetMap
		addOpenStreetMap(location, page, tdLeft);

		// Description
		HtmlComposite divDescription = addBoxDiv(tdRight, "Description", "myBox");
		divDescription.addPar(location.getDescription());
		divDescription.addPar(location.getAltitudeLevel().getLabel() + 
				", altitude " + (location.getAltitude() >= 700 ? "moyenne " : "") + location.getAltitude() + "m");
		divDescription.addPar(location.getRegion() + ", " + location.getState());
		
		// Expeditions list
		Vector<Expedition> vecExpeditions = Controller.getInstance().getExpeditions(location);
		int nExpeditions = vecExpeditions.size();
		if (nExpeditions > 0) {
			HtmlComposite divExpeditions = addBoxDiv(tdRight, "Expéditions");
			if (nExpeditions > 8) {
				divExpeditions.addPar("Observations fréquentes depuis le " 
						+ dateFormat.format(vecExpeditions.lastElement().getDateFrom()) + ".");
			} else {
				HtmlComposite ul = divExpeditions.addList();
				for (Expedition exp : vecExpeditions) {
					String sAnchor = "#expedition" + exp.getIdx();
					HtmlComposite li = ul.addListItem();
					li.addLink("journal.html" + sAnchor, "Voir les notes de terrain", exp.getTitle());
					li.addText(" &mdash; <font color='gray'>" + dateFormat.format(exp.getDateFrom()) + 
							" (" + exp.getPics().size() + " photos)</font>");
				}
			}
		}
		
		// Neighbor locations
		HtmlComposite divNeighbors = addBoxDiv(tdRight, "Lieux à proximité");
		List<Location> listNeighbors = getClosestLocations(location, 4);
		if (listNeighbors != null && !listNeighbors.isEmpty()) {
			HtmlComposite ul = divNeighbors.addList();
			for (Location locNeighbor : listNeighbors) {
				HtmlComposite li = ul.addListItem();
				String filename = "lieu" + locNeighbor.getIdx() + ".html";
				li.addLink(filename, locNeighbor.getName(), locNeighbor.getName());
			}
		}
		
		final Set<HerbierPic> tsPics = location.getPics();
		int nPics = tsPics.size();
		
		main.addTitle(2, "Photos");
		main.addSpan("pics-count", String.valueOf(nPics) + " photo" + (nPics == 1 ? "" : "s"));
		HtmlComposite table = main.addFillTable(nColumns);
		table.setCssClass("table-thumbs");
		
		for (HerbierPic pic : location.getPics()) {
			exportPicture(pic, table);
		}
		
		String filename = "lieu" + location.getIdx() + ".html";
		page.saveAs(htmlPath + filename);
	}
	
	/**
	 * Adds a geographical map for the specified location if possible.
	 * To enable the map, the location must have valid coordinates.
	 * 
	 * @param loc   the location to display on the map
	 * @param page  the HTML page to which to add a map
	 */
	private void addOpenStreetMap(Location loc, HtmlPage page, HtmlComposite parent) {
		// Check the location has valid map coords
		Location locNullIsland = new Location(0, "Null Island");
		locNullIsland.setLongitude(0.0);
		locNullIsland.setLatitude(0.0);
		locNullIsland.setMapZoom(5);
		Double dDistance = loc.getDistance(locNullIsland);
		boolean bValidCoords = (dDistance != null && dDistance.doubleValue() > 0.1);
		
		if (bValidCoords) {
			// Add headers and a map div
			page.getHead().addScript("https://cdn.rawgit.com/openlayers/openlayers.github.io/master/en/v5.3.0/build/ol.js");
			page.getHead().addScript("js/panorpa-maps.js");
			page.getHead().addCss("https://openlayers.org/en/v5.3.0/css/ol.css");
			parent.addDiv("ol-map").setCssClass("ol-map");
			
			// Call map rendering Javascript code

			String sRenderMap = "var oVectorSource, oIconStyle;\n" +
				String.format("renderMap(%.6f, %.6f, %d);\n", 
					loc.getLongitude().doubleValue(), loc.getLatitude().doubleValue(), loc.getMapZoom()) +
				String.format("addMapMarker(%.6f, %.6f, \"%s\");", 
					loc.getLongitude().doubleValue(), loc.getLatitude().doubleValue(), loc.getName());
			page.getMainDiv().addJavascript(sRenderMap);
		} else {
			log.info("Can't add map for location " + loc + ": distance to Null Island is " + dDistance);
		}
	}
	
	/**
	 * Gets the closest locations to the specified location.
	 * Returns at most the specified number of locations.
	 * @param locFrom  the location for which to find neighbors
	 * @param iMax     the maximum number of locations to return
	 * @return  a list of locations sorted by growing distance
	 */
	private List<Location> getClosestLocations(final Location locFrom, int iMax) {
		List<Location> locations = new ArrayList<>(LocationCache.getInstance().getAll());
		locations.remove(locFrom);
		Collections.sort(locations, new Comparator<Location>() {
			@Override
			public int compare(Location loc1, Location loc2) {
				Double dDist1 = locFrom.getDistance(loc1);
				Double dDist2 = locFrom.getDistance(loc2);
				if (dDist1 != null && dDist2 != null) {
					return (dDist1.doubleValue() < dDist2.doubleValue() ? -1 : 1);
				} else if (dDist1 != null) {
					return -1;
				} else {
					return 1;
				}
			}
			
		});
		return locations.subList(0, Math.min(locations.size(), iMax));
	}

}
