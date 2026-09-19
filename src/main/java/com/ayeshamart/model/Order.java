package com.ayeshamart.model;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A buyer's order. Statuses follow a delivery pipeline
 * (PLACED -&gt; CONFIRMED -&gt; PROCESSING -&gt; SHIPPED -&gt; OUT_FOR_DELIVERY -&gt; DELIVERED)
 * and can be CANCELLED. The order header carries the shipping address, totals and a
 * summary of the payment; the individual lines live in {@link OrderItem}.
 */
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String STATUS_PLACED = "PLACED";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_SHIPPED = "SHIPPED";
    public static final String STATUS_OUT_FOR_DELIVERY = "OUT_FOR_DELIVERY";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    /** Delivery pipeline in order, used for the tracking timeline. */
    public static final String[] TIMELINE = {
            STATUS_PLACED, STATUS_CONFIRMED, STATUS_PROCESSING, STATUS_SHIPPED,
            STATUS_OUT_FOR_DELIVERY, STATUS_DELIVERED
    };

    public static final int DELIVERY_DAYS = 5;
    public static final double DELIVERY_FEE = 0d;
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final Locale INDIA = new Locale("en", "IN");

    private String orderId;
    private String buyerId;
    private String fullName;
    private String phone;
    private String address;
    private String pincode;
    private double subtotal;
    private double deliveryFee;
    private double total;
    private String paymentMethod;
    private String transactionId;
    private String paymentStatus;
    private String orderStatus;
    private String placedAt;
    private String updatedAt;
    private String expectedDelivery;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPlacedAt() {
        return placedAt;
    }

    public void setPlacedAt(String placedAt) {
        this.placedAt = placedAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getExpectedDelivery() {
        return expectedDelivery;
    }

    public void setExpectedDelivery(String expectedDelivery) {
        this.expectedDelivery = expectedDelivery;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = (items == null) ? new ArrayList<>() : items;
    }

    public boolean isCancelled() {
        return STATUS_CANCELLED.equalsIgnoreCase(orderStatus);
    }

    public boolean isDelivered() {
        return STATUS_DELIVERED.equalsIgnoreCase(orderStatus);
    }

    public boolean isPaymentPending() {
        return Payment.STATUS_PENDING.equalsIgnoreCase(paymentStatus);
    }

    public String getPaymentStatusDisplay() {
        if (Payment.STATUS_SUCCESS.equalsIgnoreCase(paymentStatus)) {
            return "Paid";
        }
        if (Payment.STATUS_PENDING.equalsIgnoreCase(paymentStatus)) {
            return "Pending (pay on delivery)";
        }
        return "Failed";
    }

    public String getOrderStatusDisplay() {
        return toTitle(orderStatus);
    }

    public String getPaymentMethodDisplay() {
        if (Payment.METHOD_COD.equalsIgnoreCase(paymentMethod)) {
            return "Cash on Delivery";
        }
        if (Payment.METHOD_UPI.equalsIgnoreCase(paymentMethod)) {
            return "UPI";
        }
        if (Payment.METHOD_CARD.equalsIgnoreCase(paymentMethod)) {
            return "Card";
        }
        return paymentMethod;
    }

    public int getTotalQuantity() {
        int totalUnits = 0;
        for (OrderItem item : items) {
            totalUnits += item.getQuantity();
        }
        return totalUnits;
    }

    /**
     * Index of the current status within the delivery timeline (-1 when cancelled).
     */
    public int getStage() {
        if (orderStatus == null) {
            return 0;
        }
        for (int i = 0; i < TIMELINE.length; i++) {
            if (TIMELINE[i].equalsIgnoreCase(orderStatus)) {
                return i;
            }
        }
        return -1;
    }

    public String getSubtotalDisplay() {
        return formatRupee(subtotal);
    }

    public String getDeliveryFeeDisplay() {
        return formatRupee(deliveryFee);
    }

    public String getTotalDisplay() {
        return formatRupee(total);
    }

    private String formatRupee(double value) {
        NumberFormat format = NumberFormat.getNumberInstance(INDIA);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return "\u20B9 " + format.format(value);
    }

    private String toTitle(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String part : value.toLowerCase().split("_")) {
            if (part.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Order{orderId='" + orderId + "', buyerId='" + buyerId
                + "', orderStatus='" + orderStatus + "', total=" + total + "}";
    }
}