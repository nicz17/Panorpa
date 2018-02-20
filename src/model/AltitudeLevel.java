package model;

/**
 * Enumeration of mountain altitude levels
 * (North of the Alps).
 * 
 * @author nicz
 *
 */
public enum AltitudeLevel {
	
	HILL     (0,    "Plaine"),
	MOUNTAIN (1000, "Etage montagnard"),
	SUBALPINE(1900, "Etage subalpin"),
	ALPINE   (2500, "Etage alpin"),
	NIVAL    (2800, "Etage nival");
	
	private int minAltitude;
	private String label;
	
	private AltitudeLevel(int minAltitude, String label) {
		this.minAltitude = minAltitude;
		this.label = label;
	}

	public int getMinAltitude() {
		return minAltitude;
	}

	public String getLabel() {
		return label;
	}
	
	public static AltitudeLevel getFromAltitude(int altitude) {
		AltitudeLevel result = HILL;
		for (AltitudeLevel level : AltitudeLevel.values()) {
			if (level.getMinAltitude() < altitude) {
				result = level;
			}
		}
		return result;
	}

}
