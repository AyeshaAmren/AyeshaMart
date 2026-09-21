package com.ayeshamart.config;

import com.ayeshamart.dao.ProductDAO;
import com.ayeshamart.dao.UserDAO;
import com.ayeshamart.model.Product;
import com.ayeshamart.model.User;
import com.ayeshamart.service.CatalogSeeder;
import com.ayeshamart.service.CategoryService;
import com.ayeshamart.util.PasswordUtil;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.io.File;
import java.util.List;

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
            new CatalogSeeder().seed();
            rePointProductImages(sce.getServletContext());
            new CatalogSeeder().applyBookCovers();

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

    private void rePointProductImages(ServletContext ctx) {
        String imagesDir = ctx.getRealPath("/images/products");
        if (imagesDir == null) {
            return;
        }
        File dir = new File(imagesDir);
        if (!dir.isDirectory()) {
            return;
        }
        ProductDAO dao = new ProductDAO();
        int moved = 0;
        for (Product product : dao.findAll()) {
            String id = product.getProductId();
            if (id == null) {
                continue;
            }
            String current = product.getImage() == null ? "" : product.getImage().trim();
            String localPath = "images/products/" + id + ".svg";
            if (localPath.equals(current)) {
                continue;
            }
            boolean isLegacyMarker = current.isEmpty()
                    || current.equals("product-image?id=" + id)
                    || current.startsWith("product-image?id=");
            if (!isLegacyMarker) {
                continue;
            }
            File imageFile = new File(dir, id + ".svg");
            if (!imageFile.isFile()) {
                continue;
            }
            product.setImage(localPath);
            if (dao.update(product)) {
                moved++;
            }
        }
        if (moved > 0) {
            ctx.log("Ayesha Mart: re-pointed " + moved + " product image(s) to local SVGs.");
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
