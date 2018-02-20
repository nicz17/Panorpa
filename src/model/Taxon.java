package model;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;


/**
 * A biological taxon.
 * 
 * @author nicz
 *
 */
public class Taxon extends DataObject implements Comparable<Taxon> {
	
	private int idx;
	private String name;
	private String nameFr;
	private TaxonRank rank;
	private Taxon parent;
	private int idxParent;
	private int order;
	private boolean isTypical;
	private final Set<Taxon> children;
	private final Set<HerbierPic> pics;

	public Taxon(int idx, String name, TaxonRank rank) {
		super();
		this.idx = idx;
		this.name = name;
		this.rank = rank;
		
		// use a TreeSet to sort children by name
		this.children = new TreeSet<>();
		this.pics     = new TreeSet<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameFr() {
		return nameFr == null ? "" : nameFr;
	}

	public void setNameFr(String nameFr) {
		this.nameFr = nameFr;
	}

	public TaxonRank getRank() {
		return rank;
	}

	public void setRank(TaxonRank rank) {
		this.rank = rank;
	}

	public Taxon getParent() {
		return parent;
	}

	public void setParent(Taxon parent) {
		this.parent = parent;
	}

	public int getIdxParent() {
		return idxParent;
	}

	public void setIdxParent(int idxParent) {
		this.idxParent = idxParent;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public boolean isTypical() {
		return isTypical;
	}

	public void setTypical(boolean isTypical) {
		this.isTypical = isTypical;
	}

	/**
	 * Gets the ancestor taxon with the specified rank.
	 * 
	 * @param rank  the rank of the ancestor to return (may not be null)
	 * @return  the ancestor, or this if not found
	 */
	public Taxon getAncestor(final TaxonRank rank) {
		if (rank == null) {
			return null;
		}
		
		Taxon parent = this;
		while (parent != null && parent.getRank() != rank) {
			parent = parent.getParent();
		}
		return parent;
	}

	/**
	 * Gets the child taxa.
	 * @return a set of child taxa (may be empty, bet never null)
	 */
	public Set<Taxon> getChildren() {
		return children;
	}
	
	/**
	 * Adds the specified taxon as a child to this taxon.
	 * @param child  the taxon to add as child
	 */
	public void addChild(Taxon child) {
		if (child != null) {
			children.add(child);
		}
	}
	
	public void removeChild(int idxTaxon) {
		for (Taxon child : children) {
			if (child.getIdx() == idxTaxon) {
				children.remove(child);
				return;
			}
		}
	}
	
	/**
	 * Checks if this taxon has sub-taxa.
	 * @return true only if taxon has sub-taxa.
	 */
	public boolean hasSubTaxa() {
		return !children.isEmpty();
	}

	/**
	 * Gets the set of pictures directly linked (in database) to this taxon.
	 * @return a set of pictures (may be empty, bet never null)
	 */
	public Set<HerbierPic> getPics() {
		return pics;
	}
	
	/**
	 * Gets the set of pictures linked to this taxon and all its descendants.
	 * @return a set of pictures (may be empty, bet never null)
	 */
	public TreeSet<HerbierPic> getPicsCascade() {
		TreeSet<HerbierPic> result = new TreeSet<>();
		result.addAll(pics);
		for (Taxon child : children) {
			result.addAll(child.getPicsCascade());
		}
		return result;
	}
	
	/**
	 * Gets a picture that is considered typical for this taxon.
	 * If no such picture is defined, gets the first picture 
	 * for this taxon and its descendants.
	 * @return a picture (may be null)
	 */
	public HerbierPic getTypicalPic() {
		HerbierPic result = null;
		
		// first look for a typical child
		for (Taxon child : children) {
			if (child.isTypical) {
				result = child.getTypicalPic();
				break;
			}
		}
		
		// return the best species pic
		if (result == null && TaxonRank.SPECIES == rank) {
			result = getBestPic();
		}
		
		// else return the first available pic
		if (result == null) {
			TreeSet<HerbierPic> allPics = getPicsCascade();
			if (!allPics.isEmpty()) {
				result = allPics.first();
			}
		}
		
		return result;
	}
	
	/**
	 * Gets the highest-rated picture directly linked to this taxon.
	 * 
	 * @return  the best taxon picture, or null if no pictures.
	 */
	public HerbierPic getBestPic() {
		HerbierPic result = null;
		
		if (!pics.isEmpty()) {
			Vector<HerbierPic> vPics = new Vector<>();
			vPics.addAll(pics);
			Collections.sort(vPics, new Comparator<HerbierPic>() {
				@Override
				public int compare(HerbierPic pic1, HerbierPic pic2) {
					return pic2.getRating() - pic1.getRating();
				}
			});
			result = vPics.firstElement();
		}
		
		return result;
	}
	
	/**
	 * Adds a picture that is directly linked to this taxon.
	 * @param pic  the picture to add
	 */
	public void addPic(HerbierPic pic) {
		if (pic != null) {
			pics.add(pic);
		}
	}

	@Override
	public int getIdx() {
		return idx;
	}

	@Override
	public String[] getDataRow() {
		String strParent = (parent == null ? "" : parent.getName());
		return new String[] {String.valueOf(getIdx()), getName(), getRank().getGuiName(), 
				strParent, getNameFr()};
	}

	@Override
	public String toString() {
		return "Taxon " + idx + " " + rank.toString() + " " + getName();
	}

	@Override
	public int compareTo(Taxon taxon) {
		int result = 0;
		if (taxon == null) {
			result = -1;
		} else {
			// compare first by order, then by latin name
			result = order - taxon.getOrder();
			if (result == 0) {
				result = getName().compareTo(taxon.getName());
			}
		}
		return result;
	}

}
