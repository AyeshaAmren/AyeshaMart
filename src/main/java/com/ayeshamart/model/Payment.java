package com.ayeshamart.model;

import java.io.Serializable;

/**
 * A demo payment record for an order.
 *
 * Ayesha Mart never stores real payment credentials: only the method, a masked summary
 * (e.g. "UPI u***@ybl" or "Card **** 4242"), the transaction id and the outcome are kept.
 */
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String METHOD_COD = "COD";
    public static final String METHOD_UPI = "UPI";
    public static final String METHOD_CARD = "CARD";

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    private String paymentId;
    private String orderId;
    private String buyerId;
    private String method;
    private String transactionId;
    private double amount;
    private String maskedDetails;
    private String status;
    private String createdAt;

    public Payment() {
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getMaskedDetails() {
        return maskedDetails;
    }

    public void setMaskedDetails(String maskedDetails) {
        this.maskedDetails = maskedDetails;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isSuccess() {
        return STATUS_SUCCESS.equalsIgnoreCase(status);
    }

    public boolean isPending() {
        return STATUS_PENDING.equalsIgnoreCase(status);
    }

    public boolean isFailed() {
        return STATUS_FAILED.equalsIgnoreCase(status);
    }

    public String getMethodDisplay() {
        if (METHOD_COD.equalsIgnoreCase(method)) {
            return "Cash on Delivery";
        }
        if (METHOD_UPI.equalsIgnoreCase(method)) {
            return "UPI";
        }
        if (METHOD_CARD.equalsIgnoreCase(method)) {
            return "Card";
        }
        return method;
    }

    public String getStatusDisplay() {
        if (STATUS_SUCCESS.equalsIgnoreCase(status)) {
            return "Success";
        }
        if (STATUS_PENDING.equalsIgnoreCase(status)) {
            return "Pending";
        }
        return "Failed";
    }

    @Override
    public String toString() {
        return "Payment{paymentId='" + paymentId + "', orderId='" + orderId
                + "', method='" + method + "', status='" + status + "'}";
    }
}