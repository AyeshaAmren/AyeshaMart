package com.ayeshamart.service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thrown when checkout input or an order mutation fails a business rule.
 * Carries a short {@code code} for flow-level problems (e.g. cartempty, paymentfailed,
 * stock) plus field-level messages the checkout form can highlight.
 */
public class CheckoutException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String code;
    private final Map<String, String> errors = new LinkedHashMap<>();

    public CheckoutException(String code, String message) {
        super(message);
        this.code = code;
    }

    public CheckoutException(Map<String, String> errors) {
        super("Please fix the highlighted fields.");
        this.code = "validation";
        this.errors.putAll(errors);
    }

    public String getCode() {
        return code;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}