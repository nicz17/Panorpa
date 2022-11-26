package model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Container class for records of AppParam table.
 * 
 * <p>This table contains application parameters.
 * 
 * @author nicz
 *
 */
public class AppParam extends DataObject {
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	
	/**
	 * The parameter data kind.
	 * 
	 * @author nicz
	 *
	 */
	public enum AppParamKind {
		BOOL,
		DATE,
		FLOAT,
		INT,
		STR
	}
	
	private int idx;
	private String name;
	private String description;
	private AppParamKind kind;
	
	private String strValue;
	private double numValue;
	private Date dateValue;
	

	public AppParam(int idx, String name, String description,
			AppParamKind kind, String strValue, double numValue, Date dateValue) {
		this.idx = idx;
		this.name = name;
		this.description = description;
		this.kind = kind;
		this.strValue = strValue;
		this.numValue = numValue;
		this.dateValue = dateValue;
	}
	
	
	public Integer getIntValue() {
		if (AppParamKind.INT == kind) {
			return Integer.valueOf((int) numValue);
		}
		return null;
	}
	
	public void setIntValue(int value) {
		this.numValue = value;
	}
	
	public boolean getBoolValue() {
		if (AppParamKind.BOOL == kind) {
			return (numValue != 0);
		}
		return false;
	}
	
	public void setBoolValue(boolean value) {
		this.numValue = (value ? 1 : 0);
	}
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public AppParamKind getKind() {
		return kind;
	}

	public void setKind(AppParamKind kind) {
		this.kind = kind;
	}

	public String getStrValue() {
		return strValue;
	}

	public void setStrValue(String strValue) {
		this.strValue = strValue;
	}

	public double getDoubleValue() {
		return numValue;
	}

	public void setDoubleValue(double numValue) {
		this.numValue = numValue;
	}

	public Date getDateValue() {
		return dateValue;
	}

	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}

	@Override
	public int getIdx() {
		return idx;
	}
	
	public String getValueAsString() {
		String result = null;
		
		switch (kind) {
		case BOOL:
			result = Boolean.toString(getBoolValue());
			break;
		case DATE:
			Date date = getDateValue();
			result = (date == null ? "null" : dateFormat.format(date));
			break;
		case FLOAT:
			result = String.valueOf(numValue);
			break;
		case INT:
			result = String.format("%d", (int)numValue);
			break;
		case STR:
			result = strValue;
		default:
			break;
		}
		
		return result;
	}

	@Override
	public String[] getDataRow() {
		return new String[] {name, description, kind.name()};
	}
	
	@Override
	public String toString() {
		String result = "AppParam " + name + " " + kind.name() + ": " + getValueAsString();
		return result;
	}

}
