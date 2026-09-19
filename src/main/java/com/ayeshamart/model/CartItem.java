package com.ayeshamart.model;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * A single line in a buyer's cart: a product reference plus the chosen quantity.
 * The {@link Product} itself is resolved at read time and is not persisted here.
 */
public class CartItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Locale INDIA = new Locale("en", "IN");

    private String cartItemId;
    private String buyerId;
    private String productId;
    private int quantity;
    private String addedAt;
    private String updatedAt;
    private Product product;

    public CartItem() {
    }

    public String getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(String cartItemId) {
        this.cartItemId = cartItemId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(String addedAt) {
        this.addedAt = addedAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * True when the referenced product still exists and is listed for sale.
     */
    public boolean isAvailable() {
        return product != null && product.isActive();
    }

    public double getLineTotal() {
        if (product == null) {
            return 0d;
        }
        return product.getPrice() * quantity;
    }

    public String getLineTotalDisplay() {
        NumberFormat format = NumberFormat.getNumberInstance(INDIA);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return "\u20B9 " + format.format(getLineTotal());
    }
}
