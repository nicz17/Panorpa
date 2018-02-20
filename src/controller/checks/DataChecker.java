package controller.checks;

import java.util.Vector;

import model.DataProblem;


/**
 * Interface describing the methods used to check data quality and find problems.
 * 
 * @author nicz
 *
 */
public interface DataChecker {
	
	/**
	 * Checks the data and generates problems accordingly.
	 * 
	 * @return a possibly empty list of encountered problems
	 */
	public Vector<DataProblem> check();

}
