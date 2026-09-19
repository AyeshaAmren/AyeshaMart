package com.ayeshamart.service;

import com.ayeshamart.dao.CartDAO;
import com.ayeshamart.dao.ProductDAO;
import com.ayeshamart.model.Cart;
import com.ayeshamart.model.CartItem;
import com.ayeshamart.model.Product;
import java.util.List;

/**
 * Shopping cart workflow for buyers.
 *
 * Rules enforced here (never in the browser):
 * - quantity must be a positive whole number
 * - an item can never be added when the product is unavailable or out of stock
 * - the quantity in the cart can never exceed the seller's current stock
 * - every read/write is scoped to the acting buyer's id, so carts are private
 */
public class CartService {

    private final CartDAO cartDao = new CartDAO();
    private final ProductDAO productDao = new ProductDAO();

    /* ---------------- Queries ---------------- */

    /**
     * Loads the buyer's cart with product details resolved for display.
     */
    public Cart getCart(String buyerId) {
        Cart cart = new Cart(buyerId);
        if (buyerId == null) {
            return cart;
        }
        List<CartItem> items = cartDao.findByBuyerId(buyerId);
        for (CartItem item : items) {
            item.setProduct(productDao.findById(item.getProductId()));
        }
        cart.setItems(items);
        return cart;
    }

    /**
     * Total units in the buyer's cart (used for the header badge).
     */
    public int getCartCount(String buyerId) {
        int total = 0;
        for (CartItem item : cartDao.findByBuyerId(buyerId)) {
            total += item.getQuantity();
        }
        return total;
    }

    /* ---------------- Mutations ---------------- */

    public Cart addProduct(String buyerId, String productId, String quantityText) throws CartException {
        requireBuyer(buyerId);
        int quantity = parseQuantity(quantityText);

        Product product = productDao.findById(productId);
        if (product == null || !product.isActive()) {
            throw new CartException("unavailable", "This product is not available.");
        }
        if (!product.isInStock()) {
            throw new CartException("outofstock", "This product is out of stock.");
        }

        CartItem existing = cartDao.findItem(buyerId, productId);
        int already = (existing == null) ? 0 : existing.getQuantity();
        int target = already + quantity;
        if (target > product.getStock()) {
            int remaining = product.getStock() - already;
            if (remaining <= 0) {
                throw new CartException("stock",
                        "Your cart already has the maximum available quantity for this product.");
            }
            throw new CartException("stock",
                    "Only " + remaining + " more unit(s) can be added (available stock: " + product.getStock() + ").");
        }

        if (existing == null) {
            CartItem item = new CartItem();
            item.setBuyerId(buyerId);
            item.setProductId(productId);
            item.setQuantity(target);
            cartDao.save(item);
        } else {
            cartDao.updateQuantity(buyerId, productId, target);
        }
        return getCart(buyerId);
    }

    public Cart updateQuantity(String buyerId, String productId, String quantityText) throws CartException {
        requireBuyer(buyerId);
        int quantity = parseQuantity(quantityText);

        CartItem existing = cartDao.findItem(buyerId, productId);
        if (existing == null) {
            throw new CartException("notincart", "That item is not in your cart.");
        }

        Product product = productDao.findById(productId);
        if (product == null || !product.isActive()) {
            throw new CartException("unavailable", "This product is not available.");
        }
        if (!product.isInStock()) {
            throw new CartException("outofstock", "This product is out of stock.");
        }
        if (quantity > product.getStock()) {
            throw new CartException("stock",
                    "Only " + product.getStock() + " unit(s) are available in stock.");
        }

        cartDao.updateQuantity(buyerId, productId, quantity);
        return getCart(buyerId);
    }

    public Cart removeItem(String buyerId, String productId) throws CartException {
        requireBuyer(buyerId);
        if (productId == null || productId.trim().isEmpty()) {
            throw new CartException("notincart", "That item is not in your cart.");
        }
        cartDao.deleteItem(buyerId, productId.trim());
        return getCart(buyerId);
    }

    public Cart clearCart(String buyerId) throws CartException {
        requireBuyer(buyerId);
        cartDao.deleteByBuyerId(buyerId);
        return getCart(buyerId);
    }

    /* ---------------- Helpers ---------------- */

    private int parseQuantity(String value) throws CartException {
        if (value == null || value.trim().isEmpty()) {
            throw new CartException("quantity", "Please enter a quantity of at least 1.");
        }
        int quantity;
        try {
            quantity = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new CartException("quantity", "Please enter a valid quantity.");
        }
        if (quantity <= 0) {
            throw new CartException("quantity", "Quantity must be at least 1.");
        }
        return quantity;
    }

    private void requireBuyer(String buyerId) throws CartException {
        if (buyerId == null || buyerId.trim().isEmpty()) {
            throw new CartException("buyer", "You must be signed in as a buyer to use the cart.");
        }
    }
}
