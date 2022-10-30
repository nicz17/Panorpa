package model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import controller.GeoTrack;
import controller.LocationCache;

import view.Panorpa;

/**
 * Class representing a field trip on a single date and location.
 *
 * <p><b>Modifications:</b>
 * <ul>
 * <li>16.11.2018: nicz - Creation</li>
 * </ul>
 */
public class Expedition extends DataObject implements Comparable<Expedition> {
	
	private int idx;
	private Location location;
	private Date tFrom;
	private Date tTo;
	private String sTitle;
	private String sNotes;
	private Set<HerbierPic> vecPics;
	
	/**
	 * Constructor for non-DB expeditions
	 * @param location
	 * @param tAt
	 * @param sNotes
	 * @param vecPics
	 */
	public Expedition(Location location, Date tFrom, Date tTo, String sNotes, Set<HerbierPic> vecPics) {
		this.idx = 0;
		this.sTitle = "Nouvelle excursion";
		this.location = location;
		this.tFrom = tFrom;
		this.tTo   = tTo;
		this.sNotes = sNotes;
		this.vecPics = vecPics;
		
		if (location != null) {
			sTitle = location.getName();
		}
	}
	
	
	/**
	 * Constructor for database objects
	 * @param idx       the primary key
	 * @param location  the Location of the expedition
	 * @param tFrom     the expedition start timestamp
	 * @param tTo       the expedition end timestamp
	 * @param sTitle    the expedition title
	 * @param sNotes    the expedition notes
	 */
	public Expedition(int idx, Location location, Date tFrom, Date tTo,
			String sTitle, String sNotes) {
		this.idx = idx;
		this.location = location;
		this.tFrom = tFrom;
		this.tTo = tTo;
		this.sTitle = sTitle;
		this.sNotes = sNotes;
		this.vecPics = new HashSet<>();
	}

	/**
	 * Creates a new expedition from a GeoTrack object.
	 * 
	 * @param track  a GeoTrack object
	 * @return  the created expedition
	 */
	public static Expedition newExpedition(GeoTrack track) {
		Location loc = LocationCache.getInstance().getClosestLocation(track.getMeanPosition());
		String sDesc = track.getDesc() == null ? "" : track.getDesc();
		Expedition newObj = new Expedition(0, loc, track.getStart(), track.getEnd(), 
				track.getName(), sDesc);
		return newObj;
	}



	@Override
	public int getIdx() {
		return idx;
	}

	public Location getLocation() {
		return location;
	}

	public Date getDateFrom() {
		return tFrom;
	}

	public Date getDateTo() {
		return tTo;
	}

	public String getTitle() {
		return sTitle;
	}

	public String getNotes() {
		return sNotes;
	}

	public void setLocation(Location location) {
		this.location = location;
	}


	public void setFrom(Date tFrom) {
		this.tFrom = tFrom;
	}


	public void setTo(Date tTo) {
		this.tTo = tTo;
	}


	public void setTitle(String sTitle) {
		this.sTitle = sTitle;
	}


	public void setNotes(String sNotes) {
		this.sNotes = sNotes;
	}


	public void setPics(Set<HerbierPic> vecPics) {
		this.vecPics = vecPics;
	}


	public Set<HerbierPic> getPics() {
		return vecPics;
	}

	@Override
	public String[] getDataRow() {
		return new String[] {
				Panorpa.dateFormat.format(tFrom), 
				sTitle, 
				location == null ? "Lieu inconnu" : location.getName(), 
				Panorpa.dateFormat.format(tTo)
		};
	}
	
	@Override
	public String toString() {
		String str = "Excursion at " + 
				(location == null ? "UNDEFINED location" : location.getName()) + 
				" on " + tFrom + ", " + 
				(vecPics == null ? "-" : vecPics.size()) + " photos";
		return str;
	}

	@Override
	public int compareTo(Expedition exp) {
		return exp.getDateFrom().compareTo(getDateFrom());
	}
}
