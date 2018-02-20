package controller.checks;

import java.util.Vector;

import model.DataProblem;

import common.base.Logger;

public class DataCheckManager {
	
	private static final Logger log = new Logger("DataCheckManager", true);
	
	/** The list of data checkers */
	private final Vector<DataChecker> vecCheckers;
	
	/** The list of detected problems */
	private final Vector<DataProblem> vecProblems;
	
	/** the singleton instance */
	private static DataCheckManager _instance = null;
	
	
	public void addDataChecker(DataChecker checker) {
		vecCheckers.add(checker);
	}
	
	public void clearProblems() {
		log.info("Clearing " + vecProblems.size() + " problems");
		vecProblems.clear();
	}
	
	public Vector<DataProblem> getProblems() {
		return vecProblems;
	}
	
	/**
	 * Logs all detected problems to console.
	 */
	public void dumpProblems() {
		log.info("Dumping " + vecProblems.size() + " problems:");
		for (DataProblem problem : vecProblems) {
			log.info(problem.toString());
		}
	}
	
	/**
	 * Runs the data quality checks.
	 */
	public void checkData() {
		log.info("Checking data quality with " + vecCheckers.size() + " checkers");
		clearProblems();
		
		for (DataChecker checker : vecCheckers) {
			vecProblems.addAll(checker.check());
		}
		
		log.info("Detected " + vecProblems.size() + " problems.");
	}
	
	
	/** Gets the singleton instance. */
	public static DataCheckManager getInstance() {
		if (_instance == null)
			_instance = new DataCheckManager();
		return _instance;
	}
	
	/** Private singleton constructor */
	private DataCheckManager() {
		vecCheckers = new Vector<>();
		vecProblems = new Vector<>();
		
		addDataChecker(new PictureChecker());
		addDataChecker(new TaxonChecker());
		addDataChecker(new LocationChecker());
	}

}
