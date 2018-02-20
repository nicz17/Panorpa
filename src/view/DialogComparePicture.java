package view;

import java.io.IOException;
import java.util.Vector;

import model.HasPhoto;
import model.HerbierPic;
import model.Taxon;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import view.base.MultiPhotoBox;
import view.base.PhotoBox;
import view.base.ViewTools;

import common.view.WidgetsFactory;

import controller.Controller;

/**
 * A dialog window used to compare a picture with pictures from a specified taxon.
 * 
 * @author nicz
 *
 */
public class DialogComparePicture {
	
	private static final WidgetsFactory widgetsFactory =
			WidgetsFactory.getInstance();

	private final HasPhoto pic;
	private final Taxon taxon;
	
	private Shell  parent;
	private MultiPhotoBox photoBoxTaxon;
	private PhotoBox      photoBoxPic;
	private Button btnOpenBrowser;
	
	public DialogComparePicture(Shell parent, HasPhoto pic, Taxon taxon) {
		this.parent = parent;
		this.pic = pic;
		this.taxon = taxon;
	}
	
	public void open() {
		Display display = parent.getDisplay();
		final Shell shell =
			new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
		shell.setText("Comparer " + pic.getName() + " et " + taxon.getName());
		shell.setLayout(new GridLayout(2, false));

		photoBoxPic = new PhotoBox(shell) {
			@Override
			public void showObject(HasPhoto obj) {
				hasPhoto = obj;
				if (hasPhoto == null) {
					lblPhoto.setImage(ViewTools.getPhotoThumb(null));
				} else {
					lblPhoto.setImage(ViewTools.getOrigThumb(hasPhoto));
				}
			}
		};
		GridData data = new GridData();
		data.verticalAlignment = SWT.TOP;
		data.verticalIndent = 51;
		photoBoxPic.setLayoutData(data);
		photoBoxTaxon = new MultiPhotoBox(shell);
				
		Composite cButtons = widgetsFactory.createComposite(shell, 3, true, 12);
		
		widgetsFactory.createCloseButton(cButtons, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.dispose();
			}
		});
		
		btnOpenBrowser = widgetsFactory.createPushButton(cButtons, "Ouvrir dans Chrome", "chrome", 
				"Voir ce taxon dans un navigateur", false, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				displaySelectionInBrowser();
			}
		});

		showObject(pic);

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
	}

	private void showObject(HasPhoto pic) {
		if (taxon != null) {
			photoBoxTaxon.setPics(new Vector<HerbierPic>(taxon.getPicsCascade()), taxon.getName());
		} else {
			photoBoxTaxon.setPics(null, null);
		}
		
		photoBoxPic.showObject(pic);
		
		enableButtons();
	}
	
	private void displaySelectionInBrowser() {
		HerbierPic selPic = photoBoxTaxon.getSelectedPic();
		if (selPic != null) {
			String htmlName = selPic.getTaxon().getName().toLowerCase().replace(" ", "-");
			String url = "file://" + Controller.exportPath + "html/pages/" + htmlName + ".html";
			try {
				Runtime.getRuntime().exec(new String[] {"google-chrome-stable", url});
			} catch (IOException e) {
				ViewTools.displayException(e);
			}
		}
	}
	
	private void enableButtons() {
		btnOpenBrowser.setEnabled(photoBoxTaxon.getSelectedPic() != null);
	}

}
