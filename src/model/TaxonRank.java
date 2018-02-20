package model;

import java.util.Vector;

/**
 * Enumeration of biological taxon ranks.
 * 
 * <p>Only the 7 classical ranks are included.
 * 
 * @author nicz
 *
 */
public enum TaxonRank {
	
	SPECIES("Espèce",  7, "#ff4d2d"),
	GENUS  ("Genre",   6, "#ff8f4d"),
	FAMILY ("Famille", 5, "#ffff4d"),
	ORDER  ("Ordre",   4, "#8fff4d"),
	CLASS  ("Classe",  3, "#4dffd6"),
	PHYLUM ("Phylum",  2, "#4d57ff"),
	KINGDOM("Règne",   1, "#af2dff");
	
	
	private String guiName;
	private int order;
	private String color;
	
	private TaxonRank(String guiName, int order, String color) {
		this.guiName = guiName;
		this.order   = order;
		this.color   = color;
	}
	
	/**
	 * Gets the UI display name (in French) of this rank.
	 * @return  the rank display name
	 */
	public String getGuiName() {
		return guiName;
	}
	
	/**
	 * Gets the rank order, from 1 for kingdoms to 7 for species.
	 * @return  the rank order (1 to 7)
	 */
	public int getOrder() {
		return order;
	}

	public String getColor() {
		return color;
	}

	/**
	 * Gets the parent rank of this rank.
	 * @return the parent rank (null for kingdom)
	 */
	public TaxonRank getParentRank() {
		switch(this) {
		case SPECIES: return GENUS;
		case GENUS:   return FAMILY;
		case FAMILY:  return ORDER;
		case ORDER:   return CLASS;
		case CLASS:   return PHYLUM;
		case PHYLUM:  return KINGDOM;
		default: return null;
		}
	}
	
	/**
	 * Gets the child rank of this rank.
	 * @return  the child rank (null for species).
	 */
	public TaxonRank getChildRank() {
		switch(this) {
		case GENUS:   return SPECIES;
		case FAMILY:  return GENUS;
		case ORDER:   return FAMILY;
		case CLASS:   return ORDER;
		case PHYLUM:  return CLASS;
		case KINGDOM: return PHYLUM;
		default: return null;
		}
	}
	
	/**
	 * Gets a rank from its display name.
	 * @param guiName  the rank display name
	 * @return the rank, or null if not found.
	 */
	public static TaxonRank valueFromGuiName(String guiName) {
		for (TaxonRank rank : TaxonRank.values()) {
			if (rank.getGuiName().equals(guiName)) {
				return rank;
			}
		}
		return null;
	}
	
	/**
	 * Gets the ranks as a vector.
	 * @return  ranks vector
	 */
	public static Vector<TaxonRank> getValuesVector() {
		Vector<TaxonRank> values = new Vector<>();
		for (TaxonRank rank : TaxonRank.values()) {
			values.add(rank);
		}
		return values;
	}
	
}
