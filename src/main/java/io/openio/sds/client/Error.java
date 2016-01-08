package io.openio.sds.client;

import io.openio.sds.exceptions.BadRequestException;
import io.openio.sds.exceptions.ContainerNotFoundException;
import io.openio.sds.exceptions.ObjectExistException;
import io.openio.sds.exceptions.ObjectNotFoundException;
import io.openio.sds.exceptions.SdsException;

/**
 * 
 *
 *
 */
public class Error {

	public static final int CODE_BAD_REQUEST = 400;
	public static final int CODE_CONTAINER_NOT_FOUND = 406;
	public static final int CODE_OBJECT_NOT_FOUND = 420;
	public static final int CODE_OBJECT_EXISTS = 421;

	private int status;
	private String message;

	public Error() {

	}

	public Error(int status, String message) {
		this.status = status;
		this.message = message;
	}

	public int status() {
		return status;
	}

	public String message() {
		return message;
	}

	public SdsException ex() {
		switch (status) {
		case CODE_BAD_REQUEST:
			throw new BadRequestException(message);
		case CODE_CONTAINER_NOT_FOUND:
			throw new ContainerNotFoundException(message);
		case CODE_OBJECT_EXISTS:
			throw new ObjectExistException(message);
		case CODE_OBJECT_NOT_FOUND:
			throw new ObjectNotFoundException(message);
		default:
			throw new SdsException(String.format("(Unknown return code %d) %s",
					status, message));
		}
	}
}
