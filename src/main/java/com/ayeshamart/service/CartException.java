package com.ayeshamart.service;

/**
 * Thrown when a cart operation violates a business rule (bad quantity, stock limits,
 * unavailable product, foreign cart). Carries a short code so controllers can map it
 * to a friendly message without leaking internals.
 */
public class CartException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String code;

    public CartException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
