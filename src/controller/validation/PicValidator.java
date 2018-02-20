package controller.validation;

import common.exceptions.ValidationException;

import model.HerbierPic;

/**
 * Validator for pictures.
 * 
 * @author nicz
 *
 */
public class PicValidator extends Validator<HerbierPic> {

	@Override
	public void validateSave(HerbierPic pic) throws ValidationException {
		if (pic == null) {
			onError("Impossible d'enregistrer une image indéfinie !");
		}
		
		String name = pic.getFileName();
		if (name == null || name.isEmpty()) {
			onError("Nom de fichier invalide : " + pic);
		}
		
		if (pic.getShotAt() == null) {
			onError("Impossible d'enregistrer une image sans date : " + pic);
		}
	}

	@Override
	public void validateDelete(HerbierPic pic) throws ValidationException {
		//onError("Impossible d'effacer une image !");
	}

}
