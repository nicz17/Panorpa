package model;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import common.data.HasMapCoordinates;
import controller.GeoTrack;

/**
 * A geographical location
 * 
 * @author nicz
 *
 */
public class Location extends DataObject implements HasMapCoordinates, Comparable<Location> {
	
	private int idx;
	private String name;
	private String description;
	private String kind;
	private String town;
	private String region;
	private String state;
	private int altitude;
	private int iMapZoom;
	private Double dLongitude;
	private Double dLatitude;
	
	private AltitudeLevel level;
	private Date dateFirstPic;
	
	private final Set<HerbierPic> pics;

	public Location(int idx, String name) {
		this.idx = idx;
		this.name = name;
		this.altitude = 0;
		this.iMapZoom = 14;
		this.dLongitude = null;
		this.dLatitude  = null;
		
		this.pics = new TreeSet<>();
		this.dateFirstPic = null;
	}
	
	/**
	 * Creates a new default location with the specified name.
	 * 
	 * @param name  the location name
	 * @return  the created location
	 */
	public static Location newLocation(String name) {
		Location newObj = new Location(0, name);
		newObj.setAltitude(500);
		newObj.setState("Suisse");
		newObj.setRegion("Vaud");
		newObj.setLongitude(Double.valueOf(6.7));
		newObj.setLatitude(Double.valueOf(46.5));
		return newObj;
	}

	/**
	 * Creates a new location from the GeoTrack object.
	 * 
	 * @param track  a GeoTrack object
	 * @return  the created location
	 */
	public static Location newLocation(GeoTrack track) {
		TrackPoint tp = track.getMeanPosition();
		Location newObj = new Location(0, track.getName());
		newObj.setDescription(track.getDesc());
		newObj.setState("Suisse");
		newObj.setRegion("Vaud");
		newObj.setKind("GeoTrack");
		if (tp != null) {
			newObj.setAltitude((int)tp.getAltitude());
			newObj.setLongitude(tp.getLongitude());
			newObj.setLatitude(tp.getLatitude());
			newObj.setMapZoom(17);
		}
		return newObj;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description == null ? "" : description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getKind() {
		return kind == null ? "" : kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getTown() {
		return town == null ? "" : town;
	}

	public void setTown(String town) {
		this.town = town;
	}

	public String getRegion() {
		return region == null ? "" : region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getState() {
		return state == null ? "" : state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public int getAltitude() {
		return altitude;
	}

	public void setAltitude(int altitude) {
		this.altitude = altitude;
		this.level = AltitudeLevel.getFromAltitude(altitude);
	}
	
	@Override
	public int getMapZoom() {
		return iMapZoom;
	}

	public void setMapZoom(int iMapZoom) {
		this.iMapZoom = iMapZoom;
	}

	@Override
	public Double getLongitude() {
		return dLongitude;
	}

	public void setLongitude(Double dLongitude) {
		this.dLongitude = dLongitude;
	}

	@Override
	public Double getLatitude() {
		return dLatitude;
	}

	public void setLatitude(Double dLatitude) {
		this.dLatitude = dLatitude;
	}

	public AltitudeLevel getAltitudeLevel() {
		return level;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	@Override
	public int getIdx() {
		return idx;
	}
	
	
	/**
	 * Adds a picture that was taken at this location.
	 * @param pic  the picture to add
	 */
	public void addPic(HerbierPic pic) {
		if (pic != null) {
			pics.add(pic);
		}
	}

	/**
	 * Gets the set of pictures taken at this location.
	 * @return a set of pictures (may be empty, bet never null)
	 */
	public Set<HerbierPic> getPics() {
		return pics;
	}
	
	/**
	 * Gets the date the first picture was taken at this location.
	 * @return date of first pic for this location
	 */
	public Date getDateFirstPic() {
		return dateFirstPic;
	}

	public void computeDateFirstPic() {
		if (!pics.isEmpty()) {
			Vector<HerbierPic> vecPics = new Vector<>();
			vecPics.addAll(pics);
			Collections.sort(vecPics, new Comparator<HerbierPic>() {
				@Override
				public int compare(HerbierPic pic1, HerbierPic pic2) {
					return pic1.getShotAt().compareTo(pic2.getShotAt());
				}
			});
			dateFirstPic = vecPics.firstElement().getShotAt();
		}
	}

	@Override
	public String[] getDataRow() {
		return new String[] {name, town, region, kind, String.valueOf(altitude)};
	}
	
	@Override
	public String toString() {
		String result = "Location " + name;
		return result;
	}

	@Override
	public int compareTo(Location loc) {
		return name.compareTo(loc.getName());
	}

	@Override
	public Double getDistance(HasMapCoordinates objTo) {
		if (getLongitude() != null && getLatitude() != null && 
			objTo != null && objTo.getLongitude() != null && objTo.getLatitude() != null) {
			double dx = (getLongitude().doubleValue() - objTo.getLongitude().doubleValue());
			double dy = (getLatitude().doubleValue()  - objTo.getLatitude().doubleValue());
			double ds = Math.sqrt(dx*dx + dy*dy);
			return ds;
		}
		return null;
	}
}
