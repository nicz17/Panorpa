package controller.validation;

import common.exceptions.ValidationException;

import model.Taxon;
import model.TaxonRank;

/**
 * Validator for taxa.
 * 
 * @author nicz
 *
 */
public class TaxonValidator extends Validator<Taxon> {

	@Override
	public void validateSave(Taxon taxon) throws ValidationException {
		if (taxon == null) {
			onError("Impossible d'enregistrer un taxon indéfini !");
		}
		
		String name = taxon.getName();
		if (name == null || name.isEmpty()) {
			onError("Nom de taxon invalide: " + taxon);
		}
		
		TaxonRank rank = taxon.getRank();
		if (rank == null) {
			onError("Impossible d'enregistrer un taxon sans rang : " + taxon);
		}
		
		Taxon parent = taxon.getParent();
		if (parent != null) {
			if (parent.getRank() != rank.getParentRank()) {
				onError("Le rang du parent du taxon est invalide ! " + taxon);
			}
		}
	}

	@Override
	public void validateDelete(Taxon taxon) throws ValidationException {
		if (taxon == null) {
			onError("Impossible d'effacer un taxon indéfini !");
		}
		
		if (taxon.getIdx() <= 0) {
			onError("Impossible d'effacer un taxon non enregistré !");
		}
		
		if (!taxon.getChildren().isEmpty()) {
			onError("Impossible d'effacer le taxon " + taxon.getName() +
					"\ncar il a " + taxon.getChildren().size() + " sous-taxons.");
		}
		
		if (!taxon.getPics().isEmpty()) {
			onError("Impossible d'effacer le taxon " + taxon.getName() +
					"\nassocié à " + taxon.getPics().size() + " photos.");
		}
	}
}
