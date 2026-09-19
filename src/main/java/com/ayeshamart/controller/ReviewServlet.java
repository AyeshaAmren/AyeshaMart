package com.ayeshamart.controller;

import com.ayeshamart.model.SessionUser;
import com.ayeshamart.model.User;
import com.ayeshamart.service.ReviewException;
import com.ayeshamart.service.ReviewService;
import com.ayeshamart.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Buyer product review creation.
 * POST /review -> validates delivery/duplicate rules and saves the review.
 */
@WebServlet("/review")
public class ReviewServlet extends HttpServlet {

    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireRole(request, response, User.ROLE_BUYER)) {
            return;
        }
        SessionUser user = SessionUtil.getCurrentUser(request);

        String productId = request.getParameter("productId");
        String rating = request.getParameter("rating");
        String title = request.getParameter("title");
        String comment = request.getParameter("comment");

        try {
            reviewService.addReview(productId, user.getUserId(), rating, title, comment);
            response.sendRedirect(request.getContextPath() + "/product?id="
                    + java.net.URLEncoder.encode(productId, java.nio.charset.StandardCharsets.UTF_8)
                    + "&msg=reviewed");
        } catch (ReviewException e) {
            response.sendRedirect(request.getContextPath() + "/product?id="
                    + java.net.URLEncoder.encode(productId != null ? productId : "", java.nio.charset.StandardCharsets.UTF_8)
                    + "&error=" + e.getCode());
        }
    }
}