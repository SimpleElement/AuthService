package org.example.authservice.common.throwable.exception;

public class LargeNumberOfRequestsException extends RuntimeException {
    public LargeNumberOfRequestsException(String message) {
        super(message);
    }
}
