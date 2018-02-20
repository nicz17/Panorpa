package model;

/**
 * Abstract superclass for database objects.
 * 
 * @author nicz
 *
 */
public abstract class DataObject {
	
	/**
	 * @return the database index of the object.
	 */
	public abstract int getIdx();
	
	/**
	 * Describes how the object should be displayed in tables.
	 * @return a list of data to display in tables
	 */
	public abstract String[] getDataRow();
}
