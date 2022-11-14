package controller.validation;

import java.io.File;
import java.util.Date;

import common.exceptions.ValidationException;
import controller.Controller;
import controller.export.ExpeditionsExporter;
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
			onError("Impossible d'enregistrer un objet indéfini !");
		}
		
		String sTitle = obj.getTitle();
		if (sTitle == null || sTitle.isEmpty()) {
			onError("Titre d'expédition invalide !");
		}
		
		if (obj.getDateFrom() == null) {
			onError("Date de début invalide !");
		}
		if (obj.getDateTo() == null) {
			onError("Date de fin invalide !");
		}

		if (!obj.getDateTo().after(obj.getDateFrom())) {
			onError("Intervalle de temps invalide ! \nLa date de début est après la date de fin.");
		}
		
		Date tNow = new Date();
		if (obj.getDateFrom().after(tNow)) {
			onError("La date de début est dans le futur !");
		}
		if (obj.getDateTo().after(tNow)) {
			onError("La date de fin est dans le futur !");
		}
		
		if (obj.getTrack() != null && !obj.getTrack().isEmpty()) {
			String sFileName = Controller.htmlPath + ExpeditionsExporter.dirTrack + obj.getTrack() + ".gpx";
			File fileTrack = new File(sFileName);
			if (!fileTrack.exists()) {
				onError("Le fichier GPX n'existe pas : " + sFileName);
			}
		}
	}

	@Override
	public void validateDelete(Expedition obj) throws ValidationException {
		onError("Impossible d'effacer une expédition !");
	}

}
