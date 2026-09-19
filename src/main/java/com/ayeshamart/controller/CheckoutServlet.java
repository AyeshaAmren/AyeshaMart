package com.ayeshamart.controller;

import com.ayeshamart.model.Cart;
import com.ayeshamart.model.SessionUser;
import com.ayeshamart.model.User;
import com.ayeshamart.service.CartService;
import com.ayeshamart.service.CheckoutException;
import com.ayeshamart.service.OrderService;
import com.ayeshamart.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Buyer checkout.
 *
 * GET  /checkout           -> address + order summary + demo payment form (cart must not be empty)
 * POST /checkout           -> validate, run the demo payment, create the order and clear purchased lines
 *
 * The whole flow is scoped to the signed-in buyer; posted buyer/pub keys are never trusted
 * for ownership.
 */
@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();
    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireRole(request, response, User.ROLE_BUYER)) {
            return;
        }
        SessionUser user = SessionUtil.getCurrentUser(request);
        Cart cart = cartService.getCart(user.getUserId());
        refreshCartCount(request, user.getUserId());
        if (cart.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/cart?error=cartempty");
            return;
        }
        request.setAttribute("cart", cart);
        request.getRequestDispatcher("/checkout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireRole(request, response, User.ROLE_BUYER)) {
            return;
        }
        SessionUser user = SessionUtil.getCurrentUser(request);
        String buyerId = user.getUserId();

        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String pincode = request.getParameter("pincode");
        String method = request.getParameter("paymentMethod");
        String upiId = request.getParameter("upiId");
        String cardNumber = request.getParameter("cardNumber");
        String cardName = request.getParameter("cardName");

        try {
            String orderId = orderService.placeOrder(buyerId, fullName, phone, address, pincode,
                    method, upiId, cardNumber, cardName);
            refreshCartCount(request, buyerId);
            response.sendRedirect(request.getContextPath() + "/order/confirm?orderId=" + orderId);
        } catch (CheckoutException e) {
            if ("validation".equals(e.getCode())) {
                request.setAttribute("validationErrors", e.getErrors());
                request.setAttribute("oFullName", fullName);
                request.setAttribute("oPhone", phone);
                request.setAttribute("oAddress", address);
                request.setAttribute("oPincode", pincode);
                request.setAttribute("oPaymentMethod", method);
                request.setAttribute("oUpiId", upiId);
                request.setAttribute("oCardNumber", cardNumber);
                request.setAttribute("oCardName", cardName);
                request.setAttribute("cart", cartService.getCart(buyerId));
                request.getRequestDispatcher("/checkout.jsp").forward(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/checkout?error=" + e.getCode());
            }
        }
    }

    private void refreshCartCount(HttpServletRequest request, String buyerId) {
        request.getSession(true).setAttribute("cartCount", cartService.getCartCount(buyerId));
    }
}