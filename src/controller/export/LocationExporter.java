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
import common.html.HtmlTag;
import common.html.HtmlTagFactory;
import common.html.JavascriptHtmlTag;
import common.html.ListHtmlTag;
import common.html.ParHtmlTag;
import common.html.TableHtmlTag;

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
		PanorpaHtmlPage page = new PanorpaHtmlPage("Nature - Lieux", htmlPath + "lieux.html", "");
		addOpenLayersHeaders(page);
		
		page.addTitle(1, "Lieux");
		HtmlTag divMap = page.addDiv("ol-map");
		divMap.setClass("ol-map");
		divMap.addTag(HtmlTagFactory.div("ol-popup"));
		
		// Call map rendering Javascript code
		final JavascriptHtmlTag jsRenderMap = new JavascriptHtmlTag();
		jsRenderMap.addLine("var oVectorSource, oIconStyle;");
		jsRenderMap.addLine("renderMap(6.3902, 46.5377, 9);");
		
		TableHtmlTag table = page.addTable(2, "800px");
		table.setClass("align-top");
		
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
		
		HashMap<AltitudeLevel, ListHtmlTag> mapLevels = new HashMap<>();
		int nLevels = 0;
		Vector<HtmlTag> vecCells = new Vector<>();
		
		for (Location location : locations) {
			if (location.getPics().isEmpty()) {
				log.warn("Skipping location without pictures: " + location);
			} else {
				AltitudeLevel level = location.getAltitudeLevel();
				
				ListHtmlTag ul = mapLevels.get(level);
				if (ul == null) {
					nLevels++;
					if (nLevels == 2) {
						table.addCell(vecCells);
						vecCells.clear();
					}
					vecCells.add(new HtmlTag("h2", level.getLabel()));
					ul = new ListHtmlTag();
					vecCells.add(ul);
					mapLevels.put(level, ul);
				}
				
				String filename = "lieu" + location.getIdx() + ".html";
				ul.addItem(HtmlTagFactory.link(filename, location.getName(), location.getName()));
				
				if (location.getLongitude() != null && location.getLatitude() != null) {
					int nPics = location.getPics().size();
					String sLabel = "<a href='" + filename + "'>" + location.getName() + "</a><br>" + nPics + " photos";
					jsRenderMap.addLine(String.format("addMapMarker(%.6f, %.6f, \"%s\");", 
						location.getLongitude().doubleValue(), location.getLatitude().doubleValue(), sLabel));
//					sRenderMap += String.format("addMapMarker(%.6f, %.6f, \"%s\", '%s');\n", 
//							location.getLongitude().doubleValue(), location.getLatitude().doubleValue(), location.getName(), filename);
				}
				
				createLocationPage(location);
			}
		}
		table.addCell(vecCells);
		
		page.add(jsRenderMap);
		page.save();
	}
	
	/**
	 * Creates an HTML page for the specified location.
	 * <p>The page displays the location description and details,
	 * and its observations.
	 * 
	 * @param location  the location to export as HTML
	 */
	private void createLocationPage(Location location) {
		String filename = "lieu" + location.getIdx() + ".html";
		PanorpaHtmlPage page = new PanorpaHtmlPage("Nature &mdash; " + location.getName(), htmlPath + filename, "");
		page.addTitle(1, location.getName());
		
		TableHtmlTag tableTop = page.addTable(2, "1440px");
		tableTop.setClass("align-top");
		HtmlTag tdLeft  = tableTop.addCell();
		HtmlTag tdRight = tableTop.addCell();

		// Description
		HtmlTag divDescription = HtmlTagFactory.blueBox("Description");
		tdRight.addTag(divDescription);
		divDescription.addTag(new ParHtmlTag(location.getDescription()));
		divDescription.addTag(new ParHtmlTag(location.getAltitudeLevel().getLabel() + 
				", altitude " + (location.getAltitude() >= 700 ? "moyenne " : "") + location.getAltitude() + "m"));
		divDescription.addTag(new ParHtmlTag(location.getRegion() + ", " + location.getState()));
		
		// Expeditions list
		Vector<Expedition> vecExpeditions = Controller.getInstance().getExpeditions(location);
		int nExpeditions = vecExpeditions.size();
		if (nExpeditions > 0) {
			HtmlTag divExpeditions = HtmlTagFactory.blueBox("Excursions");
			tdRight.addTag(divExpeditions);
			if (nExpeditions > 8) {
				divExpeditions.addTag(new ParHtmlTag("Observations fréquentes depuis le " 
						+ dateFormat.format(vecExpeditions.lastElement().getDateFrom()) + "."));
			} else {
				ListHtmlTag ul = new ListHtmlTag();
				divExpeditions.addTag(ul);
				for (Expedition exp : vecExpeditions) {
					HtmlTag li = ul.addItem();
					li.addTag(HtmlTagFactory.link("excursion" + exp.getIdx() + ".html", exp.getTitle(), "Voir les notes de terrain"));
					li.addTag(HtmlTagFactory.grayFont(" &mdash; " + dateFormat.format(exp.getDateFrom()) + 
							" (" + exp.getPics().size() + " photos)"));
				}
			}
		}
		
		// Neighbor locations
		HtmlTag divNeighbors = HtmlTagFactory.blueBox("Lieux à proximité");
		tdRight.addTag(divNeighbors);
		List<Location> listNeighbors = getClosestLocations(location, 4);
		if (listNeighbors != null && !listNeighbors.isEmpty()) {
			ListHtmlTag ul = new ListHtmlTag();
			divNeighbors.addTag(ul);
			for (Location locNeighbor : listNeighbors) {
				HtmlTag li = ul.addItem();
				String filenameLoc = "lieu" + locNeighbor.getIdx() + ".html";
				li.addTag(HtmlTagFactory.link(filenameLoc, locNeighbor.getName(), locNeighbor.getName()));
				double dDistance = getDistanceKm(location, locNeighbor);
				String sDistance = String.format(" à %.3f km", dDistance);
				if (dDistance < 1.0) {
					sDistance = String.format(" à %d m", (int)(1000.0 * dDistance));
				}
				li.addTag(HtmlTagFactory.grayFont(sDistance));
			}
		} else {
			divNeighbors.addTag(HtmlTagFactory.grayFont("Pas encore de lieux dans le voisinage."));
		}
		
		// OpenStreetMap
		addOpenStreetMap(location, page, tdLeft, listNeighbors, null, null);
		
		// Photos
		final Set<HerbierPic> tsPics = location.getPics();
		int nPics = tsPics.size();
		page.addTitle(2, "Photos");
		page.addSpan("pics-count", String.valueOf(nPics) + " photo" + (nPics == 1 ? "" : "s"));
		TableHtmlTag table = page.addTable(nColumns, "100%");
		table.setClass("table-thumbs");
		for (HerbierPic pic : tsPics) {
			exportPicture(pic, table);
		}
		
		page.save();
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
