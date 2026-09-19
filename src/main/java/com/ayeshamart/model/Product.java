package com.ayeshamart.model;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Represents a product listed on Ayesha Mart by a seller.
 */
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_INACTIVE = "inactive";

    private static final Locale INDIA = new Locale("en", "IN");

    private String productId;
    private String sellerId;
    private String name;
    private String description;
    private String category;
    private String subCategory;
    private double price;
    private int stock;
    private String image;
    private String status;
    private String createdAt;
    private String updatedAt;
    private double rating;
    private int ratingCount;

    public Product() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public boolean isActive() {
        return STATUS_ACTIVE.equalsIgnoreCase(status);
    }

    public boolean isInStock() {
        return stock > 0;
    }

    /**
     * A product is buyable when it is active and has stock available.
     */
    public boolean isAvailable() {
        return isActive() && isInStock();
    }

    public boolean hasImage() {
        return image != null && !image.trim().isEmpty();
    }

    /**
     * A product has a visible rating once at least one buyer has rated it.
     * Reviews are not part of this phase, so newly listed products start unrated.
     */
    public boolean hasRating() {
        return ratingCount > 0 && rating > 0;
    }

    /**
     * Rating rounded to the nearest whole star (0-5), for rendering star icons.
     */
    public int getRatingStars() {
        if (!hasRating()) {
            return 0;
        }
        int rounded = (int) Math.round(rating);
        if (rounded < 1) {
            rounded = 1;
        }
        return Math.min(rounded, 5);
    }

    public String getRatingDisplay() {
        if (!hasRating()) {
            return "0.0";
        }
        return String.format(Locale.US, "%.1f", rating);
    }

    /**
     * Price formatted for display, e.g. "Rs 1,299.00" (uses the rupee symbol via HTML entity in JSP).
     */
    public String getPriceDisplay() {
        NumberFormat format = NumberFormat.getNumberInstance(INDIA);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return "\u20B9 " + format.format(price);
    }

    /**
     * Short status label for the UI.
     */
    public String getStatusDisplay() {
        return isActive() ? "Active" : "Inactive";
    }

    @Override
    public String toString() {
        return "Product{productId='" + productId + "', sellerId='" + sellerId
                + "', name='" + name + "', category='" + category + "', subCategory='" + subCategory
                + "', price=" + price
                + ", stock=" + stock + ", status='" + status + "'}";
    }
}
