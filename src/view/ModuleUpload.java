package view;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Vector;

import model.AppParam;
import model.AppParamName;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import view.base.ViewTools;

import common.exceptions.AppException;
import common.text.ElapsedTimeDateFormat;
import common.view.ProgressBox;
import common.view.ProgressTimeBox;
import common.view.SashModule;

import controller.BackupManager;
import controller.Controller;
import controller.listeners.DataListener;
import controller.upload.UploadManager;

public class ModuleUpload extends SashModule implements DataListener {
	
	private static final DateFormat dateFormat = new ElapsedTimeDateFormat();
	
	private Label lblLastUpload;
	private Label lblPicsToUpload;
	private Button btnExport;
	private Button btnUpload;
	private Button btnUploadModified;
	private Button btnOpenBrowser;
	private ProgressBox progressBox;
	
	private Label lblLastBackup;
	private Label lblPicsToBackup;
	private Button btnBackup;
	
	private Button btnCheckCleanup;
	private Button btnCleanup;
	private List listCleanup;


	public ModuleUpload() {
		super(Panorpa.getInstance().getFolder(), 600);
	}

	@Override
	protected void loadWidgets() {
		Group gWebsite = widgetsFactory.createGroup(cLeft, "Site web");
		Group gBackup  = widgetsFactory.createGroup(cLeft, "Backup MyBook");
		Group gCleanup = widgetsFactory.createGroup(cRight, "Nettoyage du serveur FTP");
		
		progressBox = new ProgressTimeBox(cLeft, 100);
		
		widgetsFactory.createLabel(gWebsite, "Dernier upload :");
		lblLastUpload = widgetsFactory.createLabel(gWebsite);
		
		widgetsFactory.createLabel(gWebsite, "Photos à télécharger :");
		lblPicsToUpload = widgetsFactory.createLabel(gWebsite);
		
		btnExport = widgetsFactory.createPushButton(gWebsite, "Exporter site web", "internet", 
				"Exporter site web en local", false, new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						exportToHtml();
					}
				});

		btnUpload = widgetsFactory.createPushButton(gWebsite, "Publier tout", "go-up", 
				"Publier tout le site web", false, new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						upload(false);
					}
				});

		btnUploadModified = widgetsFactory.createPushButton(gWebsite, "Publier les changements", "go-up", 
				"Publier les changements du site web", false, new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						upload(true);
					}
				});
		
		btnOpenBrowser = widgetsFactory.createPushButton(gWebsite, "Ouvrir dans Chrome", "chrome", 
				"Voir le site généré dans un navigateur", false, new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						openInBrowser();
					}
				});

		widgetsFactory.createLabel(gBackup, "Dernier backup :");
		lblLastBackup = widgetsFactory.createLabel(gBackup);
		
		widgetsFactory.createLabel(gBackup, "Photos à sauvegarder :");
		lblPicsToBackup = widgetsFactory.createLabel(gBackup);
		
		btnBackup = widgetsFactory.createPushButton(gBackup, "Backup", "db2", 
				"Effectuer un backup des nouvelles photos", false, new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						backup();
					}
				});
		
		btnCheckCleanup = widgetsFactory.createPushButton(gCleanup, "Vérifier", "system-search", 
				"Vérifier les fichiers à nettoyer", false, new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						checkCleanup();
					}
				});
		listCleanup = widgetsFactory.createList(gCleanup, 500, 800);
		
		btnCleanup = widgetsFactory.createPushButton(gCleanup, "Effacer", "clear", 
				"Effacer les fichiers à nettoyer", false, new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						cleanup();
					}
				});

	}

	@Override
	protected void loadData() {
		// Website info
		AppParam apUpload = Controller.getInstance().getAppParam(AppParamName.WEB_UPLOAD);
		Date tLastUpload = apUpload.getDateValue();
		if (tLastUpload != null) {
			lblLastUpload.setText(dateFormat.format(tLastUpload));
		} else {
			lblLastUpload.setText("Jamais");
		}
		
		int nPicsToUpload = Controller.getInstance().getPicsToUpload().size();
		lblPicsToUpload.setText(nPicsToUpload == 0 ? "Aucune photo" : String.valueOf(nPicsToUpload) + " photos");
		
		
		// Backup info
		AppParam apBackup = Controller.getInstance().getAppParam(AppParamName.BACKUP_MYBOOK);
		Date tLastBackup = apBackup.getDateValue();
		if (tLastBackup != null) {
			lblLastBackup.setText(dateFormat.format(tLastBackup));
		} else {
			lblLastBackup.setText("Jamais");
		}
		
		int nPicsToBackup = Controller.getInstance().getPicsToBackup().size();
		lblPicsToBackup.setText(nPicsToBackup == 0 ? "Aucune photo" : String.valueOf(nPicsToBackup) + " photos");
		
		btnBackup.setEnabled(nPicsToBackup > 0);
	}

	@Override
	public void pictureUpdated(int idx) {
		loadData();
	}

	@Override
	public void taxonUpdated(int idx) {
		
	}

	@Override
	public void locationUpdated(int idx) {
		
	}
	
	@Override
	public void expeditionUpdated(int idx) {
	}
	
	private void backup() {
		enableButtons(false);
		try {
			BackupManager.getInstance().backupPictures();
		} catch (AppException e) {
			ViewTools.displayException(e);
		} finally {
			enableButtons(true);
			loadData();
		}
	}
	
	private void exportToHtml() {
		enableButtons(false);
		Runnable runExport = new Runnable() {
			public void run() {
				Controller.getInstance().exportToHtml();
			}
		};
		BusyIndicator.showWhile(getDisplay(), runExport);
		enableButtons(true);
		loadData();
	}
	
	private void upload(final boolean bOnlyModified) {
		enableButtons(false);
		Runnable runUpload = new Runnable() {
			public void run() {
				Controller.getInstance().uploadWebsite(progressBox, bOnlyModified);
			}
		};
		BusyIndicator.showWhile(getDisplay(), runUpload);
		enableButtons(true);
		loadData();
	}
	
	private void openInBrowser() {
		String url = "file://" + Controller.exportPath + "html/index.html";
		try {
			Runtime.getRuntime().exec(new String[] {"google-chrome-stable", url});
		} catch (IOException e) {
			ViewTools.displayException(e);
		}
	}
	
	private void checkCleanup() {
		enableButtons(false);
		Runnable runUpload = new Runnable() {
			public void run() {
				Vector<String> vecFiles = UploadManager.getInstance().checkFilesToCleanup();
				if (vecFiles != null) {
					listCleanup.removeAll();
					if (vecFiles.isEmpty()) {
						listCleanup.add("Aucun fichier à nettoyer.");
					} else {
						for (String filename : vecFiles) {
							listCleanup.add(filename);
						}
					}
				}
			}
		};
		BusyIndicator.showWhile(getDisplay(), runUpload);
		enableButtons(true);
	}

	private void cleanup() {
		enableButtons(false);
		Runnable runUpload = new Runnable() {
			public void run() {
				int nFiles = UploadManager.getInstance().cleanup();
				listCleanup.removeAll();
				if (nFiles > 0) {
					listCleanup.add(String.valueOf(nFiles) + " fichiers effacés.");
				} else {
					listCleanup.add("Aucun fichier effacé.");
				}
			}
		};
		BusyIndicator.showWhile(getDisplay(), runUpload);
		enableButtons(true);
	}

	private void enableButtons(boolean enabled) {
		btnExport.setEnabled(enabled);
		btnExport.update();
		btnUpload.setEnabled(enabled);
		btnUpload.update();
		btnUploadModified.setEnabled(enabled);
		btnUploadModified.update();
		btnOpenBrowser.setEnabled(enabled);
		btnOpenBrowser.update();
		btnBackup.setEnabled(enabled);
		btnBackup.update();
		btnCheckCleanup.setEnabled(enabled);
		btnCheckCleanup.update();
		btnCleanup.setEnabled(enabled);
		btnCleanup.update();
	}


}
