package io.openio.sds.exceptions;


public class SettingsException extends SdsException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5395232720464963503L;

	public SettingsException(String message) {
        super(message);
    }

    public SettingsException(String message, Throwable t) {
        super(message, t);
    }
}
