package controller;

import java.sql.ResultSet;
import java.sql.SQLException;

import model.AppParam;
import model.DataObject;
import model.Expedition;
import model.HerbierPic;
import model.Location;
import model.Taxon;
import model.TaxonRank;
import model.AppParam.AppParamKind;

/**
 * Factory for {@link DataObject}s, from SQL result sets.
 * 
 * @author nicz
 *
 */
public class DataObjectFactory {
	
	/**
	 * Singleton instance.
	 */
	private static DataObjectFactory instance;
	
	
	/** Get the singleton instance */
	public static DataObjectFactory getInstance() {
		if (instance == null)
			instance = new DataObjectFactory();
		return instance;
	}
	
	/**
	 * Creates a {@link HerbierPic} from the specified result-set.
	 * 
	 * @param rs the result-set with Picture info
	 * @return the created {@link HerbierPic}
	 * @throws SQLException if fails to read results
	 */
	public HerbierPic createPicture(ResultSet rs) throws SQLException {
		HerbierPic obj = new HerbierPic(rs.getInt("idxPicture"), 
				rs.getString("picFilename"));
		//obj.setLocation(LocationCache.getInstance().getLocation(rs.getInt("picIdxLocation")));
		obj.setRemarks(rs.getString("picRemarks"));
		obj.setShotAt(rs.getTimestamp("picShotAt"));
		obj.setUpdatedAt(rs.getTimestamp("picUpdatedAt"));
		obj.setIdxTaxon(rs.getInt("picTaxon"));
		obj.setRating(rs.getInt("picRating"));
		
		Location location = LocationCache.getInstance().getLocation(rs.getInt("picIdxLocation"));
		if (location != null) {
			obj.setLocation(location);
			location.addPic(obj);
		}
		
		return obj;
	}
	
	/**
	 * Creates a {@link Taxon} from the specified result-set.
	 * Does not set the parent Taxon.
	 * 
	 * @param rs the result-set with Taxon info
	 * @return the created {@link Taxon}
	 * @throws SQLException if fails to read results
	 */
	public Taxon createTaxon(ResultSet rs) throws SQLException {
		Taxon obj = new Taxon(rs.getInt("idxTaxon"), 
				rs.getString("taxName"), TaxonRank.valueOf(rs.getString("taxRank")));
		obj.setNameFr(rs.getString("taxNameFr"));
		obj.setIdxParent(rs.getInt("taxParent"));
		obj.setOrder(rs.getInt("taxOrder"));
		obj.setTypical(rs.getBoolean("taxTypical"));
		return obj;
	}
	
	/**
	 * Creates a {@link Location} from the specified result-set.
	 * 
	 * @param rs the result-set with Location info
	 * @return the created {@link Location}
	 * @throws SQLException if fails to read results
	 */
	public Location createLocation(ResultSet rs) throws SQLException {
		Location obj = new Location(rs.getInt("idxLocation"), 
				rs.getString("locName"));
		obj.setDescription(rs.getString("locDesc"));
		obj.setKind(rs.getString("locKind"));
		obj.setTown(rs.getString("locTown"));
		obj.setRegion(rs.getString("locRegion"));
		obj.setState(rs.getString("locState"));
		obj.setAltitude(rs.getInt("locAltitude"));
		return obj;
	}
	
	/**
	 * Creates an {@link Expedition} from the specified result-set.
	 * @param rs  the result-set with Expedition info
	 * @return  the created {@link Expedition}
	 * @throws SQLException if fails to read results
	 */
	public Expedition createExpedition(ResultSet rs) throws SQLException {
		Expedition obj = null;
		Location location = LocationCache.getInstance().getLocation(rs.getInt("expLocation"));
		if (location != null) {
			obj = new Expedition(rs.getInt("idxExpedition"), 
					location, 
					rs.getTimestamp("expFrom"), 
					rs.getTimestamp("expTo"), 
					rs.getString("expName"),  
					rs.getString("expDesc"));
		}
		return obj;
	}
	
	/**
	 * Creates a {@link AppParam} from the specified result-set.
	 * 
	 * @param rs the result-set with AppParam info
	 * @return the created {@link AppParam}
	 * @throws SQLException if fails to read results
	 */
	public AppParam createAppParam(ResultSet rs) throws SQLException {
		AppParam obj = new AppParam(rs.getInt("idxAppParam"), 
				rs.getString("apName"), rs.getString("apDesc"), 
				AppParamKind.valueOf(rs.getString("apKind")), 
				rs.getString("apStrVal"), 
				rs.getDouble("apNumVal"),
				rs.getTimestamp("apDateVal"));
		return obj;
	}
	

	/** Private singleton constructor */
	private DataObjectFactory() {
	};
	
}
