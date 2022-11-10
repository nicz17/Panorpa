package model;

import java.util.Date;

import common.data.HasMapCoordinates;

/**
 * Container for 4D point: lat, lon, altitude, timestamp.
 *
 * <p><b>Modifications:</b>
 * <ul>
 * <li>11.01.2021: nicz - Creation</li>
 * </ul>
 */
public class TrackPoint implements HasMapCoordinates {
	private double dLat;
	private double dLon;
	private double dAlt;
	private Date tAt;
	
	public TrackPoint(double dLat, double dLon, double dAlt, Date tAt) {
		this.dLat = dLat;
		this.dLon = dLon;
		this.dAlt = dAlt;
		this.tAt = tAt;
	}

	@Override
	public Double getLatitude() {
		return dLat;
	}

	@Override
	public Double getLongitude() {
		return dLon;
	}

	public double getAltitude() {
		return dAlt;
	}

	public Date getTime() {
		return tAt;
	}

	@Override
	public int getMapZoom() {
		return 0;
	}

	@Override
	public Double getDistance(HasMapCoordinates objTo) {
		return null;
	}
	
	
}
