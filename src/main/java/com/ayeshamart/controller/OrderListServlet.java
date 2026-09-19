package com.ayeshamart.controller;

import com.ayeshamart.model.Order;
import com.ayeshamart.model.SessionUser;
import com.ayeshamart.model.User;
import com.ayeshamart.service.OrderService;
import com.ayeshamart.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Buyer's own order history ("My Orders").
 * GET /orders -> list of the signed-in buyer's orders, newest first.
 */
@WebServlet("/orders")
public class OrderListServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireRole(request, response, User.ROLE_BUYER)) {
            return;
        }
        SessionUser user = SessionUtil.getCurrentUser(request);

        List<Order> orders = orderService.getBuyerOrders(user.getUserId());

        int pendingDelivery = 0;
        for (Order order : orders) {
            if (!order.isCancelled() && !order.isDelivered()) {
                pendingDelivery++;
            }
        }

        request.setAttribute("orders", orders);
        request.setAttribute("orderCount", orders.size());
        request.setAttribute("pendingDelivery", pendingDelivery);
        request.getRequestDispatcher("/orders.jsp").forward(request, response);
    }
}