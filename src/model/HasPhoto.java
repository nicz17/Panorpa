package model;


/**
 * Interface for objects that can have photos.
 * 
 * Provides methods to get the image and search for images.
 * 
 * @author nicz
 *
 */
public interface HasPhoto {
	
	/**
	 * Get the filename of the image for this object,
	 * if it exists.
	 * 
	 * @return the image file name
	 */
	public String getFileName();
	
	
	public String getName();
}
