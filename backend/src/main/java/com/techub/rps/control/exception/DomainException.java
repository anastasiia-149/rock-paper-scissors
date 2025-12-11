package com.techub.rps.control.exception;

import lombok.Getter;

@Getter
public class DomainException extends RuntimeException {

    private final String errorCode;
    private final ErrorType errorType;

    private DomainException(String errorCode, String message, ErrorType errorType) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = errorType;
    }

    private DomainException(String errorCode, String message, Throwable cause, ErrorType errorType) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorType = errorType;
    }

    public static DomainException invalidHand(String message) {
        return new DomainException("INVALID_HAND", message, ErrorType.CLIENT_ERROR);
    }


    public static DomainException gameError(String message, Throwable cause) {
        return new DomainException("GAME_ERROR", message, cause, ErrorType.SERVER_ERROR);
    }


    public static DomainException randomGenerationError(String message) {
        return new DomainException("RANDOM_GENERATION_ERROR", message, ErrorType.SERVER_ERROR);
    }

    public static DomainException randomGenerationError(String message, Throwable cause) {
        return new DomainException("RANDOM_GENERATION_ERROR", message, cause, ErrorType.SERVER_ERROR);
    }

    public static DomainException userNotFound(String message) {
        return new DomainException("USER_NOT_FOUND", message, ErrorType.CLIENT_ERROR);
    }

    public static DomainException invalidUsername(String message) {
        return new DomainException("INVALID_USERNAME", message, ErrorType.CLIENT_ERROR);
    }

    public enum ErrorType {
        CLIENT_ERROR,  // Maps to HTTP 4xx
        SERVER_ERROR   // Maps to HTTP 5xx
    }
}
