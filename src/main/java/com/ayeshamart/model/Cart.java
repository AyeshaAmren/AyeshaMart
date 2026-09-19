package com.ayeshamart.model;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A buyer's shopping cart. Items are loaded from storage and enriched with their
 * {@link Product} so the UI can render prices and subtotals without extra lookups.
 */
public class Cart implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Locale INDIA = new Locale("en", "IN");

    private String buyerId;
    private List<CartItem> items = new ArrayList<>();

    public Cart() {
    }

    public Cart(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = (items == null) ? new ArrayList<>() : items;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Number of distinct products in the cart.
     */
    public int getDistinctItemCount() {
        return items.size();
    }

    /**
     * Total number of units across every line, used for the header badge.
     */
    public int getTotalQuantity() {
        int total = 0;
        for (CartItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }

    public double getSubtotal() {
        double total = 0d;
        for (CartItem item : items) {
            if (item.getProduct() != null) {
                total += item.getLineTotal();
            }
        }
        return total;
    }

    public String getSubtotalDisplay() {
        NumberFormat format = NumberFormat.getNumberInstance(INDIA);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return "\u20B9 " + format.format(getSubtotal());
    }
}
