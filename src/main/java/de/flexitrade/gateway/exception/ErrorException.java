package de.flexitrade.gateway.exception; 

import org.slf4j.Logger;

public class ErrorException extends Exception {


	private static final long serialVersionUID = 301747677900700665L;

	public ErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ErrorException(Logger log, String message) {
        super(message);
        log.error(message);
    }

    public ErrorException(Logger log, String message, Throwable cause) {
        super(message, cause);
        log.error(message);
    }

}
