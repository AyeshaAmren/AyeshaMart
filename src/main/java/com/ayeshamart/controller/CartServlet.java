package com.ayeshamart.controller;

import com.ayeshamart.model.Cart;
import com.ayeshamart.model.SessionUser;
import com.ayeshamart.model.User;
import com.ayeshamart.service.CartException;
import com.ayeshamart.service.CartService;
import com.ayeshamart.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Buyer shopping cart.
 *
 * GET  /cart -> render the buyer's own cart
 * POST /cart -> perform an action (add | update | remove | clear) and redirect back
 *
 * Every action is scoped to the signed-in buyer's id taken from the session; any
 * buyerId/productId posted by the browser is ignored for ownership purposes.
 */
@WebServlet("/cart")
public class CartServlet extends HttpServlet {

    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireRole(request, response, User.ROLE_BUYER)) {
            return;
        }
        SessionUser user = SessionUtil.getCurrentUser(request);
        Cart cart = cartService.getCart(user.getUserId());

        request.setAttribute("cart", cart);
        refreshCartCount(request, user.getUserId(), cart.getTotalQuantity());
        request.getRequestDispatcher("/cart.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireRole(request, response, User.ROLE_BUYER)) {
            return;
        }
        SessionUser user = SessionUtil.getCurrentUser(request);
        String buyerId = user.getUserId();
        String action = request.getParameter("action");

        if (action == null) {
            action = "";
        }

        try {
            switch (action) {
                case "add":
                    cartService.addProduct(buyerId, request.getParameter("productId"),
                            request.getParameter("quantity"));
                    redirect(request, response, "msg", "added", buyerId);
                    return;
                case "update":
                    cartService.updateQuantity(buyerId, request.getParameter("productId"),
                            request.getParameter("quantity"));
                    redirect(request, response, "msg", "updated", buyerId);
                    return;
                case "remove":
                    cartService.removeItem(buyerId, request.getParameter("productId"));
                    redirect(request, response, "msg", "removed", buyerId);
                    return;
                case "clear":
                    cartService.clearCart(buyerId);
                    redirect(request, response, "msg", "cleared", buyerId);
                    return;
                default:
                    redirect(request, response, "error", "unknown", buyerId);
            }
        } catch (CartException e) {
            redirect(request, response, "error", e.getCode(), buyerId);
        }
    }

    private void redirect(HttpServletRequest request, HttpServletResponse response,
                          String key, String value, String buyerId) throws IOException {
        refreshCartCount(request, buyerId, null);
        response.sendRedirect(request.getContextPath() + "/cart?" + key + "=" + value);
    }

    private void refreshCartCount(HttpServletRequest request, String buyerId, Integer knownCount) {
        int count = (knownCount != null) ? knownCount : cartService.getCartCount(buyerId);
        request.getSession(true).setAttribute("cartCount", count);
    }
}
