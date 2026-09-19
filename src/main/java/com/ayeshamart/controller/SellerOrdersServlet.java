package com.ayeshamart.controller;

import com.ayeshamart.model.Order;
import com.ayeshamart.model.SessionUser;
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
import java.util.List;

/**
 * Seller's order queue.
 *
 * GET  /seller/orders          -> all orders containing at least one of the seller's items,
 *                                  filtered to the seller's own lines, plus sales stats.
 * POST /seller/order/advance   -> move one of the seller's lines to the next delivery stage.
 * POST /seller/order/cancel    -> cancel one of the seller's lines (restores stock).
 *
 * A seller can only ever touch their own lines; other sellers' items stay invisible.
 */
@WebServlet(urlPatterns = {"/seller/orders", "/seller/order/advance", "/seller/order/cancel"})
public class SellerOrdersServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireRole(request, response, User.ROLE_SELLER)) {
            return;
        }
        SessionUser user = SessionUtil.getCurrentUser(request);

        List<Order> orders = orderService.getSellerOrders(user.getUserId());

        request.setAttribute("sellerOrders", orders);
        request.setAttribute("soldUnits", orderService.getSellerUnitsSold(user.getUserId()));
        request.setAttribute("salesAmount", orderService.getSellerSalesAmount(user.getUserId()));
        request.setAttribute("pendingSellerUnits", countPendingUnits(orders));
        request.setAttribute("sellerLineCount", countSellerLines(orders));

        request.getRequestDispatcher("/seller-orders.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireRole(request, response, User.ROLE_SELLER)) {
            return;
        }
        SessionUser user = SessionUtil.getCurrentUser(request);
        String path = request.getServletPath();
        String itemId = request.getParameter("itemId");

        try {
            if ("/seller/order/advance".equals(path)) {
                orderService.advanceSellerItem(user.getUserId(), itemId);
                response.sendRedirect(request.getContextPath() + "/seller/orders?msg=advanced");
            } else if ("/seller/order/cancel".equals(path)) {
                orderService.cancelSellerItem(user.getUserId(), itemId);
                response.sendRedirect(request.getContextPath() + "/seller/orders?msg=cancelled");
            } else {
                response.sendRedirect(request.getContextPath() + "/seller/orders");
            }
        } catch (CheckoutException e) {
            response.sendRedirect(request.getContextPath() + "/seller/orders?error=" + e.getCode());
        }
    }

    private int countPendingUnits(List<Order> orders) {
        int units = 0;
        for (Order order : orders) {
            for (com.ayeshamart.model.OrderItem item : order.getItems()) {
                if (!item.isDelivered() && !item.isCancelled()) {
                    units += item.getQuantity();
                }
            }
        }
        return units;
    }

    private int countSellerLines(List<Order> orders) {
        int lines = 0;
        for (Order order : orders) {
            lines += order.getItems().size();
        }
        return lines;
    }
}