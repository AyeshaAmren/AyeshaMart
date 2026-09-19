package com.ayeshamart.util;

import java.io.File;

/**
 * Resolves the location of the Ayesha Mart data files (Excel/XLSX storage).
 *
 * Location resolution:
 * 1. If the environment variable AYESHA_MART_DATA_DIR is set, data is stored there.
 * 2. Otherwise data is stored under <user.home>/AyeshaMartData/data/
 *    (outside the webapp so redeployments never wipe user data).
 */
public final class DataPathUtil {

    public static final String DATA_SUBDIR = "data";

    private DataPathUtil() {
    }

    /**
     * Returns the base data directory (created if missing).
     */
    public static File getDataDirectory() {
        String override = System.getenv("AYESHA_MART_DATA_DIR");
        File base;
        if (override != null && !override.trim().isEmpty()) {
            base = new File(override.trim());
        } else {
            base = new File(System.getProperty("user.home"), "AyeshaMartData");
        }
        File directory = new File(base, DATA_SUBDIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    /**
     * Returns the users.xlsx file used for user storage.
     */
    public static File getUsersFile() {
        return new File(getDataDirectory(), "users.xlsx");
    }
}