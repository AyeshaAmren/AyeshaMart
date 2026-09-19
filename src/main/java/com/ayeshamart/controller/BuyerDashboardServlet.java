package com.ayeshamart.controller;

import com.ayeshamart.model.Order;
import com.ayeshamart.model.OrderItem;
import com.ayeshamart.model.SessionUser;
import com.ayeshamart.model.User;
import com.ayeshamart.service.CartService;
import com.ayeshamart.service.OrderService;
import com.ayeshamart.service.ReviewService;
import com.ayeshamart.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Buyer dashboard.
 * GET /buyer -> cart, orders and review statistics plus the 5 most recent orders.
 */
@WebServlet("/buyer")
public class BuyerDashboardServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();
    private final CartService cartService = new CartService();
    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireRole(request, response, User.ROLE_BUYER)) {
            return;
        }
        SessionUser user = SessionUtil.getCurrentUser(request);

        List<Order> orders = orderService.getBuyerOrders(user.getUserId());

        boolean hasDeliveredItems = false;
        int pendingDelivery = 0;
        int totalSpent = 0;
        List<Order> recent = orders.size() > 5 ? orders.subList(0, 5) : orders;
        for (Order order : orders) {
            if (!order.isCancelled()) {
                totalSpent += order.getTotal();
                if (!order.isDelivered()) {
                    pendingDelivery++;
                } else {
                    hasDeliveredItems = true;
                }
            }
        }

        request.setAttribute("recentOrders", recent);
        request.setAttribute("orderCount", orders.size());
        request.setAttribute("cartItemCount", cartService.getCartCount(user.getUserId()));
        request.setAttribute("reviewsWritten", reviewService.getBuyerReviewCount(user.getUserId()));
        request.setAttribute("pendingDelivery", pendingDelivery);
        request.setAttribute("totalSpent", totalSpent);
        request.setAttribute("hasDeliveredItems", hasDeliveredItems);
        request.getSession(true).setAttribute("cartCount", cartService.getCartCount(user.getUserId()));

        request.getRequestDispatcher("/buyer-dashboard.jsp").forward(request, response);
    }
}