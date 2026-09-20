package com.ayeshamart.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Stores product picture files that sellers upload. Files live OUTSIDE the webapp
 * under {@literal <data>/images/products/<productId>.<ext>} so redeployments never
 * wipe them (mirrors the Excel data-files approach).
 *
 * The {@link com.ayeshamart.controller.ProductImageServlet} serves these files when
 * present; otherwise it falls back to a generated branded SVG tile, so every product
 * always shows an image.
 */
public final class ProductImageStore {

    public static final String IMAGES_SUBDIR = "images";
    public static final String PRODUCTS_SUBDIR = "products";

    private ProductImageStore() {
    }

    /**
     * Returns the folder where uploaded product pictures are kept (created if missing).
     */
    public static File getImageDir() {
        File directory = new File(new File(DataPathUtil.getDataDirectory(), IMAGES_SUBDIR), PRODUCTS_SUBDIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    /**
     * Returns the stored picture file for a product, or null when none has been
     * uploaded. The extension is resolved by scanning the folder, so callers never
     * need to know what was stored before.
     */
    public static File findFile(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return null;
        }
        String safeId = productId.trim();
        File dir = getImageDir();
        File[] files = dir.listFiles((d, name) -> name.startsWith(safeId + "."));
        if (files == null) {
            return null;
        }
        for (File file : files) {
            if (file.isFile() && file.length() > 0) {
                return file;
            }
        }
        return null;
    }

    /**
     * Replaces (or creates) the stored picture for a product. The previous file, if
     * any, is deleted so only one picture is ever kept per product.
     */
    public static void save(String productId, String extension, InputStream data) throws IOException {
        File dir = getImageDir();
        String safeId = productId.trim();
        File existing = findFile(safeId);
        if (existing != null) {
            existing.delete();
        }
        File target = new File(dir, safeId + "." + extension);
        Files.copy(data, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Deletes the stored picture for a product (used when a product is removed).
     */
    public static void delete(String productId) {
        File existing = findFile(productId);
        if (existing != null) {
            existing.delete();
        }
    }
}