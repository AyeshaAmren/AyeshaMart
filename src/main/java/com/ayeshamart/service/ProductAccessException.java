package com.ayeshamart.service;

/**
 * Thrown when a seller tries to access or modify a product that does not belong to them,
 * or when a non-seller attempts a seller-only action. Used to enforce ownership on the
 * server side (never trusting data posted from the browser).
 */
public class ProductAccessException extends Exception {

    private static final long serialVersionUID = 1L;

    public ProductAccessException(String message) {
        super(message);
    }
}
