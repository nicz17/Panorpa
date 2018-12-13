package controller.validation;

import common.exceptions.ValidationException;

import model.Expedition;

/**
 * Validator for Expeditions.
 * 
 * Checks the title is set and the date-range is valid.
 *
 * <p><b>Modifications:</b>
 * <ul>
 * <li>13.12.2018: nicz - Creation</li>
 * </ul>
 */
public class ExpeditionValidator extends Validator<Expedition> {

	@Override
	public void validateSave(Expedition obj) throws ValidationException {
		if (obj == null) {
			onError("Impossible d'enregistrer un lieu indéfini !");
		}
		
		String sTitle = obj.getTitle();
		if (sTitle == null || sTitle.isEmpty()) {
			onError("Titre d'expédition invalide : " + obj);
		}

		if (!obj.getDateTo().after(obj.getDate())) {
			onError("Intervalle de temps invalide : " + obj);
		}
	}

	@Override
	public void validateDelete(Expedition obj) throws ValidationException {
		onError("Impossible d'effacer une expédition !");
	}

}
