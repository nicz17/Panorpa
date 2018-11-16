package model;

import java.util.Date;
import java.util.Set;

/**
 * Class representing a field expedition on a single date and location.
 *
 * <p><b>Modifications:</b>
 * <ul>
 * <li>16.11.2018: nicz - Creation</li>
 * </ul>
 */
public class Expedition implements Comparable<Expedition> {
	
	private Location location;
	private Date tAt;
	private String sNotes;
	private Set<HerbierPic> vecPics;
	
	public Expedition(Location location, Date tAt, String sNotes, Set<HerbierPic> vecPics) {
		this.location = location;
		this.tAt = tAt;
		this.sNotes = sNotes;
		this.vecPics = vecPics;
	}

	public Location getLocation() {
		return location;
	}

	public Date getDate() {
		return tAt;
	}

	public String getNotes() {
		return sNotes;
	}

	public Set<HerbierPic> getPics() {
		return vecPics;
	}
	
	@Override
	public String toString() {
		String str = "Expedition at " + location.getName() + " on " + tAt + ", " + vecPics.size() + " photos";
		return str;
	}

	@Override
	public int compareTo(Expedition exp) {
		return exp.getDate().compareTo(getDate());
	}
}
