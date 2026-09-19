package com.ayeshamart.service;

import com.ayeshamart.dao.CartDAO;
import com.ayeshamart.dao.OrderDAO;
import com.ayeshamart.dao.PaymentDAO;
import com.ayeshamart.dao.ProductDAO;
import com.ayeshamart.model.Cart;
import com.ayeshamart.model.CartItem;
import com.ayeshamart.model.Order;
import com.ayeshamart.model.OrderItem;
import com.ayeshamart.model.Payment;
import com.ayeshamart.model.Product;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Order workflow for Ayesha Mart.
 *
 * Checkout validates the address/phone, re-validates stock at purchase time, snaps a
 * per-item price/name copy, reduces the live stock, guards with per-product locks and
 * clears the purchased cart lines. Order status is kept per line item (multi-seller
 * orders) and aggregated to an order header status for the buyer's tracking timeline.
 *
 * Access control lives here too: buyers can only read/cancel their own orders, sellers
 * only their own product lines, and admins operate on the whole order.
 */
public class OrderService {

    private final OrderDAO orderDao = new OrderDAO();
    private final CartDAO cartDao = new CartDAO();
    private final ProductDAO productDao = new ProductDAO();
    private final PaymentDAO paymentDao = new PaymentDAO();
    private final CartService cartService = new CartService();
    private final PaymentService paymentService = new PaymentService();

    private static final DateTimeFormatter DELIVERY_FORMAT = DateTimeFormatter.ofPattern(Order.DATE_FORMAT);
    private static final String PHONE_PATTERN = "^\\+?[0-9][0-9\\s\\-()]{6,14}$";
    private static final String PINCODE_PATTERN = "^[0-9]{6}$";

    /* ---------------- Checkout ---------------- */

