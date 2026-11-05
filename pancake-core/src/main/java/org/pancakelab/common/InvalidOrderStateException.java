package org.pancakelab.common;

public class InvalidOrderStateException extends IllegalStateException {
    public InvalidOrderStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
