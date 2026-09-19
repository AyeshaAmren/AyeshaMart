package com.ayeshamart.controller;

import com.ayeshamart.dao.UserDAO;
import com.ayeshamart.model.Order;
import com.ayeshamart.model.User;
import com.ayeshamart.service.CheckoutException;
import com.ayeshamart.service.OrderService;
import com.ayeshamart.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin order management: every order on the platform, searchable by status, with
 * buyer names attached.
 *
 * GET  /admin/orders            -> all orders (optional ?status= filter).
 * POST /admin/order/status      -> set an order's overall status (CANCELLED restores stock).
 * POST /admin/order/payment     -> mark a COD order's payment as received.
 */
@WebServlet(urlPatterns = {"/admin/orders", "/admin/order/status", "/admin/order/payment"})
public class AdminOrdersServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();
    private final UserDAO userDao = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireRole(request, response, User.ROLE_ADMIN)) {
            return;
        }

        String statusFilter = request.getParameter("status");
        List<Order> orders = orderService.getAllOrders();
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            String wanted = statusFilter.trim();
            orders.removeIf(order -> !wanted.equalsIgnoreCase(order.getOrderStatus()));
        }

        Map<String, String> buyerNames = new HashMap<>();
        for (User user : userDao.findAll()) {
            buyerNames.put(user.getUserId(), user.getName());
        }

        double revenue = 0d;
        for (Order order : orderService.getAllOrders()) {
            if (!order.isCancelled()) {
                revenue += order.getTotal();
            }
        }

        request.setAttribute("adminOrders", orders);
        request.setAttribute("buyerNames", buyerNames);
        request.setAttribute("statusFilter", statusFilter);
        request.setAttribute("revenue", revenue);
        request.setAttribute("totalOrders", orderService.getAllOrders().size());

        request.getRequestDispatcher("/admin-orders.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireRole(request, response, User.ROLE_ADMIN)) {
            return;
        }

        String path = request.getServletPath();
        String orderId = request.getParameter("orderId");

        try {
            if ("/admin/order/status".equals(path)) {
                String status = request.getParameter("status");
                orderService.adminUpdateOrderStatus(orderId, status);
                response.sendRedirect(request.getContextPath() + "/admin/orders?msg=updated");
            } else if ("/admin/order/payment".equals(path)) {
                orderService.adminMarkPaymentReceived(orderId);
                response.sendRedirect(request.getContextPath() + "/admin/orders?msg=paid");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/orders");
            }
        } catch (CheckoutException e) {
            response.sendRedirect(request.getContextPath() + "/admin/orders?error=" + e.getCode());
        }
    }
}