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
	
	private void createLocationsPage() {
		HtmlPage page = new HtmlPage("Nature - Lieux");
		HtmlComposite main = page.getMainDiv();
		
		main.addTitle(1, "Lieux");
		HtmlComposite table = main.addFillTable(2, "800px");
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
				createLocationPage(location);
			}
		}

		page.saveAs(htmlPath + "lieux.html");
	}
	
	
	private void createLocationPage(Location location) {
		HtmlPage page = new HtmlPage("Nature - " + location.getName());
		HtmlComposite main = page.getMainDiv();
		
		main.addTitle(1, location.getName());
		
		HtmlComposite divDescription = addBoxDiv(main, "Description", "myBox myBox-wide");
		divDescription.addPar(location.getDescription());
		divDescription.addPar(location.getAltitudeLevel().getLabel() + 
				", altitude " + (location.getAltitude() >= 700 ? "moyenne " : "") + location.getAltitude() + "m");
		divDescription.addPar(location.getRegion() + ", " + location.getState());
		
		// Expedition list
		//Vector<Expedition> vecExpeditions = ExpeditionManager.getInstance().buildExpeditions(location);
		Vector<Expedition> vecExpeditions = Controller.getInstance().getExpeditions(location);
		int nExpeditions = vecExpeditions.size();
		if (nExpeditions > 0) {
			HtmlComposite divExpeditions = addBoxDiv(main, "Expéditions");
			if (nExpeditions > 8) {
				divExpeditions.addPar("Observations fréquentes depuis le " 
						+ dateFormat.format(vecExpeditions.lastElement().getDateFrom()) + ".");
			} else {
				HtmlComposite ul = divExpeditions.addList();
				for (Expedition exp : vecExpeditions) {
					ul.addListItem().addText(dateFormat.format(exp.getDateFrom()) + 
							" <font color='gray'>(" + exp.getPics().size() + " photos)</font>");
				}
			}
		}
		// END TEST
		
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

}
