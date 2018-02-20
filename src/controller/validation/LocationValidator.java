package controller.validation;

import common.exceptions.ValidationException;

import model.Location;

public class LocationValidator extends Validator<Location> {

	@Override
	public void validateSave(Location obj) throws ValidationException {
		if (obj == null) {
			onError("Impossible d'enregistrer un lieu indéfini !");
		}
		
		String name = obj.getName();
		if (name == null || name.isEmpty()) {
			onError("Nom de lieu invalide : " + obj);
		}

	}

	@Override
	public void validateDelete(Location obj) throws ValidationException {
		onError("Impossible d'effacer un lieu !");
	}

}
