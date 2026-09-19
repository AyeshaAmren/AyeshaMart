package com.ayeshamart.controller;

import com.ayeshamart.dao.CategoryDAO;
import com.ayeshamart.dao.ProductDAO;
import com.ayeshamart.dao.UserDAO;
import com.ayeshamart.model.Order;
import com.ayeshamart.model.OrderItem;
import com.ayeshamart.model.Product;
import com.ayeshamart.model.User;
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
 * Admin dashboard with platform-wide statistics (users, catalogue, orders, revenue).
 * GET /admin -> live stats driven by the Excel data files.
 */
@WebServlet("/admin")
public class AdminDashboardServlet extends HttpServlet {

    private final UserDAO userDao = new UserDAO();
    private final ProductDAO productDao = new ProductDAO();
    private final CategoryDAO categoryDao = new CategoryDAO();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireRole(request, response, User.ROLE_ADMIN)) {
            return;
        }

        List<User> users = userDao.findAll();
        int buyerCount = 0;
        int sellerCount = 0;
        for (User user : users) {
            if (User.ROLE_BUYER.equalsIgnoreCase(user.getRole())) {
                buyerCount++;
            } else if (User.ROLE_SELLER.equalsIgnoreCase(user.getRole())) {
                sellerCount++;
            }
        }

        List<Product> products = productDao.findAll();
        int activeProducts = 0;
        for (Product product : products) {
            if (product.isActive()) {
                activeProducts++;
            }
        }

        List<Order> orders = orderService.getAllOrders();
        double revenue = 0d;
        int pendingPayments = 0;
        int delivered = 0;
        int inTransit = 0;
        for (Order order : orders) {
            if (order.isCancelled()) {
                continue;
            }
            revenue += order.getTotal();
            if (order.isPaymentPending()) {
                pendingPayments++;
            }
            if (order.isDelivered()) {
                delivered++;
            } else {
                for (OrderItem item : order.getItems()) {
                    if (!item.isCancelled()
                            && Order.STATUS_OUT_FOR_DELIVERY.equalsIgnoreCase(item.getItemStatus())) {
                        inTransit++;
                    }
                }
            }
        }

        request.setAttribute("userCount", users.size());
        request.setAttribute("buyerCount", buyerCount);
        request.setAttribute("sellerCount", sellerCount);
        request.setAttribute("productCount", products.size());
        request.setAttribute("activeProducts", activeProducts);
        request.setAttribute("categoryCount", categoryDao.findAll().size());
        request.setAttribute("orderCount", orders.size());
        request.setAttribute("revenue", revenue);
        request.setAttribute("pendingPayments", pendingPayments);
        request.setAttribute("deliveredOrders", delivered);
        request.setAttribute("inTransit", inTransit);
        request.setAttribute("recentOrders", orders.size() > 5 ? orders.subList(0, 5) : orders);
        request.setAttribute("recentUsers", users.size() > 5 ? users.subList(0, 5) : users);

        Map<String, String> buyerNames = new HashMap<>();
        for (User user : users) {
            buyerNames.put(user.getUserId(), user.getName());
        }
        request.setAttribute("buyerNames", buyerNames);

        request.getRequestDispatcher("/admin-dashboard.jsp").forward(request, response);
    }
}