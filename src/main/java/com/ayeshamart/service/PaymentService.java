package com.ayeshamart.service;

import com.ayeshamart.model.Payment;

/**
 * Generates demo payment decisions for Ayesha Mart.
 *
 * Demo rules (kept deterministic so the flow is testable):
 * - Cash on Delivery          -> status PENDING (paid on delivery).
 * - UPI                       -> SUCCESS unless the VPA contains the word "fail".
 * - Card                      -> SUCCESS unless the card number ends in 0.
 *
 * Ayesha Mart NEVER stores real card numbers, CVV or PINs: only the method, a masked
 * summary ("UPI u***@ybl", "Card **** 4242") and the outcome are kept.
 */
public class PaymentService {

    /**
     * Builds a demo payment decision for the given method. The returned {@link Payment}
     * has no orderId yet - the caller attaches it before persisting.
     */
    public Payment prepare(String method, String upiId, String cardNumber, String cardName, double amount)
            throws CheckoutException {
        if (method == null || method.trim().isEmpty()) {
            throw new CheckoutException(errors("payment", "Please choose a payment method."));
        }

        Payment payment = new Payment();
        payment.setMethod(normalizeMethod(method.trim()));
        payment.setAmount(amount);

        String id = uniqueId();
        switch (payment.getMethod()) {
            case Payment.METHOD_COD -> {
                payment.setStatus(Payment.STATUS_PENDING);
                payment.setTransactionId("COD" + id);
                payment.setMaskedDetails("Cash on delivery");
            }
            case Payment.METHOD_UPI -> {
                String vpa = trimToNull(upiId);
                if (vpa == null || !vpa.contains("@")) {
                    throw new CheckoutException(errors("upiId", "Enter a valid UPI ID like name@bank."));
                }
                payment.setTransactionId("UPI" + id);
                payment.setMaskedDetails("UPI " + maskId(vpa));
                payment.setStatus(vpa.toLowerCase().contains("fail") ? Payment.STATUS_FAILED : Payment.STATUS_SUCCESS);
            }
            case Payment.METHOD_CARD -> {
                String number = trimToNull(cardNumber);
                String name = trimToNull(cardName);
                if (number == null) {
                    throw new CheckoutException(errors("cardNumber", "Enter your card number."));
                }
                if (!number.matches("\\d{12,19}")) {
                    throw new CheckoutException(errors("cardNumber", "Enter a valid 12-19 digit card number."));
                }
                if (name == null || name.length() < 2) {
                    throw new CheckoutException(errors("cardName", "Enter the name on the card."));
                }
                String last4 = number.substring(number.length() - 4);
                payment.setTransactionId("CARD" + id);
                payment.setMaskedDetails("Card **** " + last4);
                payment.setStatus(number.endsWith("0") ? Payment.STATUS_FAILED : Payment.STATUS_SUCCESS);
            }
            default -> throw new CheckoutException(errors("payment", "Please choose a valid payment method."));
        }
        return payment;
    }

    private String normalizeMethod(String method) {
        for (String candidate : new String[]{Payment.METHOD_COD, Payment.METHOD_UPI, Payment.METHOD_CARD}) {
            if (candidate.equalsIgnoreCase(method)) {
                return candidate;
            }
        }
        return method;
    }

    private String maskId(String vpa) {
        int at = vpa.indexOf('@');
        String local = at > 0 ? vpa.substring(0, at) : vpa;
        String domain = at > 0 ? vpa.substring(at) : "";
        if (local.length() <= 1) {
            return vpa;
        }
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + domain;
    }

    private String uniqueId() {
        return System.currentTimeMillis() + "" + (System.nanoTime() % 1000);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private java.util.Map<String, String> errors(String field, String message) {
        java.util.Map<String, String> errors = new java.util.LinkedHashMap<>();
        errors.put(field, message);
        return errors;
    }
}