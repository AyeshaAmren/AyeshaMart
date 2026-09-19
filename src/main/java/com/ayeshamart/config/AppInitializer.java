package com.ayeshamart.config;

import com.ayeshamart.dao.UserDAO;
import com.ayeshamart.model.User;
import com.ayeshamart.service.CategoryService;
import com.ayeshamart.util.PasswordUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Application startup tasks for Ayesha Mart.
 *
 * Public registration only allows buyers and sellers, so this listener seeds a
 * single default administrator the first time the application runs. The credentials
 * can be overridden with the AYESHA_MART_ADMIN_EMAIL / AYESHA_MART_ADMIN_PASSWORD
 * environment variables.
 */
@WebListener
public class AppInitializer implements ServletContextListener {

    private static final String DEFAULT_ADMIN_EMAIL = "admin@ayeshamart.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@123";
    private static final String DEFAULT_ADMIN_NAME = "Ayesha Mart Admin";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            new CategoryService().seedDefaults();

            UserDAO userDao = new UserDAO();
            if (hasAdmin(userDao)) {
                return;
            }

            String email = envOrDefault("AYESHA_MART_ADMIN_EMAIL", DEFAULT_ADMIN_EMAIL).toLowerCase();
            String password = envOrDefault("AYESHA_MART_ADMIN_PASSWORD", DEFAULT_ADMIN_PASSWORD);

            if (userDao.emailExists(email)) {
                return;
            }

            User admin = new User();
            admin.setName(DEFAULT_ADMIN_NAME);
            admin.setEmail(email);
            admin.setPhone("+91 90000 00000");
            admin.setAddress("Ayesha Mart Head Office");
            admin.setRole(User.ROLE_ADMIN);
            admin.setPassword(PasswordUtil.hashPassword(password));
            userDao.save(admin);

            sce.getServletContext().log("Ayesha Mart: seeded default admin account '" + email + "'.");
        } catch (RuntimeException e) {
            sce.getServletContext().log("Ayesha Mart: could not seed the default admin account.", e);
        }
    }

    private boolean hasAdmin(UserDAO userDao) {
        for (User user : userDao.findAll()) {
            if (User.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
                return true;
            }
        }
        return false;
    }

    private String envOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }
}
