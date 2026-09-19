package com.ayeshamart.service;

/**
 * Thrown when a review cannot be created: the buyer did not buy the product, the item was
 * not yet delivered, a review already exists, or the input is invalid.
 */
public class ReviewException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String code;

    public ReviewException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}