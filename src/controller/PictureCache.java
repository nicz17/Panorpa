package controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import model.HerbierPic;
import model.Taxon;

import common.base.Logger;

public class PictureCache {
	
	private static final Logger log = new Logger("PictureCache", true);
	
	/** Map of pictures by database idx. */
	private final Map<Integer, HerbierPic> mapById;

	/** the singleton instance */
	private static PictureCache _instance = null;
	
	
	/**
	 * Gets a Picture by its database idx.
	 * @param idx  the database index.
	 * @return  the picture, or null if not found.
	 */
	public HerbierPic getPicture(int idx) {
		if (idx > 0) {
			return mapById.get(Integer.valueOf(idx));
		} else {
			return null;
		}
	}
	
	/** 
	 * Reloads the cache.
	 * Fetches all pictures from database and fills the cache. 
	 */
	public void loadAll() {
		clear();
		Vector<HerbierPic> pics = DataAccess.getInstance().getHerbierPics(null, null);
		log.info("Loaded " + pics.size() + " pictures");
		
		for (HerbierPic pic : pics) {
			addPic(pic);
			Taxon taxon = TaxonCache.getInstance().getTaxon(pic.getIdxTaxon());
			if (taxon != null) {
				pic.setTaxon(taxon);
				taxon.addPic(pic);
			}
		}
	}
	
	public Collection<HerbierPic> getAll() {
		return mapById.values();
	}
	

	/**
	 * Gets the size of the cache.
	 * Size is the number of pictures with different IDs.
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
	 * Adds the specified picture to cache.
	 * 
	 * @param pic  the picture to add
	 */
	private void addPic(HerbierPic pic) {
		if (pic != null) {
			mapById.put(Integer.valueOf(pic.getIdx()), pic);
		}
	}

	
	/** Gets the singleton instance. */
	public static PictureCache getInstance() {
		if (_instance == null)
			_instance = new PictureCache();
		return _instance;
	}

	/** Private singleton constructor */
	private PictureCache() {
		mapById = new HashMap<>();
	}

}
