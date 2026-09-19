package com.ayeshamart.model;

import java.io.Serializable;

/**
 * A product review left by a buyer who purchased and received the item.
 * Ratings are 1-5 stars; each buyer may leave at most one review per product.
 */
public class Review implements Serializable {

    private static final long serialVersionUID = 1L;

    private String reviewId;
    private String productId;
    private String buyerId;
    private String orderId;
    private int rating;
    private String title;
    private String comment;
    private String createdAt;

    /** Reviewer display name, resolved at read time (not persisted). */
    private String buyerName;

    public Review() {
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Review{reviewId='" + reviewId + "', productId='" + productId
                + "', buyerId='" + buyerId + "', rating=" + rating + "}";
    }
}