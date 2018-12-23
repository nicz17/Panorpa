package controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Vector;

import model.AppParam;
import model.AppParamName;
import model.Expedition;
import model.HerbierPic;
import model.Location;
import model.NamedValue;
import model.Taxon;
import model.TaxonRank;

import common.base.Logger;
import controller.DatabaseTools.eOrdering;

/**
 * Class for database access.
 * 
 * <p>Handles fetching and saving of objects.
 * 
 * @author nicz
 *
 */
public class DataAccess {
	private static final Logger log = new Logger("DataAccess", true);
	
	private DataObjectFactory objectFactory;
	private DatabaseTools dbTools;
	
	/** the singleton instance */
	private static DataAccess instance;
	
	
	public static DataAccess getInstance() {
		if (instance == null)
			instance = new DataAccess();
		return instance;
	}

	/**
	 * Closes the database connection.
	 */
	public void terminate() {
		dbTools.closeConnection();
	}
	
	
	public Vector<HerbierPic> getHerbierPics(eOrdering eOrder, String filter) {
		return getHerbierPics(" WHERE 1=1 ", eOrder, filter);
	}
	
	public Vector<HerbierPic> getHerbierPicsUpdatedAfter(Date date) {
		if (date == null) {
			return getHerbierPics(null, null);
		}
		return getHerbierPics(" WHERE picUpdatedAt > " + DatabaseTools.toSqlDateTime(date), null, null);
	}
		
	public Vector<HerbierPic> getHerbierPics(String where, eOrdering eOrder, String filter) {
		Vector<HerbierPic> vecResult = new Vector<HerbierPic>();
				
		where += getFilterWhere(filter, "picFilename", "picLocation", "picRemarks");

		String order = "";
		if (eOrder != null) {
			switch(eOrder) {
			case BY_IDX: 
				order = " ORDER BY idxPicture ";
				break;
			case BY_DEFAULT:
			case BY_FILENAME: 
				order = " ORDER BY picFilename ";
				break;
			case BY_LOCATION: 
				order = " ORDER BY picLocation, picFilename ";
				break;
			case BY_REMARKS: 
				order = " ORDER BY picRemarks, picFilename ";
				break;
			case BY_DATE: 
				order = " ORDER BY picShotAt, picFilename ";
				break;
			case BY_RATING: 
				order = " ORDER BY picRating, picFilename ";
				break;
			default:
				break;
			}
		}
		
		String query = "SELECT * FROM Picture " +
			where + order;
		log.debug("SQL: " + query);
		
		try {
			Connection conn = dbTools.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			rs.beforeFirst();
			while (rs.next()) {
				HerbierPic pic = objectFactory.createPicture(rs);
				vecResult.add(pic);
			}
			log.debug("Returning " + vecResult.size() + " pictures");
			stmt.close();
			return vecResult;
			
		} catch (SQLException e) {
			log.error("Fetching pics failed: " + e.getMessage());
		}
		return null;
	}
	

