package controller;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import model.OriginalPic;
import model.TrackPoint;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import common.base.Logger;
import common.exceptions.AppException;

/**
 * Helper class to add GPS data to JPG files, using the mobile GeoTracker app.
 *
 * <p><b>Modifications:</b>
 * <ul>
 * <li>10.01.2021: nicz - Creation</li>
 * </ul>
 */
public class GeoTracker {
	
	private static final Logger log = new Logger("GeoTracker", true);
	private static final DateFormat dateFormatTrack = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	private static final DateFormat dateFormatLog   = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	
	/** The singleton instance */
	private static GeoTracker instance;
	
	/**
	 * Reads GPS data from specified XML file
	 * @param fileGeoTrack geoTracker .gpx file (XML)
	 */
	public GeoTrack readGeoData(File fileGeoTrack) {
		log.info("Reading GeoTracker data from file " + fileGeoTrack.getAbsolutePath());
		GeoTrack geoTrack = new GeoTrack();
		
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder documentBuilder;
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(fileGeoTrack);
			
			// Get track name and description
			String sName = "Unknown track name";
			String sDesc = null;
			NodeList nlTrack = document.getElementsByTagName("trk");
			for (int iNode = 0; iNode < nlTrack.item(0).getChildNodes().getLength(); ++iNode) {
				Node node = nlTrack.item(0).getChildNodes().item(iNode);
				if ("name".equals(node.getNodeName())) {
					sName = node.getTextContent();
				} else if ("desc".equals(node.getNodeName())) {
					sDesc = node.getTextContent();
				}
			}
			//log.info("Track name is " + sName);
			geoTrack.setName(sName);
			geoTrack.setDesc(sDesc);
			
			// Iterate on track points
			NodeList trackPoints = document.getElementsByTagName("trkpt");
			log.info("Track length is " + trackPoints.getLength());
			for (int iTrackPoint = 0; iTrackPoint < trackPoints.getLength(); ++iTrackPoint) {
				Node trackPoint = trackPoints.item(iTrackPoint);
				String sLat = trackPoint.getAttributes().getNamedItem("lat").getTextContent();
				String sLon = trackPoint.getAttributes().getNamedItem("lon").getTextContent();
				String sTime = null;
				String sEle = null;
				for (int iNode = 0; iNode < trackPoint.getChildNodes().getLength(); ++iNode) {
					Node node = trackPoint.getChildNodes().item(iNode);
					if ("time".equals(node.getNodeName())) {
						sTime = node.getTextContent();
					}
					if ("ele".equals(node.getNodeName())) {
						sEle = node.getTextContent();
					}
				}
				Date tAt = dateFormatTrack.parse(sTime);
				//log.info("Lat is " + sLat + " lon is " + sLon + " at " + tAt);
				geoTrack.addTrackPoint(new TrackPoint(Double.valueOf(sLat), Double.valueOf(sLon), Double.valueOf(sEle), tAt));
			}
			
			log.info("Result: " + geoTrack);
			
		} catch (Exception e) {
			log.error("Failed to parse GeoTracker XML", e);
		}
		return geoTrack;
	}
	
	/**
	 * Tries to add geographical data to pics in the specified dir,
	 * using the specified GeoTrack.
	 * 
	 * @param vecPics a list of pictures
	 * @param track   a GeoTrack object
	 */
	public int addGeoDataToPics(Vector<OriginalPic> vecPics, GeoTrack track, boolean bDryRun) {
		if (vecPics == null || track == null) {
			return 0;
		}
		
		log.info("Adding GPS data to " + vecPics.size() + " pics using " + track);
		int nMatches = 0;
		
		for (OriginalPic pic : vecPics) {
			TrackPoint tp = track.findClosestPoint(pic.getShotAt());
			if (tp != null) {
				log.info("Pic " + pic + " at " + dateFormatLog.format(pic.getShotAt()) + " matches TrackPoint " + tp.getTime());
				nMatches++;
				if (!bDryRun) {
					try {
						writeExifGeoData(tp, pic);
					} catch (Exception e) {
						log.error("Failed to set GPS coordinates", e);
					}
				}
			} else {
				log.info("Pic " + pic + " at " + dateFormatLog.format(pic.getShotAt()) + " has no match.");
			}
		}
		
		return nMatches;
	}
	
	/**
	 * Writes latitude and longitude EXIF tags to the specified picture,
	 * using exiftool commands.
	 * 
	 * @param tp   the track point to use
	 * @param pic  the picture file to update
	 * @throws Exception  if exiftool command failed
	 */
	private void writeExifGeoData(TrackPoint tp, OriginalPic pic) throws Exception {
		// NB: could also use exiftool -geotag ../geotracker/10_janv._2021_13_12_41.gpx .
		// to update all pics at once, but how to handle time offset ?
		if (tp != null && pic != null) {
			// exiftool -exif:gpslatitude="lat" -exif:gpslatituderef=S your.jpg
			String sCmd = "exiftool -exif:gpslatitude=\"" + tp.getLat() + "\" -exif:gpslatituderef=N -overwrite_original " + pic.getOrigFile().getAbsolutePath();
			log.info(sCmd);
			Process p = Runtime.getRuntime().exec(sCmd);
			p.waitFor();
			
			sCmd = "exiftool -exif:gpslongitude=\"" + tp.getLon() + "\" -exif:gpslongituderef=E -overwrite_original " + pic.getOrigFile().getAbsolutePath();
			log.info(sCmd);
			p = Runtime.getRuntime().exec(sCmd);
			p.waitFor();
		}
	}

	/**
	 * Gets the singleton instance.
	 */
	public static GeoTracker getInstance() {
		if (instance == null) {
			instance = new GeoTracker();
		}
		return instance;
	}
	
	/**
	 * Private singleton constructor.
	 */
	private GeoTracker() {
		// GeoTracker XML has timestamps in UTC, so we have to set the time zone
		dateFormatTrack.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * Testing method
	 * @param args  unused
	 */
	public static void main(String[] args) {
		File dir  = new File("/home/nicz/Pictures/Nature-2021-01/test/");
		File file = new File("/home/nicz/Pictures/Nature-2021-01/geotracker/10_janv._2021_13_12_41.gpx");
		boolean bDryRun = true;
		
		try {
			GeoTrack track = GeoTracker.getInstance().readGeoData(file);
			log.info("Altitude moyenne: " + track.getMeanPosition().getAltitude() + "m");
			
			int iOffset = 4*60*1000;
			track.setOffset(iOffset);
			
			Vector<OriginalPic> vecPics = FileManager.getInstance().getOrigFiles(dir, null);
			int nMatches = GeoTracker.getInstance().addGeoDataToPics(vecPics, track, bDryRun);
			log.info("Pictures matching track: " + nMatches);
		} catch (AppException e) {
			e.printStackTrace();
		}
	}

}
