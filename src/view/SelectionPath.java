package view;

import java.util.Vector;

import model.Taxon;

/**
 * Simple container class to store the path used
 * to select a taxon in a tree.
 * 
 * @author nicz
 *
 */
public class SelectionPath {

	private Vector<Integer> vecIds;
	
	/**
	 * Constructor.
	 * Builds a path from the given taxon,
	 * which will be the final item.
	 * 
	 * @param taxon the taxon from which to build the path.
	 */
	public SelectionPath(Taxon taxon) {
		vecIds = new Vector<Integer>();
		addToPath(taxon);
	}
	
	/**
	 * Gets the path.
	 * 
	 * @return the path as taxon indexes.
	 */
	public Vector<Integer> getPath() {
		return vecIds;
	}

	/**
	 * Gets the number of taxa in the path.
	 * 
	 * @return the depth of the path
	 */
	public int size() {
		return vecIds.size();
	}
	
	public String serialize() {
		String result = "";
		for (Integer idx : vecIds) {
			if (idx != null)
				result += idx + " ";
		}
		return result;
	}
	
	public static SelectionPath deserialize(String serial) {
		String[] ids = serial.split(" ");
		SelectionPath path = new SelectionPath(null);
		for (String id : ids) {
			if (!id.isEmpty()) {
				Integer idx = Integer.decode(id);
				if (idx != null)
					path.vecIds.add(idx);
			}
		}
		return path;
	}
	
	public String toString() {
		return "SelectionPath [" + serialize() + "]";
	}

	private void addToPath(Taxon taxon) {
		if (taxon == null)
			return;
		
		vecIds.add(0, taxon.getIdx());
		if (taxon.getParent() != null)
			addToPath(taxon.getParent());
	}
}
