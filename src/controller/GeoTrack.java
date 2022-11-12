package controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import common.base.Logger;
import model.TrackPoint;

/**
 * Container to store track points (latitude, longitude, timestamp)
 * and find the closest track point, in time, to a picture taken without GPS.
 * 
 * The goal is to add GPS coordinates to EXIF tags.
 *
 * <p><b>Modifications:</b>
 * <ul>
 * <li>11.01.2021: nicz - Creation</li>
 * </ul>
 */
public class GeoTrack {

	private static final Logger log = new Logger("GeoTrack", true);
	private static final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	private static final int iTolerance = 180*1000;
	private LinkedHashMap<Date, TrackPoint> mapTrack;
	private String sName;
	private String sDesc;
	private Date tStart;
	private Date tEnd;
	private int iOffset;
	
	public GeoTrack() {
		mapTrack = new LinkedHashMap<>();
		iOffset = 0;
	}
	
	public String getName() {
		return sName;
	}

	public void setName(String sName) {
		this.sName = sName;
	}

	public String getDesc() {
		return sDesc;
	}

	public void setDesc(String sDesc) {
		this.sDesc = sDesc;
	}

	public Date getStart() {
		return tStart;
	}

	public Date getEnd() {
		return tEnd;
	}

	public void addTrackPoint(TrackPoint tp) {
		if (tp.getTime() != null) {
			mapTrack.put(tp.getTime(), tp);
			
			if (tStart == null || tp.getTime().before(tStart)) {
				tStart = tp.getTime();
			}
			if (tEnd == null || tp.getTime().after(tEnd)) {
				tEnd = tp.getTime();
			}
		}
	}
	
	public TrackPoint findClosestPoint(Date tFrom) {
		TrackPoint result = null;
		
		if (tFrom == null) {
			return null;
		}
		
		// Add offset
		Date tOffset = new Date(tFrom.getTime() - iOffset);
		
		if (tOffset.before(tStart) || tOffset.after(tEnd)) {
			return null;
		}
		
		long iMinDiff = iTolerance;
		for (Date tAt : mapTrack.keySet()) {
			long iDiff = Math.abs(tOffset.getTime() - tAt.getTime());
			if (iDiff < iMinDiff) {
				iMinDiff = iDiff;
				result = mapTrack.get(tAt);
			}
			/*
			// We assume timestamps are in increasing order ! WRONG
			if (Math.abs(tOffset.getTime() - tAt.getTime()) < iTolerance) {
				result = mapTrack.get(tAt);
				break;
			//} else {
			//	break;
			} */
		}
		
		return result;
	}
	
	public TrackPoint getMeanPosition() {
		TrackPoint result = null;
		if (!mapTrack.isEmpty()) {
			double dLatMean = 0;
			double dLonMean = 0;
			double dAltMean = 0;
			for (Date tAt : mapTrack.keySet()) {
				TrackPoint tp = mapTrack.get(tAt);
				dLatMean += tp.getLatitude();
				dLonMean += tp.getLongitude();
				dAltMean += tp.getAltitude();
			}
			double dSize = (double)mapTrack.size();
			dLatMean /= dSize;
			dLonMean /= dSize;
			dAltMean /= dSize;
			result = new TrackPoint(dLatMean, dLonMean, dAltMean, null);
		}
		return result;
	}
	
	/**
	 * Adds an offset in milliseconds to the photo timestamps
	 * @param iOffset  offset in milliseconds
	 */
	public void setOffset(int iOffset) {
		this.iOffset = iOffset;
	}
	
	public String getDescription() {
		String str = "GeoTrack " + sName + "\n\n";
		if (sDesc != null) {
			str += sDesc + "\n\n";
		}
		str += mapTrack.size() + " positions ";
		str += "de \n" + dateFormat.format(tStart);
		str += " à \n" + dateFormat.format(tEnd);
		return str;
	}
	
	public void dump() {
		for (Date tAt : mapTrack.keySet()) {
			log.info("... " + dateFormat.format(tAt));
		}
	}
	
	@Override
	public String toString() {
		String str = "GeoTrack with " + mapTrack.size() + " points";
		str += " from " + dateFormat.format(tStart) + " to " + dateFormat.format(tEnd);
		return str;
	}
}
