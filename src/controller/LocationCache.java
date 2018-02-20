package controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import model.Location;

import common.base.Logger;

import controller.DatabaseTools.eOrdering;

public class LocationCache {
	
	private static final Logger log = new Logger("LocationCache", true);
	
	/** Map of Locations by database idx. */
	private final Map<Integer, Location> mapById;

	/** the singleton instance */
	private static LocationCache _instance = null;
	
	
	/**
	 * Gets a location by its database index.
	 * @param idx  the database index.
	 * @return  the location, or null if not found.
	 */
	public Location getLocation(int idx) {
		if (idx > 0) {
			return mapById.get(new Integer(idx));
		} else {
			return null;
		}
	}
	
	/** 
	 * Reloads the cache.
	 * Fetches all locations from database and fills the cache. 
	 */
	public void loadAll() {
		clear();
		Vector<Location> locations = DataAccess.getInstance().getLocations(null, eOrdering.BY_NAME, null);
		log.info("Loaded " + locations.size() + " locations");
		
		for (Location location : locations) {
			addLocation(location);
		}
	}
	
	public Collection<Location> getAll() {
		return mapById.values();
	}
	
	
	/**
	 * Refresh the cache for the specified location.
	 * @param idxLocation  the index of the location to refresh.
	 */
	public void refresh(int idxLocation) {
		if (idxLocation > 0) {
			Location location = DataAccess.getInstance().getLocation(idxLocation);
			log.info("Refreshing cache for " + location);
			addLocation(location);
		}
	}

	/**
	 * Gets the size of the cache.
	 * Size is the number of Locations with different IDs.
	 * 
	 * @return  cache size
	 */
	public int size() {
		return mapById.size();
	}

	/** 
	 * Clears the cache.
	 */
	public void clear() {
		mapById.clear();
	}
	
	/**
	 * Adds the specified Location to cache.
	 * 
	 * @param location  the Location to add
	 */
	private void addLocation(Location location) {
		if (location != null) {
			mapById.put(new Integer(location.getIdx()), location);
		}
	}

	
	/** Gets the singleton instance. */
	public static LocationCache getInstance() {
		if (_instance == null)
			_instance = new LocationCache();
		return _instance;
	}

	/** Private singleton constructor */
	private LocationCache() {
		mapById = new HashMap<>();
	}

}
