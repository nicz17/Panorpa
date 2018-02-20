package controller.validation;

import common.base.Logger;
import common.exceptions.ValidationException;

import model.DataObject;

public abstract class Validator<T extends DataObject> {
	
	private static Logger log = new Logger("Validator");
	
	/**
	 * Validates the specified object before saving.
	 * 
	 * @param obj  the database object to save
	 * @throws ValidationException  if it is invalid to save object
	 */
	public abstract void validateSave(T obj) throws ValidationException;
	
	/**
	 * Validates the specified object before deleting.
	 * 
	 * @param obj  the database object to delete
	 * @throws ValidationException  if it is invalid to delete object
	 */
	public abstract void validateDelete(T obj) throws ValidationException;

	/**
	 * Writes an error message to log and throws a {@link ValidationException}
	 * with the specified error message.
	 * 
	 * @param msg  the error message to log and throw
	 * @throws ValidationException  always
	 */
	protected void onError(String msg) throws ValidationException {
		log.error(msg);
		throw new ValidationException(msg);
	}
}
