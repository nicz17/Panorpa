package controller.listeners;

import model.HerbierPic;
import model.Location;
import model.Taxon;

/**
 * Listener for changes to data objects.
 * 
 * @author nicz
 *
 */
public interface DataListener {
	
	/**
	 * A {@link HerbierPic} was updated.
	 * @param idx the index of the updated picture
	 */
	public void pictureUpdated(int idx);
	
	/**
	 * A {@link Taxon} was updated.
	 * @param idx the index of the updated taxon
	 */
	public void taxonUpdated(int idx);
	
	/**
	 * A {@link Location} was updated.
	 * @param idx the index of the updated location
	 */
	public void locationUpdated(int idx);
	
}