	/**
	 * Saves the specified {@link HerbierPic} to database.
	 * 
	 * @param obj  the picture to save
	 * @return the database index of saved object
	 */
	protected int savePicture(HerbierPic obj) {
		Connection conn = dbTools.getConnection();
		log.info("Saving " + obj);

		int idx = -1;
		int idxTaxon = obj.getTaxon() == null ? -1 : obj.getTaxon().getIdx();
		int idxLocation = obj.getLocation() == null ? -1 : obj.getLocation().getIdx();
		
		try {
			Statement stmt = conn.createStatement();
			
			if (obj.getIdx() > 0) {
				// update existing
				String query = String.format("UPDATE Picture SET picFilename = %s, " +
						"picShotAt = %s, picIdxLocation = %s, picRemarks = %s, " +
						"picTaxon = %s, picRating = %d, picUpdatedAt = %s " +
						"WHERE idxPicture = %d", 
						DatabaseTools.toSQLstring(obj.getFileName()), 
						DatabaseTools.toSqlDateTime(obj.getShotAt()), 
						idxLocation <= 0 ? "null" : String.valueOf(idxLocation), 
						DatabaseTools.toSQLstring(obj.getRemarks()), 
						idxTaxon <= 0 ? "null" : String.valueOf(idxTaxon), 
						obj.getRating(),
						DatabaseTools.toSqlDateTime(obj.getUpdatedAt()), 
						obj.getIdx() );
				log.debug("SQL: " + query);
				stmt.execute(query);
				idx = obj.getIdx();
			} else {
				// create new
				String query = String.format("INSERT INTO Picture " +
						"(idxPicture, picFilename, picShotAt, picIdxLocation, " +
						"picRemarks, picTaxon, picRating, picUpdatedAt) " +
						"VALUES (null, %s, %s, %s, %s, %s, %d, now() )", 
						DatabaseTools.toSQLstring(obj.getFileName()), 
						DatabaseTools.toSqlDateTime(obj.getShotAt()), 
						idxLocation <= 0 ? "null" : String.valueOf(idxLocation), 
						DatabaseTools.toSQLstring(obj.getRemarks()),
						idxTaxon <= 0 ? "null" : String.valueOf(idxTaxon), 
						obj.getRating() );
				log.debug("SQL: " + query);
				stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
				
				// GET ID
				ResultSet rs = stmt.getGeneratedKeys();
				if (rs.next())
					idx = rs.getInt(1);
				rs.close();
			}
			stmt.close();
		} catch (SQLException e) {
			log.error("Saving Picture failed: " + e.getMessage());
		}
		return idx;
	}
	
	/**
	 * Deletes the specified picture from database.
	 * 
	 * @param obj the picture to delete
	 */
	protected void deletePicture(HerbierPic obj) {
		Connection conn = dbTools.getConnection();
		log.info("Deleting " + obj);
		try {
			Statement stmt = conn.createStatement();
			if (obj.getIdx() > 0) {
				String query = String.format("DELETE FROM Picture WHERE idxPicture = %d", 
						obj.getIdx() );
				log.debug("SQL: " + query);
				stmt.execute(query);
			} else {
				log.error("Picture to delete has invalid idx: " + obj);
			}
			stmt.close();
		} catch (SQLException e) {
			log.error("Deleting Picture failed: " + e.getMessage());
		}
	}

	public Taxon getTaxon(int idxTaxon) {
		String where = " WHERE idxTaxon = " + idxTaxon;
		Vector<Taxon> taxons = getTaxons(where, null, null);
		if (taxons.isEmpty()) {
			return null;
		} else {
			return taxons.firstElement();
		}
	}
	
	public Taxon getTaxon(String name, TaxonRank rank) {
		String where = " WHERE taxRank = " + DatabaseTools.toSQLstring(rank.name()) +
				" AND taxName = '" + name + "' ";
		Vector<Taxon> taxons = getTaxons(where, null, null);
		if (taxons.isEmpty()) {
			return null;
		} else {
			return taxons.firstElement();
		}
	}
	
	public Vector<Taxon> getTaxons(TaxonRank rank) {
		if (rank == null) {
			return new Vector<>();
		} else {
			String where = " WHERE taxRank = " + DatabaseTools.toSQLstring(rank.name());
			return getTaxons(where, eOrdering.BY_DEFAULT, null);
		}
	}
	
	public Vector<Taxon> getTaxons(String where, eOrdering eOrder, String filter) {
		Vector<Taxon> vecResult = new Vector<>();
				
		if (where == null) {
			where = " WHERE 1=1 ";
		}
		where += getFilterWhere(filter, "taxName", "taxNameFr");

		//if (eOrder == null) eOrder = eOrdering.BY_DEFAULT;
		//String order = " ORDER BY taxRank, taxName ";
		String order = "";
		if (eOrder != null) {
			switch(eOrder) {
			case BY_IDX: 
				order = " ORDER BY idxTaxon ";
				break;
			case BY_NAME: 
				order = " ORDER BY taxName ";
				break;
			default:
				order = " ORDER BY taxRank, taxName ";
				break;
			}
		}
		
		String query = "SELECT * FROM Taxon " +
			where + order;
		
		try {
			Connection conn = dbTools.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			rs.beforeFirst();
			while (rs.next()) {
				Taxon taxon = objectFactory.createTaxon(rs);
				vecResult.add(taxon);
			}
			log.debug("Returning " + vecResult.size() + " taxons");
			stmt.close();
			return vecResult;
			
		} catch (SQLException e) {
			log.error("Fetching taxa failed: " + e.getMessage());
		}
		return null;
	}
	
