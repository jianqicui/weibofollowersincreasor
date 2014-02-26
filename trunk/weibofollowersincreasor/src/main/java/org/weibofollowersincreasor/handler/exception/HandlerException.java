package org.weibofollowersincreasor.handler.exception;

public class HandlerException extends Exception {

	private static final long serialVersionUID = 1L;

	public HandlerException(Throwable cause) {
		super(cause);
	}
	
	public HandlerException(String message) {
		super(message);
	}

}
