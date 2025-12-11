package com.techub.rps.boundary.incoming;

import com.techub.rps.boundary.incoming.dto.ErrorResponse;
import com.techub.rps.control.exception.DomainException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex) {
        if (ex.getErrorType() == DomainException.ErrorType.CLIENT_ERROR) {
            log.warn("Client error - {}: {}", ex.getErrorCode(), ex.getMessage());
            return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getErrorCode(), ex.getMessage());
        } else {
            log.error("Server error - {}: {}", ex.getErrorCode(), ex.getMessage(), ex);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getErrorCode(), ex.getMessage());
        }
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> handleClientValidationErrors(Exception ex) {
        String message = extractClientErrorMessage(ex);
        log.warn("Client validation error: {}", message);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestBody(HttpMessageNotReadableException ex) {
        String message = "Invalid request body";
        if (ex.getCause() instanceof InvalidFormatException invalidFormatEx) {
            message = buildEnumErrorMessage(invalidFormatEx);
        }

        log.warn("Invalid request body: {}", message);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_BODY", message);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not supported: {}", ex.getMethod());
        return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                String.format("HTTP method '%s' is not supported for this endpoint", ex.getMethod()));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        log.warn("Media type not supported: {}", ex.getContentType());
        return buildErrorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE",
                "Content type not supported. Please use 'application/json'");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException ex) {
        log.warn("Endpoint not found: {}", ex.getRequestURL());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND",
                String.format("Endpoint '%s' not found", ex.getRequestURL()));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedError(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later.");
    }


    private String extractClientErrorMessage(Exception ex) {
        return switch (ex) {
            case MethodArgumentNotValidException validationEx -> {
                String fieldErrors = validationEx.getBindingResult().getFieldErrors().stream()
                        .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                        .collect(Collectors.joining(", "));
                yield fieldErrors.isEmpty() ? "Validation failed" : fieldErrors;
            }
            case MethodArgumentTypeMismatchException typeMismatchEx ->
                    String.format("Invalid value '%s' for parameter '%s'",
                            typeMismatchEx.getValue(), typeMismatchEx.getName());
            case MissingServletRequestParameterException missingParamEx ->
                    String.format("Required parameter '%s' is missing", missingParamEx.getParameterName());
            case IllegalArgumentException illegalArgEx -> illegalArgEx.getMessage();
            default -> "Invalid request";
        };
    }

    private String buildEnumErrorMessage(InvalidFormatException ex) {
        if (ex.getTargetType().isEnum()) {
            String validValues = java.util.Arrays.stream(ex.getTargetType().getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            return String.format("Invalid value '%s'. Valid values are: %s", ex.getValue(), validValues);
        }
        return "Invalid format in request body";
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String errorCode, String message) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(status.value());
        errorResponse.setErrorCode(errorCode);
        errorResponse.setMessage(message);
        errorResponse.setTimestamp(OffsetDateTime.ofInstant(Instant.now(), ZoneOffset.UTC));
        return ResponseEntity.status(status).body(errorResponse);
    }
}
