package com.ayeshamart.model;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * A single line inside an {@link Order}: a snapshot of the product (name and price at
 * purchase time) plus the seller who fulfilled it and that line's own delivery status.
 *
 * Per-line status lets a multi-seller order be progressed by each seller independently;
 * the order header status is then recomputed from the lines.
 */
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Locale INDIA = new Locale("en", "IN");

    private String orderItemId;
    private String orderId;
    private String productId;
    private String sellerId;
    private String productName;
    private double price;
    private int quantity;
    private String itemStatus;

    public OrderItem() {
    }

    public String getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }

    public boolean isCancelled() {
        return Order.STATUS_CANCELLED.equalsIgnoreCase(itemStatus);
    }

    public boolean isDelivered() {
        return Order.STATUS_DELIVERED.equalsIgnoreCase(itemStatus);
    }

    public String getItemStatusDisplay() {
        String value = itemStatus == null ? Order.STATUS_PLACED : itemStatus;
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

    public double getLineTotal() {
        return price * quantity;
    }

    public String getLineTotalDisplay() {
        NumberFormat format = NumberFormat.getNumberInstance(INDIA);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return "\u20B9 " + format.format(getLineTotal());
    }

    public String getPriceDisplay() {
        NumberFormat format = NumberFormat.getNumberInstance(INDIA);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return "\u20B9 " + format.format(price);
    }

    @Override
    public String toString() {
        return "OrderItem{orderItemId='" + orderItemId + "', productId='" + productId
                + "', quantity=" + quantity + ", itemStatus='" + itemStatus + "'}";
    }
}