	/**
	 * Gets a single location by its database index.
	 * 
	 * @param idxLocation  the location database index
	 * @return  the fetched location, or null if not found
	 */
	public Location getLocation(int idxLocation) {
		String where = " WHERE idxLocation = " + idxLocation;
		Vector<Location> locations = getLocations(where, null, null);
		if (locations.isEmpty()) {
			return null;
		} else {
			return locations.firstElement();
		}
	}
	

	public Vector<Location> getLocations(String where, eOrdering eOrder, String filter) {
		Vector<Location> vecResult = new Vector<>();
				
		if (where == null) {
			where = " WHERE 1=1 ";
		}
		where += getFilterWhere(filter, "locName", "locRegion", "locTown", "locKind");

		//if (eOrder == null) eOrder = eOrdering.BY_DEFAULT;
		String order = "";
		if (eOrder != null) {
			switch(eOrder) {
			case BY_KIND: 
				order = " ORDER BY locKind, locName ";
				break;
			case BY_TOWN: 
				order = " ORDER BY locTown, locName ";
				break;
			case BY_REGION: 
				order = " ORDER BY locRegion, locName ";
				break;
			case BY_ALTITUDE: 
				order = " ORDER BY locAltitude, locName ";
				break;
			default:
				order = " ORDER BY locName ";
				break;
			}
		}
		
		String query = "SELECT * FROM Location " +
			where + order;
		
		try {
			Connection conn = dbTools.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			rs.beforeFirst();
			while (rs.next()) {
				Location obj = objectFactory.createLocation(rs);
				vecResult.add(obj);
			}
			log.debug("Returning " + vecResult.size() + " locations");
			stmt.close();
			return vecResult;
			
		} catch (SQLException e) {
			log.error("Fetching locations failed: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Saves the specified {@link Location} to database.
	 * 
	 * @param obj  the location to save
	 * @return the database index of saved object
	 */
	protected int saveLocation(Location obj) {
		Connection conn = dbTools.getConnection();
		log.info("Saving " + obj);

		int idx = -1;
		
		try {
			Statement stmt = conn.createStatement();
			
			if (obj.getIdx() > 0) {
				// update existing
				String query = String.format("UPDATE Location SET locName = %s, " +
						"locDesc = %s, locKind = %s, locTown = %s, locRegion = %s, locState = %s, " +
						"locAltitude = %d, locLongitude = %s, locLatitude = %s, locMapZoom = %d " +
						"WHERE idxLocation = %d", 
						DatabaseTools.toSQLstring(obj.getName()), 
						DatabaseTools.toSQLstring(obj.getDescription()),
						DatabaseTools.toSQLstring(obj.getKind()), 
						DatabaseTools.toSQLstring(obj.getTown()), 
						DatabaseTools.toSQLstring(obj.getRegion()), 
						DatabaseTools.toSQLstring(obj.getState()),
						obj.getAltitude(), 
						DatabaseTools.toSQLDouble(obj.getLongitude()),
						DatabaseTools.toSQLDouble(obj.getLatitude()),
						obj.getMapZoom(),
						obj.getIdx() );
				log.debug("SQL: " + query);
				stmt.execute(query);
				idx = obj.getIdx();
			} else {
				// create new
				String query = String.format("INSERT INTO Location " +
						"(idxLocation, locName, locDesc, locKind, locTown, " +
						"locRegion, locState, locAltitude, " +
						"locLongitude, locLatitude, locMapZoom) " +
						"VALUES (null, %s, %s, %s, %s, %s, %s, %d, %s, %s, %d)", 
						DatabaseTools.toSQLstring(obj.getName()), 
						DatabaseTools.toSQLstring(obj.getDescription()),
						DatabaseTools.toSQLstring(obj.getKind()), 
						DatabaseTools.toSQLstring(obj.getTown()), 
						DatabaseTools.toSQLstring(obj.getRegion()), 
						DatabaseTools.toSQLstring(obj.getState()), 
						obj.getAltitude(), 
						DatabaseTools.toSQLDouble(obj.getLongitude()),
						DatabaseTools.toSQLDouble(obj.getLatitude()),
						obj.getMapZoom());
				log.debug("SQL: " + query);
				stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
				
				// GET ID
				ResultSet rs = stmt.getGeneratedKeys();
				if (rs.next())
					idx = rs.getInt(1);
				rs.close();
			}
			stmt.close();
		} catch (SQLException e) {
			log.error("Saving Location failed: " + e.getMessage());
		}
		return idx;
	}
	
	/**
	 * Fetches Expeditions from database.
	 * 
	 * @param where   optional where-clause
	 * @param eOrder  optional ordering object
	 * @param filter  optional filter
	 * @return
	 */
	public Vector<Expedition> getExpeditions(String where, eOrdering eOrder, String filter) {
		Vector<Expedition> vecResult = new Vector<>();
				
		if (where == null) {
			where = " WHERE 1=1 ";
		}
		where += getFilterWhere(filter, "expName", "expDesc");

		String order = "";
		if (eOrder != null) {
			switch(eOrder) {
			case BY_NAME: 
				order = " ORDER BY expName, expFrom ";
				break;
			case BY_LOCATION: 
				order = " ORDER BY expLocation ";
				break;
			case BY_DATE: 
				order = " ORDER BY expFrom DESC ";
				break;
			default:
				order = " ORDER BY expName ";
				break;
			}
		}
		
		String query = "SELECT * FROM Expedition " +
			where + order;
		
		try {
			Connection conn = dbTools.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			rs.beforeFirst();
			while (rs.next()) {
				Expedition obj = objectFactory.createExpedition(rs);
				vecResult.add(obj);
			}
			log.debug("Returning " + vecResult.size() + " expeditions");
			stmt.close();
			return vecResult;
			
		} catch (SQLException e) {
			log.error("Fetching expeditions failed: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Saves the specified {@link Expedition} to database.
	 * 
	 * @param obj  the expedition to save
	 * @return the database index of saved object
	 */
	protected int saveExpedition(Expedition obj) {
		Connection conn = dbTools.getConnection();
		log.info("Saving " + obj);

		int idx = -1;
		
		try {
			Statement stmt = conn.createStatement();
			
			if (obj.getIdx() > 0) {
				// update existing
				String query = String.format("UPDATE Expedition SET expName = %s, " +
						"expDesc = %s, expLocation = %d, expFrom = %s, expTo = %s " +
						"WHERE idxExpedition = %d", 
						DatabaseTools.toSQLstring(obj.getTitle()), 
						DatabaseTools.toSQLstring(obj.getNotes()),
						obj.getLocation().getIdx(), 
						DatabaseTools.toSqlDateTime(obj.getDateFrom()), 
						DatabaseTools.toSqlDateTime(obj.getDateTo()), 
						obj.getIdx() );
				log.debug("SQL: " + query);
				stmt.execute(query);
				idx = obj.getIdx();
			} else {
				// create new
				String query = String.format("INSERT INTO Expedition " +
						"(idxExpedition, expName, expDesc, expLocation, " +
						"expFrom, expTo) " +
						"VALUES (null, %s, %s, %d, %s, %s)", 
						DatabaseTools.toSQLstring(obj.getTitle()), 
						DatabaseTools.toSQLstring(obj.getNotes()),
						obj.getLocation().getIdx(), 
						DatabaseTools.toSqlDateTime(obj.getDateFrom()), 
						DatabaseTools.toSqlDateTime(obj.getDateTo()));
				log.debug("SQL: " + query);
				stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
				
				// GET ID
				ResultSet rs = stmt.getGeneratedKeys();
				if (rs.next())
					idx = rs.getInt(1);
				rs.close();
			}
			stmt.close();
		} catch (SQLException e) {
			log.error("Saving Expedition failed: " + e.getMessage());
		}
		return idx;
	}

	protected AppParam getAppParam(AppParamName apName) {
		String query = "SELECT * FROM AppParam WHERE apName = '" + apName.getDbName() + "'";
		AppParam obj = null;
		
		try {
			Connection conn = dbTools.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			rs.beforeFirst();
			while (rs.next()) {
				obj = objectFactory.createAppParam(rs);
			}
			stmt.close();
		} catch (SQLException e) {
			log.error("Fetching AppParam " + apName.getDbName() + " failed: " + e.getMessage());
		}
		
		if (obj == null) {
			log.error("Could not find AppParam named " + apName.getDbName());
		}
		
		return obj;
	}
	
	/**
	 * Updates the specified application parameter in database.
	 * Parameter must already be inserted.
	 * 
	 * @param ap  the application parameter to update.
	 */
	protected void saveAppParam(AppParam ap) {
		if (ap.getIdx() <= 0) {
			log.error("Cannot save undefined " + ap);
		} else {
			log.info("Saving " + ap);
			
			String strVal  = "NULL";
			String numVal  = "NULL";
			String dateVal = "NULL";
			
			switch (ap.getKind()) {
			case BOOL:
				numVal = ap.getBoolValue() ? "1" : "0";
				break;
			case INT:
				numVal = String.format("%d", ap.getIntValue());
				break;
			case FLOAT:
				numVal = String.format("%f", ap.getDoubleValue());
				break;
			case DATE:
				dateVal = DatabaseTools.toSqlDateTime(ap.getDateValue());
				break;
			case STR:
				if (ap.getStrValue() != null && !ap.getStrValue().isEmpty()) {	
					strVal = DatabaseTools.toSQLstring(ap.getStrValue());
				}
				break;
			default:
				log.error("Unhandled AppParamKind " + ap.getKind().name());
				break;
			}
			
			try {
				Connection conn = dbTools.getConnection();
				Statement stmt = conn.createStatement();
				String query = String.format("UPDATE AppParam SET " +
						"apStrVal = %s, apNumVal = %s, apDateVal = %s " +
						"WHERE idxAppParam = %d", 
						strVal, numVal, dateVal, ap.getIdx() );
				log.debug("SQL: " + query);
				stmt.execute(query);
				stmt.close();
			} catch (SQLException e) {
				log.error("Saving " + ap + " failed: " + e.getMessage());
			}
		}
	}

	/**
	 * Saves the specified {@link Taxon} to database.
	 * 
	 * @param obj  the picture to save
	 * @return the database index of saved object
	 */
	protected int saveTaxon(Taxon obj) {
		int idx = -1;
		Connection conn = dbTools.getConnection();
		log.info("Saving " + obj);
		try {
			Statement stmt = conn.createStatement();
			
			int idxParent = -1;
			if (obj.getParent() != null) {
				idxParent = obj.getParent().getIdx();
			}
			
			if (obj.getIdx() > 0) {
				// update existing
				String query = String.format("UPDATE Taxon SET taxName = %s, " +
						"taxNameFr = %s, taxRank = %s, taxParent = %s, " +
						"taxOrder = %d, taxTypical = %d " +
						"WHERE idxTaxon = %d", 
						DatabaseTools.toSQLstring(obj.getName()), 
						DatabaseTools.toSQLstring(obj.getNameFr()), 
						DatabaseTools.toSQLstring(obj.getRank().name()),
						idxParent <= 0 ? "null" : String.valueOf(idxParent), 
						obj.getOrder(),
						obj.isTypical() ? 1 : 0,
						obj.getIdx() );
				log.debug("SQL: " + query);
				stmt.execute(query);
				idx = obj.getIdx();
			} else {
				// create new
				String query = String.format("INSERT INTO Taxon " +
						"(idxTaxon, taxName, taxNameFr, taxRank, taxParent, taxOrder, taxTypical) " +
						"VALUES (null, %s, %s, %s, %s, %d, %d )", 
						DatabaseTools.toSQLstring(obj.getName()), 
						DatabaseTools.toSQLstring(obj.getNameFr()), 
						DatabaseTools.toSQLstring(obj.getRank().name()),
						idxParent <= 0 ? "null" : String.valueOf(idxParent), 
						obj.getOrder(),
						obj.isTypical() ? 1 : 0);
				log.debug("SQL: " + query);
				stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
				
				// GET ID
				ResultSet rs = stmt.getGeneratedKeys();
				if (rs.next())
					idx = rs.getInt(1);
				rs.close();
			}
			stmt.close();
		} catch (SQLException e) {
			log.error("Saving Taxon failed: " + e.getMessage());
		}
		return idx;
	}
	
	/**
	 * Deletes the specified taxon from database.
	 * 
	 * @param obj the taxon to delete
	 */
	protected void deleteTaxon(Taxon obj) {
		Connection conn = dbTools.getConnection();
		log.info("Deleting " + obj);
		try {
			Statement stmt = conn.createStatement();
			if (obj.getIdx() > 0) {
				String query = String.format("DELETE FROM Taxon WHERE idxTaxon = %d", 
						obj.getIdx() );
				log.debug("SQL: " + query);
				stmt.execute(query);
			} else {
				log.error("Taxon to delete has invalid idx: " + obj);
			}
			stmt.close();
		} catch (SQLException e) {
			log.error("Deleting Taxon failed: " + e.getMessage());
		}
	}
	
	/**
	 * Counts distinct occurrences in the specified column of the specified table,
	 * grouped by their value.
	 * 
	 * @param table  the database table
	 * @param column the table column
	 * @return  a sorted list of counts by value
	 */
	public Vector<NamedValue> getGroupedCount(String table, String column, String order) {
		if (order == null) {
			order = "count(*) DESC";
		}
		
		String query = "SELECT " + column + ", count(*) FROM " + table + 
				" GROUP BY " + column + " ORDER BY " + order;
		Vector<NamedValue> result = new Vector<>();
		
		try {
			Connection conn = dbTools.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			rs.beforeFirst();
			
			while (rs.next()) {
				result.add(new NamedValue(rs.getInt("count(*)"), rs.getString(column)));
			}
			
			stmt.close();
		} catch (SQLException e) {
			log.error("Counting failed: " + e.getMessage());
		}
		return result;
	}

	public int countPictures() {
		String query = "SELECT count(*) as nObj from Picture";
		return countAny(query);
	}

	private int countAny(String query) {
		try {
			Connection conn = dbTools.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			rs.beforeFirst();
			rs.first();
			int nObj = rs.getInt("nObj");
			stmt.close();
			return nObj;
		} catch (SQLException e) {
			log.error("Counting failed: " + e.getMessage());
		}
		return 0;
	}

	/**
	 * Gets a SQL filtering for the specified filter and fields.
	 * @param filter  an optional filter (may be null)
	 * @param fields  a list of fields to filter
	 * @return an SQL filtering 
	 */
	private String getFilterWhere(String filter, String ... fields) {
		if (filter == null || filter.isEmpty()) return " ";
		String where = " AND (0=1";
		for (String field : fields) {
			where += " OR " + field + " LIKE '" + filter + "%' OR " +
				field + " LIKE '%" + filter + "%'";
		}
		where += ") ";
		return where;
	}
	
	/**
	 * Private singleton constructor.
	 */
	private DataAccess() {
		objectFactory = DataObjectFactory.getInstance();
		dbTools = new DatabaseTools();
	}
}
