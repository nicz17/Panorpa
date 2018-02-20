package view.base;

import java.util.Vector;

import model.HerbierPic;

import org.eclipse.swt.widgets.Composite;

import common.view.CyclicNavigationBar;

/**
 * A composite displaying multiple pictures, 
 * with a cyclic navigation bar to select the picture.
 * 
 * <p>Displays the picture species in the navigation bar status.
 * 
 * @author nicz
 *
 */
public class MultiPhotoBox extends LegendPhotoBox {
	
	protected CyclicNavigationBar navBar;
	
	protected Vector<HerbierPic> vecPics;
	protected String taxonName;
	protected HerbierPic selectedPic;

	/**
	 * Constructs a new multi-photo box.
	 * @param parent  the composite's parent
	 */
	public MultiPhotoBox(Composite parent) {
		super(parent);
		
		navBar = new CyclicNavigationBar(this, 0) {
			@Override
			public void onSelectionChange(int selection) {
				HerbierPic pic = null;
				if (vecPics != null && !vecPics.isEmpty()) {
					pic = vecPics.get(selection);
				}
				showObject(pic);
			}
		};
	}
	
	/**
	 * Sets the pictures to display.
	 * @param vecPics  the list of pictures to display.
	 */
	public void setPics(Vector<HerbierPic> vecPics, String taxonName) {
		this.vecPics = vecPics;
		this.taxonName = taxonName;
		navBar.reset(vecPics == null ? 0 : vecPics.size());
	}
	
	/**
	 * Gets the currently displayed picture, or null if nothing is displayed.
	 * @return the currently displayed picture
	 */
	public HerbierPic getSelectedPic() {
		HerbierPic pic = null;
		if (vecPics != null && !vecPics.isEmpty()) {
			pic = vecPics.get(navBar.getSelection());
		}
		return pic;
	}
	
	/**
	 * Displays the specified picture:
	 * <ul>
	 * <li>displays photo in image label (PhotoBox)
	 * <li>displays image info in details label
	 * <li>updates navigation bar display
	 * </ul>
	 * 
	 * @param pic  the picture to display
	 */
	protected void showObject(HerbierPic pic) {
		super.showObject(pic);
		
		if (pic != null) {
			navBar.setStatus("Photos de " + taxonName + " (" + navBar.getIofN() + ")");
		} else {
			navBar.setStatus("Sélectionner un taxon");
		}
	}
}
