package com.ayeshamart.util;

import java.io.File;

/**
 * Resolves the location of the Ayesha Mart data files (Excel/XLSX storage).
 *
 * Location resolution (first match wins), so no hard-coded machine-specific paths:
 * 1. Environment variable AYESHA_MART_DATA_DIR
 * 2. JVM system property ayeshaMart.dataDir (e.g. -DayeshaMart.dataDir=<dir>)
 * 3. The Tomcat base directory (system property catalina.base) -> <catalina.base>/ayesha-mart-data/data
 * 4. <user.home>/AyeshaMartData/data
 *
 * Data is kept OUTSIDE the webapp, so redeployments never wipe user data.
 */
public final class DataPathUtil {

    public static final String DATA_SUBDIR = "data";
    public static final String ENV_DATA_DIR = "AYESHA_MART_DATA_DIR";
    public static final String PROP_DATA_DIR = "ayeshaMart.dataDir";
    public static final String PROP_CATALINA_BASE = "catalina.base";

    private DataPathUtil() {
    }

    /**
     * Returns the base data directory (created if missing).
     */
    public static File getDataDirectory() {
        File directory = new File(resolveBase(), DATA_SUBDIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    private static File resolveBase() {
        String envOverride = System.getenv(ENV_DATA_DIR);
        if (isNotBlank(envOverride)) {
            return new File(envOverride.trim());
        }
        String propOverride = System.getProperty(PROP_DATA_DIR);
        if (isNotBlank(propOverride)) {
            return new File(propOverride.trim());
        }
        String catalinaBase = System.getProperty(PROP_CATALINA_BASE);
        if (isNotBlank(catalinaBase)) {
            return new File(catalinaBase.trim(), "ayesha-mart-data");
        }
        return new File(System.getProperty("user.home"), "AyeshaMartData");
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Returns the users.xlsx file used for user storage.
     */
    public static File getUsersFile() {
        return new File(getDataDirectory(), "users.xlsx");
    }

    /**
     * Returns the products.xlsx file used for product storage.
     */
    public static File getProductsFile() {
        return new File(getDataDirectory(), "products.xlsx");
    }

    /**
     * Returns the categories.xlsx file used for category storage.
     */
    public static File getCategoriesFile() {
        return new File(getDataDirectory(), "categories.xlsx");
    }

    /**
     * Returns the cart-items.xlsx file used for buyer cart storage.
     */
    public static File getCartItemsFile() {
        return new File(getDataDirectory(), "cart-items.xlsx");
    }

    /**
     * Returns the orders.xlsx file used for order and order-item storage.
     */
    public static File getOrdersFile() {
        return new File(getDataDirectory(), "orders.xlsx");
    }

    /**
     * Returns the payments.xlsx file used for demo payment records.
     */
    public static File getPaymentsFile() {
        return new File(getDataDirectory(), "payments.xlsx");
    }

    /**
     * Returns the reviews.xlsx file used for product reviews and ratings.
     */
    public static File getReviewsFile() {
        return new File(getDataDirectory(), "reviews.xlsx");
    }
}