package com.ayeshamart.controller;

import com.ayeshamart.model.Order;
import com.ayeshamart.model.SessionUser;
import com.ayeshamart.model.User;
import com.ayeshamart.service.CheckoutException;
import com.ayeshamart.service.CartService;
import com.ayeshamart.service.OrderService;
import com.ayeshamart.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Buyer order detail + delivery tracking timeline.
 *
 * GET  /order/confirm?id=...  -> order placed confirmation page.
 * GET  /order?id=...          -> full order detail with tracking timeline.
 * POST /order/cancel          -> buyer cancels their own PLACED/CONFIRMED order.
 *
 * Only the owner of the order may view or cancel it.
 */
@WebServlet(urlPatterns = {"/order", "/order/confirm", "/order/cancel"})
public class OrderDetailServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();
    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireRole(request, response, User.ROLE_BUYER)) {
            return;
        }
        SessionUser user = SessionUtil.getCurrentUser(request);
        String path = request.getServletPath();

        String orderId = request.getParameter("id");
        if ((orderId == null || orderId.trim().isEmpty()) && request.getParameter("orderId") != null) {
            orderId = request.getParameter("orderId");
        }

        Order order = (orderId == null) ? null : orderService.getOrderForBuyer(orderId.trim(), user.getUserId());
        if (order == null) {
            response.sendRedirect(request.getContextPath() + "/orders?error=notfound");
            return;
        }

        request.setAttribute("order", order);
        request.setAttribute("confirmPage", "/order/confirm".equals(path));
        request.getSession(true).setAttribute("cartCount", cartService.getCartCount(user.getUserId()));
        request.getRequestDispatcher("/order-track.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireRole(request, response, User.ROLE_BUYER)) {
            return;
        }
        SessionUser user = SessionUtil.getCurrentUser(request);
        String orderId = request.getParameter("orderId");
        try {
            orderService.cancelOrder(orderId, user.getUserId());
            response.sendRedirect(request.getContextPath() + "/order/confirm?orderId=" + orderId + "&msg=cancelled");
        } catch (CheckoutException e) {
            response.sendRedirect(request.getContextPath()
                    + "/order/confirm?orderId=" + orderId + "&error=" + e.getCode());
        }
    }
}