package model;

public class StatItem extends DataObject {
	private String desc, value;

	public StatItem(String desc, String value) {
		this.desc = desc;
		this.value = value;
	}
	
	public StatItem(String desc, int value) {
		this.desc = desc;
		this.value = Integer.valueOf(value).toString();
	}
	
	public StatItem(String desc, float value) {
		this.desc = desc;
		this.value = Float.valueOf(value).toString();
	}
	
	public String[] getDataRow() {
		return new String[] { desc, value };
	}
	
	public final static String[] getTableHeader() {
		return new String[] {"Description", "Valeur"};
	}

	public final static double[] getColWidths() {
		return new double[] {0.5, 0.5};
	}
	
	@Override
	public String toString() {
		return "StatItem " + desc;
	}

	@Override
	public int getIdx() {
		return 0;
	}
}
