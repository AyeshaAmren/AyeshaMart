package com.ayeshamart.service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thrown when user input for registration fails validation.
 * Carries a field-to-message map so the JSP can highlight each invalid field.
 */
public class UserValidationException extends Exception {

    private final Map<String, String> errors = new LinkedHashMap<>();

    public UserValidationException() {
        super("Registration validation failed");
    }

    public void addError(String field, String message) {
        errors.put(field, message);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}