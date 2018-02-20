package model;

/**
 * Container class for a value and its description, for pie charts.
 * 
 * @author nicz
 *
 */
public class NamedValue {
	
	private final int value;
	private final String name;
	
	public NamedValue(int value, String name) {
		super();
		this.value = value;
		this.name = name;
	}

	public int getValue() {
		return value;
	}

	public String getName() {
		return name;
	}
	
	

}
