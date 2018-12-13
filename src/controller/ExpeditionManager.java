package controller;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import model.Expedition;
import model.HerbierPic;
import model.Location;

import common.base.Logger;
import common.io.SpecialChars;

/**
 * Manages Expedition objects.
 * 
 * @author nicz
 *
 */
public class ExpeditionManager {
	
	private static final Logger log = new Logger("ExpeditionManager", true);
	
	// Map of recent expeditions by location
	private final Map<Location, Expedition> mapRecentExpeditions;
	
	// Minimum number of photos to create an Expedition object
	public static final int nMinPics = 5;
	
	/**
	 * Builds the list of expeditions for the specified location.
	 * Only dates with enough photos are kept.
	 * @param loc  the location to build for
	 * @return  a (possibly empty) list of Expeditions, sorted by date
	 */
	public Vector<Expedition> buildExpeditions(Location loc) {
		log.info("Building expeditions list for " + loc);
		
		// Get pics for location
		Set<HerbierPic> pics = loc.getPics();
		log.info("Got " + pics.size() + " photos for " + loc);
		
		// Sort pics by date without time
		Map<Date, Set<HerbierPic>> mapPicsByDate = new HashMap<>();
		for (HerbierPic pic : pics) {
			Date tNoTime = getDateWithoutTime(pic.getShotAt());
			if (!mapPicsByDate.containsKey(tNoTime)) {
				mapPicsByDate.put(tNoTime, new HashSet<HerbierPic>());
			}
			mapPicsByDate.get(tNoTime).add(pic);
		}
		
		// Build Expedition objects
		Vector<Expedition> vecResult = new Vector<>();
		log.info("Found pics on " + mapPicsByDate.size() + " different dates");
		for (Date tAt : mapPicsByDate.keySet()) {
			int nPics = mapPicsByDate.get(tAt).size();
			if (nPics < nMinPics) {
				log.info("Skipping date " + tAt + " with only " + nPics + " photos for " + loc);
			} else {
				Expedition exp = new Expedition(loc, tAt, "", mapPicsByDate.get(tAt));
				vecResult.add(exp);
			}
		}
		
		// Sort (most recent first) and return results
		Collections.sort(vecResult);
		log.info("Got " + vecResult.size() + " Expeditions for " + loc);
		if (!vecResult.isEmpty()) {
			mapRecentExpeditions.put(loc, vecResult.firstElement());
		}
		for (Expedition exp : vecResult) {
			log.debug("... " + exp);
		}
		
		return vecResult;
	}
	
	/**
	 * Gets a list of the nMax most recent expeditions (one by location).
	 * @param nMax  max number of expeditions returned
	 * @return a list of expeditions
	 */
	public List<Expedition> getRecentExpeditions(int nMax) {
		Vector<Expedition> vecExpeditions = new Vector<>();
		
		vecExpeditions.addAll(mapRecentExpeditions.values());
		Collections.sort(vecExpeditions);
		return vecExpeditions.subList(0, Math.min(vecExpeditions.size(), nMax));
	}
	
	/**
	 * Adds pictures to the specified expedition.
	 * Any picture at the location matching the expedition date-range is added.
	 * @param expedition  the expedition to update
	 */
	public void setExpeditionPics(Expedition expedition) {
		log.info("Setting pics to " + expedition);
		//log.info("Location has " + expedition.getLocation().getPics().size() + " pics");
		for (HerbierPic pic : expedition.getLocation().getPics()) {
			Date tShotAt = pic.getShotAt();
			//log.debug("... checking pic shot at " + tShotAt);
			if (!tShotAt.before(expedition.getDate()) && !tShotAt.after(expedition.getDateTo())) {
				expedition.getPics().add(pic);
				//log.debug("... adding pic " + pic);
			}
		}
		//log.info("Expedition now has " + expedition.getPics().size() + " pics");
	}
	
	public void clearRecentExpeditions() {
		mapRecentExpeditions.clear();
	}
	
	private Date getDateWithoutTime(Date tAt) {
		Calendar cal = Calendar.getInstance();
        cal.setTime(tAt);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date tResult = cal.getTime();
        //log.info("... Date with time: " + tAt + " - without time: " + tResult);
		return tResult;
	}
	
	/** the singleton instance */
	private static ExpeditionManager _instance = null;
	
	/** Gets the singleton instance. */
	public static ExpeditionManager getInstance() {
		if (_instance == null)
			_instance = new ExpeditionManager();
		return _instance;
	}
	
	/** Private singleton constructor */
	private ExpeditionManager() {
		mapRecentExpeditions = new HashMap<>();
	}
	
	/**
	 * @param args  unused
	 */
	public static void main(String[] args) {
		SpecialChars.init();
		Location loc = Controller.getInstance().getDefaultLocation();
		ExpeditionManager.getInstance().buildExpeditions(loc);
	}

}
