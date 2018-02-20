package view.base;

import model.HasPhoto;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import common.view.WidgetsFactory;

/**
 * A Composite with a Label displaying an image.
 * 
 * @author nicz
 *
 */
public class PhotoBox extends Composite {
	protected Label lblPhoto;
	protected HasPhoto hasPhoto;

	/**
	 * Constructs a new photo box for the specified parent.
	 * @param parent  the parent composite
	 */
	public PhotoBox(Composite parent) {
		super(parent, 0);
		
		GridData gd = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		gd.heightHint = 500;
		gd.widthHint = 500;
		this.setLayout(new GridLayout());
		this.setLayoutData(gd);
		
		gd = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		gd.heightHint = 500;
		gd.widthHint = 500;
		lblPhoto = WidgetsFactory.getInstance().createPictureLabel(this, 500, 500);
		lblPhoto.setLayoutData(gd);
		
		hasPhoto = null;
	}
	
	/**
	 * Displays the image of the specified HasPhoto object.
	 * If that object is null, resets the display.
	 * 
	 * @param obj  the photo object to display (may be null)
	 */
	public void showObject(HasPhoto obj) {
		hasPhoto = obj;
		if (hasPhoto == null) {
			lblPhoto.setImage(ViewTools.getPhotoThumb(null));
		} else {
			lblPhoto.setImage(ViewTools.getPhotoThumb(hasPhoto.getFileName()));
		}
	}

}
