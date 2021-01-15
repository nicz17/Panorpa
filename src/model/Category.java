package model;

/**
 * Enumeration of categories used to generate pie charts of observations.
 * 
 * <p>A category is just a taxon name and a chart color.
 *
 * <p><b>Modifications:</b>
 * <ul>
 * <li>05.01.2019: nicz - Creation</li>
 * </ul>
 */
public enum Category {
	
	FERNS("Filicopsida", "#00b000"),
	MONOCOTS("Liliopsida", "#40d000"),
	DICOTS("Magnoliopsida", "#80ee00"),
	BIRDS("Aves", "#4040ff"),
	MAMMALS("Mammalia", "#ee8000"),
	REPTILES("Reptilia", "#808080"),
	INSECTS("Insecta", "#ee0000"),
	ARACHNIDS("Arachnida", "#ee8000"),
	FUNGI("Fungi", "#cdba96");
	
	private String name;
	private String color;
	
	private Category(String name, String color) {
		this.name = name;
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public String getColor() {
		return color;
	}
	
	
}
