package com.ayeshamart.service;

import com.ayeshamart.dao.ProductDAO;
import com.ayeshamart.dao.ReviewDAO;
import com.ayeshamart.dao.UserDAO;
import com.ayeshamart.model.Order;
import com.ayeshamart.model.OrderItem;
import com.ayeshamart.model.Product;
import com.ayeshamart.model.Review;
import com.ayeshamart.model.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Product review/rating workflow.
 *
 * Only a buyer who purchased AND received the product (a DELIVERED order line) can
 * review it, and each buyer may review a product at most once. When a review is saved
 * the product's aggregate rating and count are recomputed in products.xlsx.
 */
public class ReviewService {

    private final ReviewDAO reviewDao = new ReviewDAO();
    private final ProductDAO productDao = new ProductDAO();
    private final UserDAO userDao = new UserDAO();
    private final OrderService orderService = new OrderService();

    /**
     * Creates a review after validating purchase, delivery and duplicate rules.
     *
     * @return the saved review
     */
    public Review addReview(String productId, String buyerId, String ratingText,
                            String title, String comment) throws ReviewException {
        if (productId == null || productId.trim().isEmpty()) {
            throw new ReviewException("product", "Product not found.");
        }
        requireBuyer(buyerId);

        Product product = productDao.findById(productId.trim());
        if (product == null) {
            throw new ReviewException("product", "That product no longer exists.");
        }

        if (reviewDao.findByBuyerAndProduct(buyerId, productId.trim()) != null) {
            throw new ReviewException("duplicate", "You have already reviewed this product.");
        }

        int rating = parseRating(ratingText);
        String titleValue = validateTitle(title);
        String commentValue = validateComment(comment);

        String orderId = findDeliveredOrderItem(buyerId, productId.trim());
        if (orderId == null) {
            throw new ReviewException("notDelivered",
                    "You can review this product only after you have purchased and received it.");
        }

        Review review = new Review();
        review.setProductId(productId.trim());
        review.setBuyerId(buyerId);
        review.setOrderId(orderId);
        review.setRating(rating);
        review.setTitle(titleValue);
        review.setComment(commentValue);
        reviewDao.save(review);

        refreshProductRating(productId.trim());
        return review;
    }

    /**
     * Returns every review for a product with the reviewer name attached,
     * newest first.
     */
    public List<Review> getProductReviews(String productId) {
        List<Review> reviews = reviewDao.findByProductId(productId);
        Map<String, String> names = new HashMap<>();
        for (User user : userDao.findAll()) {
            names.put(user.getUserId(), user.getName());
        }
        for (Review review : reviews) {
            review.setBuyerName(names.getOrDefault(review.getBuyerId(), "Verified Buyer"));
        }
        return reviews;
    }

    /**
     * True when a review already exists from this buyer for this product.
     */
    public boolean hasReviewed(String buyerId, String productId) {
        if (buyerId == null) {
            return false;
        }
        return reviewDao.findByBuyerAndProduct(buyerId, productId) != null;
    }

    /**
     * Returns the buyer's review count (used by the buyer dashboard).
     */
    public int getBuyerReviewCount(String buyerId) {
        if (buyerId == null) {
            return 0;
        }
        int count = 0;
        for (Review review : reviewDao.findAll()) {
            if (buyerId.equals(review.getBuyerId())) {
                count++;
            }
        }
        return count;
    }

    /**
     * True when this buyer can write a review for the product: they purchased it AND it was
     * delivered, and they have not already reviewed it.
     */
    public boolean canReview(String buyerId, String productId) {
        if (buyerId == null || productId == null) {
            return false;
        }
        if (reviewDao.findByBuyerAndProduct(buyerId, productId) != null) {
            return false;
        }
        return findDeliveredOrderItem(buyerId, productId) != null;
    }

    /**
     * Recomputed the product's aggregate rating/ratingCount from all its reviews.
     */
    private void refreshProductRating(String productId) {
        List<Review> reviews = reviewDao.findByProductId(productId);
        if (reviews.isEmpty()) {
            productDao.updateRating(productId, 0d, 0);
            return;
        }
        double sum = 0d;
        for (Review review : reviews) {
            sum += review.getRating();
        }
        productDao.updateRating(productId, sum / reviews.size(), reviews.size());
    }

    /**
     * Finds the id of an order in which this buyer has a DELIVERED line for the product.
     */
    private String findDeliveredOrderItem(String buyerId, String productId) {
        for (Order order : orderService.getBuyerOrders(buyerId)) {
            if (order.isCancelled()) {
                continue;
            }
            for (OrderItem item : order.getItems()) {
                if (productId.equals(item.getProductId()) && item.isDelivered()) {
                    return order.getOrderId();
                }
            }
        }
        return null;
    }

    private int parseRating(String ratingText) throws ReviewException {
        if (ratingText == null || ratingText.trim().isEmpty()) {
            throw new ReviewException("rating", "Please select a star rating.");
        }
        int rating;
        try {
            rating = Integer.parseInt(ratingText.trim());
        } catch (NumberFormatException e) {
            throw new ReviewException("rating", "Please select a rating between 1 and 5 stars.");
        }
        if (rating < 1 || rating > 5) {
            throw new ReviewException("rating", "Please select a rating between 1 and 5 stars.");
        }
        return rating;
    }

    private String validateTitle(String title) throws ReviewException {
        String value = trimToNull(title);
        if (value == null) {
            throw new ReviewException("title", "Please add a short review title.");
        }
        if (value.length() < 3 || value.length() > 120) {
            throw new ReviewException("title", "Title must be between 3 and 120 characters.");
        }
        return value;
    }

    private String validateComment(String comment) throws ReviewException {
        String value = trimToNull(comment);
        if (value == null) {
            throw new ReviewException("comment", "Please write a short review comment.");
        }
        if (value.length() < 5 || value.length() > 2000) {
            throw new ReviewException("comment", "Comment must be between 5 and 2000 characters.");
        }
        return value;
    }

    private void requireBuyer(String buyerId) throws ReviewException {
        if (buyerId == null || buyerId.trim().isEmpty()) {
            throw new ReviewException("buyer", "You must be signed in as a buyer to review a product.");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}