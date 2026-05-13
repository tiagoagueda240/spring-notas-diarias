package com.tiagoagueda.api.core.exception;

/**
 * Lançada quando o utilizador tenta criar uma entrada de diário
 * para um dia em que já existe uma. Convertida em HTTP 409 (Conflict).
 */
public class DuplicateEntryException extends RuntimeException {
    public DuplicateEntryException(String message) {
        super(message);
    }
}
