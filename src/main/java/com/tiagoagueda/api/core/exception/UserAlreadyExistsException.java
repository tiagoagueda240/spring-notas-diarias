package com.tiagoagueda.api.core.exception;

/**
 * Exceção de domínio para tentativa de registo com email já existente.
 *
 * É convertida pelo GlobalExceptionHandler em HTTP 409 (Conflict).
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}