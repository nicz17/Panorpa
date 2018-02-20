package controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import model.Taxon;

import common.base.Logger;

import controller.DatabaseTools.eOrdering;

/**
 * Cache of {@link Taxon} objects to avoid frequent fetching.
 * 
 * @author nicz
 *
 */
public class TaxonCache {
	
	private static final Logger log = new Logger("TaxonCache", true);
	
	/** Map of taxons by database idx. */
	private final Map<Integer, Taxon> mapById;
	
	/** Map of taxons by name. */
	private final Map<String, Taxon> mapByName;
	
	/** Set of top-level taxons */
	private final Set<Taxon> setTopLevel;
	
	/**
	 * Gets a Taxon by its name.
	 * @param name  the taxon name
	 * @return  the taxon, or null if not found.
	 */
	public Taxon getTaxon(String name) {
		if (name != null) {
			return mapByName.get(name);
		} else {
			return null;
		}
	}
	
	/**
	 * Gets a Taxon by its database idx.
	 * @param idx  the database index.
	 * @return  the taxon, or null if not found.
	 */
	public Taxon getTaxon(int idx) {
		if (idx > 0) {
			return mapById.get(new Integer(idx));
		} else {
			return null;
		}
	}
	
	/**
	 * Refresh the cache for the specified taxon.
	 * @param idxTaxon  the index of the taxon to refresh.
	 */
	public void refresh(int idxTaxon) {
		if (idxTaxon > 0) {
			Taxon taxon = DataAccess.getInstance().getTaxon(idxTaxon);
			log.info("Refreshing cache for " + taxon);
			addTaxon(taxon);
			
			if (taxon.getIdxParent() > 0) {
				Taxon parent = getTaxon(taxon.getIdxParent());
				if (parent != null) {
					parent.removeChild(idxTaxon);
				}
			}
			setParent(taxon);
		}
	}
	
	/** 
	 * Reloads the cache.
	 * Fetches all taxons from database and fills the cache. 
	 */
	public void loadAll() {
		clear();
		Vector<Taxon> taxons = DataAccess.getInstance().getTaxons(null, eOrdering.BY_NAME, null);
		log.info("Loaded " + taxons.size() + " taxons");
		
		for (Taxon taxon : taxons) {
			addTaxon(taxon);
		}
		
		for (Taxon taxon : taxons) {
			setParent(taxon);
		}
	}
	
	public Collection<Taxon> getAll() {
		return mapById.values();
	}
	
	/**
	 * Gets top-level taxons (without parent).
	 * 
	 * @return  a set of top-level taxons.
	 */
	public Set<Taxon> getTopLevel() {
		return setTopLevel;
	}

	/**
	 * Gets the size of the cache.
	 * Size is the number of taxons with different IDs.
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
		mapByName.clear();
		setTopLevel.clear();
	}
	
	/**
	 * Adds the specified Taxon to cache.
	 * 
	 * @param taxon  the taxon to add
	 */
	private void addTaxon(Taxon taxon) {
		if (taxon != null) {
			mapById.put(new Integer(taxon.getIdx()), taxon);
			mapByName.put(taxon.getName(), taxon);
		}
	}
	
	private void setParent(Taxon taxon) {
		if (taxon.getIdxParent() > 0) {
			Taxon parent = getTaxon(taxon.getIdxParent());
			if (parent == null) {
				log.error("Could not find parent " + taxon.getIdxParent() + " of taxon " + taxon);
			} else {
				taxon.setParent(parent);
				parent.addChild(taxon);
				setTopLevel.remove(taxon);
			}
		} else {
			setTopLevel.add(taxon);
		}
	}
	
	/** the singleton instance */
	private static TaxonCache _instance = null;
	
	/** Gets the singleton instance. */
	public static TaxonCache getInstance() {
		if (_instance == null)
			_instance = new TaxonCache();
		return _instance;
	}
	
	/** Private singleton constructor */
	private TaxonCache() {
		mapById     = new HashMap<>();
		mapByName   = new HashMap<>();
		setTopLevel = new TreeSet<>();
	}

}