    /**
     * Validates input, runs the demo payment and - only when successful/COD - creates the
     * order and its line items, reduces stock and clears the purchased cart lines.
     *
     * @return the created order's id
     */
    public String placeOrder(String buyerId, String fullName, String phone, String address,
                             String pincode, String method, String upiId, String cardNumber, String cardName)
            throws CheckoutException {
        requireBuyer(buyerId);

        Map<String, String> errors = new LinkedHashMap<>();
        validateAddress(fullName, phone, address, pincode, errors);
        Payment payment;
        try {
            payment = paymentService.prepare(method, upiId, cardNumber, cardName, 0d);
        } catch (CheckoutException e) {
            errors.putAll(e.getErrors());
            payment = null;
        }
        if (!errors.isEmpty()) {
            throw new CheckoutException(errors);
        }

        Cart cart = cartService.getCart(buyerId);
        if (cart.isEmpty()) {
            throw new CheckoutException("cartempty", "Your cart is empty. Add products before checking out.");
        }

        double subtotal = cart.getSubtotal();
        List<OrderItem> items = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product == null || !product.isActive()) {
                throw new CheckoutException("unavailable", "\"" + safeName(item) + "\" is no longer available.");
            }
            if (item.getQuantity() <= 0) {
                throw new CheckoutException("stock", "Invalid quantity for \"" + safeName(item) + "\".");
            }
            if (product.getStock() < item.getQuantity()) {
                throw new CheckoutException("stock",
                        "Only " + product.getStock() + " unit(s) of \"" + product.getName()
                                + "\" are in stock right now. Please update your cart.");
            }
            OrderItem line = new OrderItem();
            line.setProductId(product.getProductId());
            line.setSellerId(product.getSellerId());
            line.setProductName(product.getName());
            line.setPrice(product.getPrice());
            line.setQuantity(item.getQuantity());
            line.setItemStatus(Order.STATUS_PLACED);
            items.add(line);
        }

        if (payment != null && (payment.isFailed())) {
            throw new CheckoutException("paymentfailed",
                    "The demo payment failed. Please try a different payment method. Your cart is untouched.");
        }

        double total = subtotal + Order.DELIVERY_FEE;

        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setFullName(trimToNull(fullName));
        order.setPhone(trimToNull(phone));
        order.setAddress(trimToNull(address));
        order.setPincode(trimToNull(pincode));
        order.setSubtotal(subtotal);
        order.setDeliveryFee(Order.DELIVERY_FEE);
        order.setTotal(total);
        order.setPaymentMethod(payment.getMethod());
        order.setTransactionId(payment.getTransactionId());
        order.setPaymentStatus(payment.getStatus());
        order.setOrderStatus(Order.STATUS_PLACED);
        order.setExpectedDelivery(LocalDate.now().plusDays(Order.DELIVERY_DAYS).format(DELIVERY_FORMAT));

        boolean stockReduced = true;
        try {
            stockReduced = reduceStock(items);
            if (!stockReduced) {
                throw new CheckoutException("stock", "Stock changed while checking out. Please review your cart and try again.");
            }
            orderDao.save(order, items);
            cartDao.deleteByBuyerId(buyerId);
            payment.setOrderId(order.getOrderId());
            paymentDao.save(payment);
            return order.getOrderId();
        } catch (CheckoutException e) {
            if (stockReduced) {
                restoreStock(items);
            }
            throw e;
        } catch (RuntimeException e) {
            if (stockReduced) {
                restoreStock(items);
            }
            throw e;
        }
    }

    /* ---------------- Buyer queries ---------------- */

    public List<Order> getBuyerOrders(String buyerId) {
        if (buyerId == null) {
            return new ArrayList<>();
        }
        return orderDao.findByBuyerId(buyerId);
    }

    /* ---------------- Admin queries ---------------- */

    public List<Order> getAllOrders() {
        return orderDao.findAll();
    }

    /**
     * Returns an order only when it belongs to the given buyer (ownership check).
     */
    public Order getOrderForBuyer(String orderId, String buyerId) {
        if (orderId == null || buyerId == null) {
            return null;
        }
        Order order = orderDao.findById(orderId);
        if (order == null || !buyerId.equals(order.getBuyerId())) {
            return null;
        }
        return order;
    }

    /**
     * Cancels a buyer's own order, but only while it is still in the early stage
     * (PLACED or CONFIRMED) and no line has shipped. Stock is restored.
     */
    public void cancelOrder(String orderId, String buyerId) throws CheckoutException {
        requireBuyer(buyerId);
        Order order = getOrderForBuyer(orderId, buyerId);
        if (order == null) {
            throw new CheckoutException("notfound", "Order not found.");
        }
        if (order.isCancelled()) {
            throw new CheckoutException("already", "This order is already cancelled.");
        }
        if (order.getStage() > 1) {
            throw new CheckoutException("tooLate", "This order has already been processed and can no longer be cancelled.");
        }
        for (OrderItem item : order.getItems()) {
            orderDao.updateItemStatus(item.getOrderItemId(), Order.STATUS_CANCELLED);
            productDao.restoreStock(item.getProductId(), item.getQuantity());
        }
        orderDao.updateStatus(orderId, Order.STATUS_CANCELLED);
    }

    /* ---------------- Seller queries & updates ---------------- */

    /**
     * Orders containing at least one line sold by the seller. Each returned order carries
     * only that seller's own lines, so sellers never see other sellers' data.
     */
    public List<Order> getSellerOrders(String sellerId) {
        List<Order> result = new ArrayList<>();
        if (sellerId == null) {
            return result;
        }
        for (Order order : orderDao.findAll()) {
            List<OrderItem> mine = new ArrayList<>();
            for (OrderItem item : order.getItems()) {
                if (sellerId.equals(item.getSellerId())) {
                    mine.add(item);
                }
            }
            if (!mine.isEmpty()) {
                Order copy = order;
                copy.setItems(mine);
                result.add(copy);
            }
        }
        return result;
    }

    public int getSellerUnitsSold(String sellerId) {
        int units = 0;
        for (Order order : getSellerOrders(sellerId)) {
            for (OrderItem item : order.getItems()) {
                if (!item.isCancelled()) {
                    units += item.getQuantity();
                }
            }
        }
        return units;
    }

    public double getSellerSalesAmount(String sellerId) {
        double total = 0d;
        for (Order order : getSellerOrders(sellerId)) {
            for (OrderItem item : order.getItems()) {
                if (!item.isCancelled()) {
                    total += item.getLineTotal();
                }
            }
        }
        return total;
    }

    /**
     * A seller advances one of their own lines to the next delivery stage.
     */
    public void advanceSellerItem(String sellerId, String orderItemId) throws CheckoutException {
        ItemGuard guard = guardSellerItem(sellerId, orderItemId);
        OrderItem item = guard.item;
        String next = nextStage(item.getItemStatus());
        if (next == null) {
            throw new CheckoutException("terminal", "This item has already reached its final status.");
        }
        orderDao.updateItemStatus(orderItemId, next);
        recomputeOrderStatus(guard.order.getOrderId());
    }

    /**
     * A seller cancels one of their own lines (stock is returned to the product).
     */
    public void cancelSellerItem(String sellerId, String orderItemId) throws CheckoutException {
        ItemGuard guard = guardSellerItem(sellerId, orderItemId);
        OrderItem item = guard.item;
        if (item.isCancelled()) {
            throw new CheckoutException("already", "This line is already cancelled.");
        }
        if (item.isDelivered()) {
            throw new CheckoutException("terminal", "Delivered items cannot be cancelled.");
        }
        orderDao.updateItemStatus(orderItemId, Order.STATUS_CANCELLED);
        productDao.restoreStock(item.getProductId(), item.getQuantity());
        recomputeOrderStatus(guard.order.getOrderId());
    }

    /* ---------------- Admin updates ---------------- */

    public void adminUpdateOrderStatus(String orderId, String status) throws CheckoutException {
        if (!isValidOrderStatus(status)) {
            throw new CheckoutException("status", "That is not a valid order status.");
        }
        Order order = orderDao.findById(orderId);
        if (order == null) {
            throw new CheckoutException("notfound", "Order not found.");
        }
        if (Order.STATUS_CANCELLED.equalsIgnoreCase(status)) {
            for (OrderItem item : order.getItems()) {
                orderDao.updateItemStatus(item.getOrderItemId(), Order.STATUS_CANCELLED);
                productDao.restoreStock(item.getProductId(), item.getQuantity());
            }
            orderDao.updateStatus(orderId, Order.STATUS_CANCELLED);
        } else {
            for (OrderItem item : order.getItems()) {
                orderDao.updateItemStatus(item.getOrderItemId(), status);
            }
            orderDao.updateStatus(orderId, status);
        }
    }

    public void adminMarkPaymentReceived(String orderId) throws CheckoutException {
        Order order = orderDao.findById(orderId);
        if (order == null) {
            throw new CheckoutException("notfound", "Order not found.");
        }
        orderDao.updatePaymentStatus(orderId, Payment.STATUS_SUCCESS);
        paymentDao.updateStatus(orderId, Payment.STATUS_SUCCESS);
    }

    /* ---------------- Aggregates ---------------- */

    /**
     * Recomputed order header status from its lines: all cancelled -> CANCELLED, otherwise
     * the earliest stage still in progress.
     */
    public void recomputeOrderStatus(String orderId) {
        Order order = orderDao.findById(orderId);
        if (order == null || order.getItems().isEmpty()) {
            return;
        }
        boolean allCancelled = true;
        int minStage = Integer.MAX_VALUE;
        for (OrderItem item : order.getItems()) {
            if (!item.isCancelled()) {
                allCancelled = false;
                minStage = Math.min(minStage, stageOf(item.getItemStatus()));
            }
        }
        if (allCancelled || minStage == Integer.MAX_VALUE) {
            orderDao.updateStatus(orderId, Order.STATUS_CANCELLED);
        } else {
            orderDao.updateStatus(orderId, Order.TIMELINE[minStage]);
        }
    }

    /* ---------------- Helpers ---------------- */

    private ItemGuard guardSellerItem(String sellerId, String orderItemId) throws CheckoutException {
        if (sellerId == null || orderItemId == null || orderItemId.trim().isEmpty()) {
            throw new CheckoutException("notfound", "That order line was not found.");
        }
        for (Order order : orderDao.findAll()) {
            for (OrderItem item : order.getItems()) {
                if (orderItemId.equals(item.getOrderItemId())) {
                    if (!sellerId.equals(item.getSellerId())) {
                        throw new CheckoutException("forbidden", "You can only manage your own order lines.");
                    }
                    return new ItemGuard(order, item);
                }
            }
        }
        throw new CheckoutException("notfound", "That order line was not found.");
    }

    private boolean reduceStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            if (!productDao.decreaseStock(item.getProductId(), item.getQuantity())) {
                return false;
            }
        }
        return true;
    }

    private void restoreStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            productDao.restoreStock(item.getProductId(), item.getQuantity());
        }
    }

    private void validateAddress(String fullName, String phone, String address, String pincode,
                                 Map<String, String> errors) {
        String name = trimToNull(fullName);
        if (name == null) {
            errors.put("fullName", "Full name is required.");
        } else if (name.length() < 2) {
            errors.put("fullName", "Name must be at least 2 characters long.");
        }

        String phoneValue = trimToNull(phone);
        if (phoneValue == null) {
            errors.put("phone", "Phone number is required.");
        } else if (!phoneValue.matches(PHONE_PATTERN)) {
            errors.put("phone", "Please enter a valid phone number.");
        }

        String addressValue = trimToNull(address);
        if (addressValue == null) {
            errors.put("address", "Delivery address is required.");
        } else if (addressValue.length() < 8) {
            errors.put("address", "Please enter a complete delivery address.");
        }

        String pin = trimToNull(pincode);
        if (pin == null) {
            errors.put("pincode", "PIN / postal code is required.");
        } else if (!pin.matches(PINCODE_PATTERN)) {
            errors.put("pincode", "Enter a valid 6-digit PIN code.");
        }
    }

    private String nextStage(String current) {
        int stage = stageOf(current);
        if (stage < 0) {
            return null;
        }
        int next = stage + 1;
        if (next >= Order.TIMELINE.length) {
            return null;
        }
        return Order.TIMELINE[next];
    }

    private int stageOf(String status) {
        if (status == null) {
            return 0;
        }
        for (int i = 0; i < Order.TIMELINE.length; i++) {
            if (Order.TIMELINE[i].equalsIgnoreCase(status)) {
                return i;
            }
        }
        return Integer.MAX_VALUE;
    }

    private boolean isValidOrderStatus(String status) {
        if (Order.STATUS_CANCELLED.equalsIgnoreCase(status)) {
            return true;
        }
        for (String candidate : Order.TIMELINE) {
            if (candidate.equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    private void requireBuyer(String buyerId) throws CheckoutException {
        if (buyerId == null || buyerId.trim().isEmpty()) {
            throw new CheckoutException("buyer", "You must be signed in as a buyer to place an order.");
        }
    }

    private String safeName(CartItem item) {
        Product product = item.getProduct();
        return product != null && product.getName() != null ? product.getName() : "Product";
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static final class ItemGuard {
        private final Order order;
        private final OrderItem item;

        private ItemGuard(Order order, OrderItem item) {
            this.order = order;
            this.item = item;
        }
    }
}