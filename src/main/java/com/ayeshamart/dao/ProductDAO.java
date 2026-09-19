package com.ayeshamart.dao;

import com.ayeshamart.model.Product;
import com.ayeshamart.util.DataPathUtil;
import com.ayeshamart.util.ExcelUtil;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * ProductDAO persists Product objects in an Excel workbook (data/products.xlsx)
 * using Apache POI. All file access is synchronized so reads and writes stay consistent.
 */
public class ProductDAO {

    private static final Object FILE_LOCK = new Object();

    private static final String SHEET_NAME = "Products";
    private static final String[] HEADERS = {
            "productId", "sellerId", "name", "description", "category",
            "price", "stock", "image", "status", "createdAt", "updatedAt", "rating", "ratingCount",
            "subCategory"
    };
    private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("^P(\\d+)$");
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final DataFormatter formatter = new DataFormatter();

    /**
     * Appends a new product, assigning productId, createdAt and updatedAt automatically.
     */
    public Product save(Product product) {
        synchronized (FILE_LOCK) {
            File file = DataPathUtil.getProductsFile();
            Workbook workbook = ExcelUtil.openOrCreate(file, SHEET_NAME, HEADERS);
            try {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    sheet = workbook.createSheet(SHEET_NAME);
                }

                String now = timestamp();
                product.setProductId(nextProductId(sheet));
                product.setCreatedAt(now);
                product.setUpdatedAt(now);

                Row row = sheet.createRow(sheet.getLastRowNum() + 1);
                writeProduct(row, product);

                ExcelUtil.save(file, workbook);
                return product;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    /**
     * Returns every product. Empty list when nothing is stored yet.
     */
    public List<Product> findAll() {
        synchronized (FILE_LOCK) {
            List<Product> products = new ArrayList<>();
            File file = DataPathUtil.getProductsFile();
            if (!file.exists()) {
                return products;
            }
            Workbook workbook = ExcelUtil.openOrCreate(file, SHEET_NAME, HEADERS);
            try {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    return products;
                }
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        continue;
                    }
                    String productId = formatter.formatCellValue(row.getCell(0));
                    if (productId == null || productId.trim().isEmpty()) {
                        continue;
                    }
                    products.add(rowToProduct(row));
                }
                return products;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    /**
     * Returns the product with the given id, or null when not found.
     */
    public Product findById(String productId) {
        if (productId == null) {
            return null;
        }
        for (Product product : findAll()) {
            if (productId.equals(product.getProductId())) {
                return product;
            }
        }
        return null;
    }

    /**
     * True when no products are stored yet (used by the catalog seeder, which only
     * runs once on an empty store so existing products are never duplicated).
     */
    public boolean isEmpty() {
        return findAll().isEmpty();
    }

    /**
     * Returns all products belonging to a seller.
     */
    public List<Product> findBySellerId(String sellerId) {
        List<Product> result = new ArrayList<>();
        if (sellerId == null) {
            return result;
        }
        for (Product product : findAll()) {
            if (sellerId.equals(product.getSellerId())) {
                result.add(product);
            }
        }
        return result;
    }

    /**
     * Updates an existing product's editable fields. Returns true when a matching row was updated.
     */
    public boolean update(Product product) {
        if (product == null || product.getProductId() == null) {
            return false;
        }
        synchronized (FILE_LOCK) {
            File file = DataPathUtil.getProductsFile();
            if (!file.exists()) {
                return false;
            }
            Workbook workbook = ExcelUtil.openOrCreate(file, SHEET_NAME, HEADERS);
            try {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    return false;
                }
                int rowIndex = findRowIndex(sheet, product.getProductId());
                if (rowIndex < 0) {
                    return false;
                }
                Row row = sheet.getRow(rowIndex);
                product.setUpdatedAt(timestamp());
                writeProduct(row, product);
                ExcelUtil.save(file, workbook);
                return true;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    /**
     * Deletes a product row. Returns true when a matching row was removed.
     */
    public boolean delete(String productId) {
        if (productId == null) {
            return false;
        }
        synchronized (FILE_LOCK) {
            File file = DataPathUtil.getProductsFile();
            if (!file.exists()) {
                return false;
            }
            Workbook workbook = ExcelUtil.openOrCreate(file, SHEET_NAME, HEADERS);
            try {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    return false;
                }
                int rowIndex = findRowIndex(sheet, productId);
                if (rowIndex < 0) {
                    return false;
                }
                Row row = sheet.getRow(rowIndex);
                sheet.removeRow(row);
                int lastRow = sheet.getLastRowNum();
                if (rowIndex < lastRow) {
                    sheet.shiftRows(rowIndex + 1, lastRow, -1);
                }
                ExcelUtil.save(file, workbook);
                return true;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    /**
     * Reduces a product's stock by the given quantity (checked and updated in the same
     * locked block so two requests cannot oversell). Returns true when stock was reduced.
     */
    public boolean decreaseStock(String productId, int quantity) {
        if (productId == null || quantity <= 0) {
            return false;
        }
        synchronized (FILE_LOCK) {
            File file = DataPathUtil.getProductsFile();
            if (!file.exists()) {
                return false;
            }
            Workbook workbook = ExcelUtil.openOrCreate(file, SHEET_NAME, HEADERS);
            try {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    return false;
                }
                int rowIndex = findRowIndex(sheet, productId);
                if (rowIndex < 0) {
                    return false;
                }
                Row row = sheet.getRow(rowIndex);
                int current = (int) parseDouble(formatter.formatCellValue(row.getCell(6)));
                if (current < quantity) {
                    return false;
                }
                String status = formatter.formatCellValue(row.getCell(8));
                if (!Product.STATUS_ACTIVE.equalsIgnoreCase(status)) {
                    return false;
                }
                writeCell(row, 6, String.valueOf(current - quantity));
                writeCell(row, 10, timestamp());
                ExcelUtil.save(file, workbook);
                return true;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    /**
     * Restores stock for a product (used when an order is cancelled).
     */
    public boolean restoreStock(String productId, int quantity) {
        if (productId == null || quantity <= 0) {
            return false;
        }
        synchronized (FILE_LOCK) {
            File file = DataPathUtil.getProductsFile();
            if (!file.exists()) {
                return false;
            }
            Workbook workbook = ExcelUtil.openOrCreate(file, SHEET_NAME, HEADERS);
            try {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    return false;
                }
                int rowIndex = findRowIndex(sheet, productId);
                if (rowIndex < 0) {
                    return false;
                }
                Row row = sheet.getRow(rowIndex);
                int current = (int) parseDouble(formatter.formatCellValue(row.getCell(6)));
                writeCell(row, 6, String.valueOf(current + quantity));
                writeCell(row, 10, timestamp());
                ExcelUtil.save(file, workbook);
                return true;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    /**
     * Stores the aggregate rating and review count derived from the reviews workbook.
     */
    public boolean updateRating(String productId, double rating, int count) {
        if (productId == null) {
            return false;
        }
        synchronized (FILE_LOCK) {
            File file = DataPathUtil.getProductsFile();
            if (!file.exists()) {
                return false;
            }
            Workbook workbook = ExcelUtil.openOrCreate(file, SHEET_NAME, HEADERS);
            try {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    return false;
                }
                int rowIndex = findRowIndex(sheet, productId);
                if (rowIndex < 0) {
                    return false;
                }
                Row row = sheet.getRow(rowIndex);
                writeCell(row, 11, String.valueOf(rating));
                writeCell(row, 12, String.valueOf(count));
                ExcelUtil.save(file, workbook);
                return true;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    private void writeProduct(Row row, Product product) {
        writeCell(row, 0, product.getProductId());
        writeCell(row, 1, product.getSellerId());
        writeCell(row, 2, product.getName());
        writeCell(row, 3, product.getDescription());
        writeCell(row, 4, product.getCategory());
        writeCell(row, 5, String.valueOf(product.getPrice()));
        writeCell(row, 6, String.valueOf(product.getStock()));
        writeCell(row, 7, product.getImage());
        writeCell(row, 8, product.getStatus());
        writeCell(row, 9, product.getCreatedAt());
        writeCell(row, 10, product.getUpdatedAt());
        writeCell(row, 11, String.valueOf(product.getRating()));
        writeCell(row, 12, String.valueOf(product.getRatingCount()));
        writeCell(row, 13, product.getSubCategory());
    }

    private Product rowToProduct(Row row) {
        Product product = new Product();
        product.setProductId(formatter.formatCellValue(row.getCell(0)));
        product.setSellerId(formatter.formatCellValue(row.getCell(1)));
        product.setName(formatter.formatCellValue(row.getCell(2)));
        product.setDescription(formatter.formatCellValue(row.getCell(3)));
        product.setCategory(formatter.formatCellValue(row.getCell(4)));
        product.setPrice(parseDouble(formatter.formatCellValue(row.getCell(5))));
        product.setStock(parseInt(formatter.formatCellValue(row.getCell(6))));
        product.setImage(formatter.formatCellValue(row.getCell(7)));
        product.setStatus(formatter.formatCellValue(row.getCell(8)));
        product.setCreatedAt(formatter.formatCellValue(row.getCell(9)));
        product.setUpdatedAt(formatter.formatCellValue(row.getCell(10)));
        product.setRating(parseDouble(formatter.formatCellValue(row.getCell(11))));
        product.setRatingCount(parseInt(formatter.formatCellValue(row.getCell(12))));
        product.setSubCategory(formatter.formatCellValue(row.getCell(13)));
        return product;
    }

    private int findRowIndex(Sheet sheet, String productId) {
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            String value = formatter.formatCellValue(row.getCell(0));
            if (productId.equals(value)) {
                return i;
            }
        }
        return -1;
    }

    private String nextProductId(Sheet sheet) {
        int highest = 0;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            String value = formatter.formatCellValue(row.getCell(0));
            Matcher matcher = PRODUCT_ID_PATTERN.matcher(value.trim());
            if (matcher.matches()) {
                int current = Integer.parseInt(matcher.group(1));
                if (current > highest) {
                    highest = current;
                }
            }
        }
        return String.format("P%04d", highest + 1);
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return 0d;
        }
    }

    private int parseInt(String value) {
        try {
            return (int) Double.parseDouble(value.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return 0;
        }
    }

    private String timestamp() {
        return new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
    }

    private void writeCell(Row row, int index, String value) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            cell = row.createCell(index);
        }
        cell.setCellValue(value == null ? "" : value);
    }

    private void closeQuietly(Workbook workbook) {
        try {
            workbook.close();
        } catch (Exception ignored) {
        }
    }
}
