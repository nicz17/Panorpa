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
import common.data.HasMapCoordinates;
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
	
	/** The max distance for neighbour locations */
	private static final double dDistanceMax = 1.0;
	
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
		addOpenLayersHeaders(page);
		
		HtmlComposite main = page.getMainDiv();	
		main.addTitle(1, "Lieux");
		HtmlComposite divMap = main.addDiv("ol-map");
		divMap.setCssClass("ol-map");
		divMap.addDiv("ol-popup");
		
		// Call map rendering Javascript code
		String sRenderMap = "var oVectorSource, oIconStyle;\n" +
			"renderMap(6.3902, 46.5377, 9);\n";
		
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
				
				if (location.getLongitude() != null && location.getLatitude() != null) {
					int nPics = location.getPics().size();
					String sLabel = "<a href='" + filename + "'>" + location.getName() + "</a><br>" + nPics + " photos";
					sRenderMap += String.format("addMapMarker(%.6f, %.6f, \"%s\");\n", 
						location.getLongitude().doubleValue(), location.getLatitude().doubleValue(), sLabel);
//					sRenderMap += String.format("addMapMarker(%.6f, %.6f, \"%s\", '%s');\n", 
//							location.getLongitude().doubleValue(), location.getLatitude().doubleValue(), location.getName(), filename);
				}
				
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
			HtmlComposite divExpeditions = addBoxDiv(tdRight, "Excursions");
			if (nExpeditions > 8) {
				divExpeditions.addPar("Observations fréquentes depuis le " 
						+ dateFormat.format(vecExpeditions.lastElement().getDateFrom()) + ".");
			} else {
				HtmlComposite ul = divExpeditions.addList();
				for (Expedition exp : vecExpeditions) {
					HtmlComposite li = ul.addListItem();
					li.addLink("excursion" + exp.getIdx() + ".html", "Voir les notes de terrain", exp.getTitle());
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
				double dDistance = getDistanceKm(location, locNeighbor);
				String sDistance = String.format(" à %.3f km", dDistance);
				if (dDistance < 1.0) {
					sDistance = String.format(" à %d m", (int)(1000.0 * dDistance));
				}
				li.addText(sDistance);
			}
		} else {
			divNeighbors.addPar("<font color='grey'>Pas encore de lieux dans le voisinage.</font>");
		}
		
		// OpenStreetMap
		addOpenStreetMap(location, page, tdLeft, listNeighbors, null);
		
		final Set<HerbierPic> tsPics = location.getPics();
		int nPics = tsPics.size();
		
		main.addTitle(2, "Photos");
		main.addSpan("pics-count", String.valueOf(nPics) + " photo" + (nPics == 1 ? "" : "s"));
		HtmlComposite table = main.addFillTable(nColumns);
		table.setCssClass("table-thumbs");
		
		for (HerbierPic pic : tsPics) {
			exportPicture(pic, table);
		}
		
		String filename = "lieu" + location.getIdx() + ".html";
		page.saveAs(htmlPath + filename);
	}
	
	/**
	 * Gets the closest locations to the specified location.
	 * Returns at most the specified number of locations.
	 * @param locFrom  the location for which to find neighbors
	 * @param iMax     the maximum number of locations to return
	 * @return  a list of locations sorted by growing distance
	 */
	private List<Location> getClosestLocations(final Location locFrom, int iMax) {
		// Get all locations from cache, remove locFrom
		List<Location> locations = new ArrayList<>(LocationCache.getInstance().getAll());
		locations.remove(locFrom);
		
		// Keep only close neighbors
		List<Location> listNeighbors = new ArrayList<>();
		for (Location loc : locations) {
			Double dDistance = locFrom.getDistance(loc);
			if (dDistance != null && dDistance.doubleValue() < dDistanceMax) {
				listNeighbors.add(loc);
			}
		}
		
		// Sort by distance, closest first
		Collections.sort(listNeighbors, new Comparator<Location>() {
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
		return listNeighbors.subList(0, Math.min(listNeighbors.size(), iMax));
	}
	
	private double getDistanceKm(HasMapCoordinates hmc1, HasMapCoordinates hmc2) {
		final double r = 6371; // Radius of the earth in km
		double lat1 = hmc1.getLatitude().doubleValue();
		double lat2 = hmc2.getLatitude().doubleValue();
		double dLat = deg2rad(lat2 - lat1);
		double dLon = deg2rad(hmc2.getLongitude().doubleValue() - hmc1.getLongitude().doubleValue()); 
		double a = 
				Math.sin(dLat/2) * Math.sin(dLat/2) +
				Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * 
				Math.sin(dLon/2) * Math.sin(dLon/2); 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		double d = r * c; // Distance in km
		return d;
	}

	private double deg2rad(double deg) {
		return deg * (Math.PI/180.0);
	}

}
