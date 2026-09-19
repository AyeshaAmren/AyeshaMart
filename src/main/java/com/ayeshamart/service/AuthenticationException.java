package com.ayeshamart.service;

/**
 * Thrown when a login attempt cannot be authenticated.
 * The message is safe to show to the user on the login form.
 */
public class AuthenticationException extends Exception {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }
}
