package controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.base.Logger;

/**
 * Various database utility methods.
 * 
 * @author nicz
 *
 */
public class DatabaseTools {
	
	private static final Logger log = new Logger("DatabaseTools", true);
	private static final String dbName  = "herbier"; 
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	
	/** The database connection */
	private Connection conn;
	
	/**
	 * Gets a database connection, creating it if needed.
	 * 
	 * @return a database connection
	 */
	public Connection getConnection() {
		//log.info("Opening database connection...");
		if (conn == null) {
			try {
				//Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
				conn = DriverManager.getConnection(String.format(
						"jdbc:mysql://localhost:3306/%s?user=nicz&password=nico17sql",
						dbName));
				if (conn != null) {
					log.info(String.format("Connected to database %s.", dbName));
				} else {
					log.error("Failed to connect to DB " + dbName);
				}
			} catch (Exception e) {
				log.error("Opening database failed: " + e.getMessage());
			}
		}
		return conn;
	}
	
	/**
	 * Closes the database connection if needed.
	 */
	public void closeConnection() {
		if (conn != null) {
			try {
				conn.close();
				log.info("Closed database connection");
			} catch (SQLException e) {
				log.error("Failed to close DB connection: " + e.getMessage());
			}
		}
	}

	/**
	 * The names of the months, in French
	 */
	public final static String[] monthNames = {
		"janvier", "février", "mars", "avril", "mai", "juin", 
		"juillet", "août", "septembre", "octobre", "novembre", "décembre"
	};
	
	/**
	 * Enum for table orderings.
	 * 
	 * @author nicz
	 *
	 */
	public enum eOrdering {
		BY_DEFAULT,
		BY_IDX,
		BY_DATE,
		BY_FILENAME,
		BY_LOCATION,
		BY_REMARKS,
		BY_NAME,
		BY_NAME_FR,
		BY_RANK,
		BY_ALTITUDE,
		BY_KIND,
		BY_TOWN,
		BY_REGION,
		BY_RATING
	}
	
	/**
	 * Enum for database object updates.
	 * 
	 * @author nicz
	 *
	 */
	public enum UpdateType {
		PICTURE,
		TAXON, 
		LOCATION,
		EXPEDITION
	}

	/**
	 * Convert an database date string into a pretty date in French.
	 * @param sqlDate the database date string
	 * @return the pretty date string
	 */
	public static String toPrettyDate(String sqlDate) {
		String prettyDate = sqlDate;
		Pattern pat = Pattern.compile("(\\d\\d\\d\\d)\\-(\\d\\d)\\-(\\d\\d)");
		Matcher mat = pat.matcher(sqlDate);
		if (mat.find()) {
			int iDay = Integer.valueOf(mat.group(3)).intValue();
			String strDay = (iDay==1 ? "1er" : String.format("%d", iDay));
			prettyDate = String.format("%s %s %s", strDay,
					monthNames[Integer.valueOf(mat.group(2)).intValue()-1], 
					mat.group(1) );
		} else {
			System.out.println("Could not convert to pretty date: <" + sqlDate + ">");
		}
		return prettyDate;
	}
	
	/**
	 * Get the current date as yyyy-mm-dd formatted string.
	 * @return the current date string in database format
	 */
	public static String getDateNow() {
		Calendar calendar = Calendar.getInstance();
		return String.format("%4d-%02d-%02d", calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DATE));
	}
	
	/**
	 * Return the year part of a database date string.
	 * @param sqlDate the database date string
	 * @return the year, or 0 if failed to parse
	 */
	public static int getYear(String sqlDate) {
		Pattern pat = Pattern.compile("(\\d\\d\\d\\d)\\-(\\d\\d)\\-(\\d\\d)");
		Matcher mat = pat.matcher(sqlDate);
		if (mat.find()) {
			return Integer.valueOf(mat.group(1)).intValue();
		} else {
			System.out.println("Could not find year in date: <" + sqlDate + ">");
		}
		return 0;
	}
	
	/**
	 * Escape special chars in the string and add '' around it.
	 * 
	 * @param s the string (may be null)
	 * @return the converted string, or "null"
	 */
	public static String toSQLstring(String s) {
		if (s != null) {
			Pattern p = Pattern.compile("\\'");
			Matcher m = p.matcher(s);
			s = m.replaceAll("\\'\\'");
			return "'" + s + "'";
		}
		return "null";
	}
	
	/**
	 * Writes the specified Double value, which may be null,
	 * as an SQL compatible string.
	 * @param dVal  the value to express as SQL
	 * @return the converted string or "null"
	 */
	public static String toSQLDouble(Double dVal) {
		if (dVal != null) {
			//return String.valueOf(dVal.doubleValue());
			return String.format("%.6f", dVal.doubleValue());
		}
		return "null";
	}
	
	/**
	 * Formats the specified date for SQL, with '' around it.
	 * Does not format the time.
	 * @param date  the date to format (may be null)
	 * @return the SQL formatted date, or 'NULL' if date is null
	 */
	public static String toSqlDate(Date date) {
		if (date == null) {
			return "NULL";
		}
		return "'" + dateFormat.format(date) + "'";
	}
	
	/**
	 * Formats the specified date for SQL, with '' around it.
	 * Also formats the time.
	 * @param date  the date to format (may be null)
	 * @return the SQL formatted date and time, or 'NULL' if date is null
	 */
	public static String toSqlDateTime(Date date) {
		if (date == null) {
			return "NULL";
		}
		return "'" + dateTimeFormat.format(date) + "'";
	}
	
	public static Date sqlToDate(String strDate) {
		try {
			return dateFormat.parse(strDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * Get an SQL where clause for the given date-range.
	 * 
	 * @param evtSel a date-range
	 * @param col the table column to filter on
	 * @return an SQL where clause
	 */
//	public static String getWhere(DateSelect evtSel, String col) {
//		switch(evtSel) {
//		case TODAY:     
//			return String.format(" WHERE dayofyear(%s) = dayofyear(now()) AND year(%s) = year(now()) ",
//					col, col);
//		case THISMONTH: 
//			return String.format(" WHERE Month(%s) = Month(now()) AND Year(%s) = Year(now()) ",
//					col, col);
//		default: 
//			return " WHERE 1=1 ";
//		}
//	}
}
