package view.base;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import model.HasPhoto;
import model.TaxonRank;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import common.view.IconManager;
import common.view.MessageBox;

import controller.Controller;

/**
 * Utility methods for view.
 * 
 * Mostly handles creation of images.
 * 
 * @author nicz
 *
 */
public class ViewTools {

	private static Display display;
	private static String photoDir  = null; 
	private static String thumbsDir = null; 

	private static Map<TaxonRank, Color> mapRankColors;

	/**
	 * Initialize the {@link Display} on which to create images.
	 * 
	 * @param display the display for image creation.
	 */
	public static void init(Display display) {
		ViewTools.display = display;
		mapRankColors = new HashMap<TaxonRank, Color>();
		
		photoDir  = Controller.picturesPath;
		thumbsDir = Controller.mediumPath;
	}
	
	/**
	 * Get the thumbnail version of an image
	 * @param picName the name of the desired thumbnail
	 * @return the thumbnail, if it exists, or the blank thumbnail
	 */
	public static Image getPhotoThumb(String picName) {
		if (picName != null) {
			File thumb = new File(thumbsDir + picName);
			if (thumb.exists()) {
				return new Image(display, thumbsDir + picName);
			}
		}
		return new Image(display, thumbsDir + "blank.jpg");
	}
	
	/**
	 * Get the full version of an image
	 * @param picName the name of the desired image
	 * @return the image, if it exists, or null
	 */
	public static Image getPhotoFull(String picName) {
		if (picName != null) {
			File thumb = new File(photoDir + picName);
			if (thumb.exists()) {
				return new Image(display, photoDir + picName);
			}
		}
		return null;
	}
	
	public static Image getOrigThumb(HasPhoto pic) {
		if (pic != null) {
			File thumb = new File(pic.getFileName());
			if (thumb.exists()) {
				return new Image(display, thumb.getAbsolutePath());
			}
		}
		return new Image(display, thumbsDir + "blank.jpg");
	}
	
	public static Display getDisplay() {
		return display;
	}

	/**
	 * Displays the specified exception in an error message box.
	 * @param exc the exception to display
	 */
	public static void displayException(Exception exc) {
		MessageBox.error(exc.getMessage());
	}
	
	public static Image getRankIcon(TaxonRank rank) {
		final int iconSize = 16;
		return IconManager.createColorIcon(iconSize, getRankColor(rank));
	}

	public static Color getRankColor(TaxonRank rank) {
		if (rank == null)
			return display.getSystemColor(SWT.COLOR_WHITE);
		
		Color color = null;
		if (mapRankColors.containsKey(rank)) {
			//log.debug("Reusing stored color for priority " + prio.name());
			color = mapRankColors.get(rank);
		} else {
			color = new Color(display, getRankRGB(rank));
			mapRankColors.put(rank, color);
		}
		return color;
	}

	private static RGB getRankRGB(TaxonRank rank) {
		RGB color = new RGB(127, 127, 127);
		switch(rank) {
		case SPECIES:
			color = new RGB(255, 88, 44);
			break;
		case GENUS:
			color = new RGB(255, 170, 88);
			break;
		case FAMILY:
			color = new RGB(255, 255, 88);
			break;
		case ORDER:
			color = new RGB(176, 255, 88);
			break;
		case CLASS:
			color = new RGB(88, 255, 247);
			break;
		case PHYLUM:
			color = new RGB(88, 99, 255);
			break;
		case KINGDOM:
			color = new RGB(176, 44, 255);
			break;
		default:
			break;
		}
		return color;
	}

}
