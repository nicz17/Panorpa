package view;

import common.view.IsModule;

/**
 * Enumeration of the application modules,
 * with their class, title and icon.
 * 
 * @author nicz
 *
 */
public enum Module implements IsModule {
	
    PICS     (ModulePics.class.getName(),         "Photos",         "leaf"),
    TAXA     (ModuleTaxonsTree.class.getName(),   "Classification", "tree"),
    LOCATIONS(ModuleLocations.class.getName(),    "Lieux",          "location"),
    UPLOAD   (ModuleUpload.class.getName(),       "Upload",         "internet"),
    PROBLEMS (ModuleProblems.class.getName(),     "Status",         "evolution-tasks"),
    STATS    (ModuleStats.class.getName(),        "Graphiques",     "stats"),
//  ARCHIVE  (ModuleArchive.class.getName(),      "Archives raw",   "folder"),
    PRESELECT(ModulePreselection.class.getName(), "Préselection",   "camera"),
    EXPEDITIONS(ModuleExpeditions.class.getName(), "Expéditions",   "trekking")
    ;

	private String moduleClass;
	private String title;
	private String icon;
	
	Module(String moduleClass, String name, String icon) {
		this.moduleClass = moduleClass;
		this.title = name;
		this.icon  = icon;
	}
	
	@Override
	public String getModuleClass() {
		return moduleClass;
	}
	
	@Override
	public String getTitle() {
		return title;
	}
	
	@Override
	public String getIcon() {
		return icon;
	}

}
