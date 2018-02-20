package controller.checks;

import java.util.Collection;
import java.util.Vector;

import model.DataProblem;
import model.Location;

import common.base.Logger;

import controller.LocationCache;

public class LocationChecker implements DataChecker {

	private static final Logger log = new Logger("LocationChecker", true);
	
	public LocationChecker() {
	}

	@Override
	public Vector<DataProblem> check() {
		Vector<DataProblem> vecProblems = new Vector<>();
		
		Collection<Location> vecLocations = LocationCache.getInstance().getAll();
		
		log.info("Quality check of " + vecLocations.size() + " locations");
		
		for (Location location : vecLocations) {
			String descr = location.getDescription();
			if (descr == null || descr.isEmpty()) {
				DataProblem prob = new DataProblem(location.getIdx(), ProblemKind.LOC_NO_DESCR, 
						"Lieu '" + location.getName() + "' sans description (" +
						location.getPics().size() + " photos)");
				vecProblems.add(prob);
				//log.debug(prob.toString());
			}
		}
		
		log.info("Quality check of " + vecLocations.size() + " locations: " +
				vecProblems.size() + " problems");
		
		return vecProblems;
	}

}
