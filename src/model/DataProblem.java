package model;

import controller.checks.ProblemKind;

/**
 * Container class describing a problem that was detected in data quality checks.
 * 
 * <p>A problem is not a critical error, but something that should be fixed with 
 * low priority to increase overall quality.
 * 
 * @author nicz
 *
 */
public class DataProblem extends DataObject {

	private int idx;
	private ProblemKind kind;
	private String description;
	
	public DataProblem(int idx, ProblemKind kind, String description) {
		super();
		this.idx = idx;
		this.kind = kind;
		this.description = description;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public ProblemKind getKind() {
		return kind;
	}

	public void setKind(ProblemKind kind) {
		this.kind = kind;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	
	public String[] getDataRow() {
		return new String[] {kind == null ? "(inconnu)" : kind.getGuiName(), description};
	}

	@Override
	public String toString() {
		String str = "DataProblem " + 
				(kind == null ? "unknown kind" : kind.getGuiName()) + ": " +
				description;
		return str;
	}
}
