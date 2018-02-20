package controller.checks;

/**
 * Enumeration of problem kinds.
 * 
 * @author nicz
 *
 */
public enum ProblemKind {
	
	PIC_TOO_LARGE("Image trop grande"),
	PIC_NO_LOCATION("Image sans lieu"),
	PIC_MISSING("Image manquante"),
	TAX_NO_FRNAME("Taxon sans nom français"),
	TAX_NO_DEFPIC("Taxon sans image"),
	LOC_NO_DESCR("Lieu sans description")
	;
	
	
	private String guiName;
	
	private ProblemKind(String guiName) {
		this.guiName = guiName;
	}

	public String getGuiName() {
		return guiName;
	}
	
}
