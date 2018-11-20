package controller.export;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;

import model.Taxon;

import common.base.Chronometer;
import common.base.Logger;

import controller.Controller;

/**
 * Singleton class to create website.
 * 
 * @author nicz
 *
 */
public class ExportManager {
	
	private static final Logger log = new Logger("ExportManager", true);
	
	private WebsiteExporter      websiteExporter;
	private PictureExporter      pictureExporter;
	private HomePageExporter     homePageExporter;
	private LocationExporter     locationExporter;
	private ExpeditionsExporter  expeditionsExporter;
	private HighchartsExporter   highchartsExporter;
	//private TaxonPicsExporter   taxonPicsExporter;
	//private BookExporter        bookExporter;
	
	/** the singleton instance */
	private static ExportManager _instance = null;
	
	
	public void export(Set<Taxon> taxa) {
		log.info("Exporting " + taxa.size() + " top-level taxa");
		
		if (taxa == null || taxa.isEmpty()) {
			log.warn("Nothing to export, aborting");
			return;
		}
		
		Chronometer chrono = new Chronometer();
		cleanup();
		
		websiteExporter.export(taxa);
		locationExporter.export();
		homePageExporter.export();
		pictureExporter.export();
		expeditionsExporter.export();
		highchartsExporter.export();
		//taxonPicsExporter.export();
		//bookExporter.export();
		
		chrono.stop();
		log.info("Done exporting in " + chrono.getElapsedTime() + " ms");
	}
	
	/** Gets the singleton instance. */
	public static ExportManager getInstance() {
		if (_instance == null)
			_instance = new ExportManager();
		return _instance;
	}
	
	/**
	 * Deletes local html pages.
	 */
	private void cleanup() {
		String path = Controller.htmlPath + "pages/";
		log.info("Deleting html pages from " + path);
		
		File directory = new File(path);

		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".html");
			}
		};
	    File[] files = directory.listFiles(filter);
	    log.info("Deleting " + files.length + " html pages before html export");
	    
	    for (File file : files) {
	    	file.delete();
	    }
	}
	
	/** Private singleton constructor */
	private ExportManager() {
		websiteExporter     = new WebsiteExporter();
		pictureExporter     = new PictureExporter();
		homePageExporter    = new HomePageExporter();
		locationExporter    = new LocationExporter();
		highchartsExporter  = new HighchartsExporter();
		expeditionsExporter = new ExpeditionsExporter();
		//taxonPicsExporter = new TaxonPicsExporter();
		//bookExporter      = new BookExporter();
	}

}
