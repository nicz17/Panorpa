package view.base;

import model.HasPhoto;
import model.HerbierPic;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;

import view.Panorpa;
import view.Module;

import common.view.WidgetsFactory;

/**
 * A composite displaying a picture,
 * with a legend displaying info about the picture
 * and navigation links to the other modules.
 * 
 * @author nicz
 *
 */
public class LegendPhotoBox extends PhotoBox {
	
	protected Link lnkPicture;
	protected Link lnkLocation;

	/**
	 * Constructs a new multi-photo box.
	 * @param parent  the composite's parent
	 */
	public LegendPhotoBox(Composite parent) {
		super(parent);
		
		GridData gd = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		gd.heightHint = 700;
		gd.widthHint = 500;
		this.setLayoutData(gd);
		
		lnkPicture = WidgetsFactory.getInstance().createLink(this, "", "Editer cette photo", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				HerbierPic pic = (HerbierPic) hasPhoto;
				
				if (pic != null) {
					Panorpa.getInstance().navigate(Module.PICS, pic.getIdx());
				}
			}
		});
		
		gd = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		gd.widthHint = 500;
		lnkPicture.setLayoutData(gd);
		
		lnkLocation = WidgetsFactory.getInstance().createLink(this, "", "Editer ce lieu", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				HerbierPic pic = (HerbierPic) hasPhoto;
				
				if (pic != null && pic.getLocation() != null) {
					Panorpa.getInstance().navigate(Module.LOCATIONS, pic.getLocation().getIdx());
				}
			}
		});
		
		gd = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		gd.widthHint = 500;
		lnkLocation.setLayoutData(gd);
	}
	
	/**
	 * Displays the specified picture:
	 * <ul>
	 * <li>displays photo in image label (PhotoBox)
	 * <li>displays image info in details label
	 * </ul>
	 * 
	 * @param photo  the picture to display
	 */
	@Override
	public void showObject(HasPhoto photo) {
		super.showObject(photo);
		
		HerbierPic pic = (HerbierPic)photo;
		if (pic != null) {
			String details = "<a>" + pic.getName() + "</a> (" + pic.getFrenchName() + ")";
			String locDate = "";
			if (pic.getLocation() != null) {
				locDate += "<a>" + pic.getLocation().getName() + "</a>, ";
			}
			locDate += Panorpa.dateFormat.format(pic.getShotAt());
			
			lnkPicture.setText(details);
			lnkPicture.setToolTipText("Editer cette photo : " + details);
			lnkLocation.setText(locDate);
			lnkLocation.setToolTipText("Editer ce lieu");
			lnkPicture.pack();
			lnkLocation.pack();
		} else {
			lnkPicture.setText("");
			lnkPicture.setToolTipText(null);
			lnkLocation.setText("");
			lnkLocation.setToolTipText(null);
		}
	}
}
