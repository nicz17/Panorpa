package model;

/**
 * Enumeration of application parameter names.
 * 
 * @author nicz
 *
 */
public enum AppParamName {
	
	/** The database index of the last taxon selected in taxon tree */
	TAXON_TREE_SEL("taxonTreeSel"),
	
	/** The timestamp of the last website upload */
	WEB_UPLOAD("websiteUpload"),
	
	/** The timestamp of the last MyBook backup */
	BACKUP_MYBOOK("backupBook"),
	
	/** The default location */
	DEFAULT_LOCATION("defLocation"),
	
	/** FTP password */
	FTP_PWD("ftpPwd");
	
	
	private String dbName;
	
	private AppParamName(String dbName) {
		this.dbName = dbName;
	}

	public String getDbName() {
		return dbName;
	}
	
	

}
