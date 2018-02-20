package controller.checks;

import java.util.Collection;
import java.util.Vector;

import model.DataProblem;
import model.HerbierPic;
import model.Taxon;

import common.base.Logger;

import controller.TaxonCache;

public class TaxonChecker implements DataChecker {

	private static final Logger log = new Logger("TaxonChecker", true);
	
	public TaxonChecker() {
	}

	@Override
	public Vector<DataProblem> check() {
		Vector<DataProblem> vecProblems = new Vector<>();
		
		Collection<Taxon> taxons = TaxonCache.getInstance().getAll();
		
		log.info("Quality check of " + taxons.size() + " taxons");
		
		for (Taxon taxon : taxons) {
			String frName = taxon.getNameFr();
			if (frName == null || frName.isEmpty()) {
				DataProblem problem = new DataProblem(taxon.getIdx(), ProblemKind.TAX_NO_FRNAME, 
						"Le taxon " + taxon.toString() + " n'a pas de nom français");
				vecProblems.add(problem);
			}
			
			HerbierPic pic = taxon.getTypicalPic();
			if (pic == null) {
				DataProblem problem = new DataProblem(taxon.getIdx(), ProblemKind.TAX_NO_DEFPIC, 
						"Le taxon " + taxon.toString() + " n'a pas d'image-type");
				vecProblems.add(problem);
			}
			
			// TODO check that non-kingdoms have a parent taxon
		}
		
		log.info("Quality check of " + taxons.size() + " taxons: " +
				vecProblems.size() + " problems");
		
		return vecProblems;
	}

}
