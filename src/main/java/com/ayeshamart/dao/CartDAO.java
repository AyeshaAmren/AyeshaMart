package com.ayeshamart.dao;

import com.ayeshamart.model.CartItem;
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
 * Persists cart lines in an Excel workbook (data/cart-items.xlsx).
 *
 * Every lookup is scoped by buyerId, so one buyer can never read or mutate another
 * buyer's cart even if a productId is forged in the browser.
 */
public class CartDAO {

    private static final Object FILE_LOCK = new Object();

    private static final String SHEET_NAME = "CartItems";
    private static final String[] HEADERS = {
            "cartItemId", "buyerId", "productId", "quantity", "addedAt", "updatedAt"
    };
    private static final Pattern ITEM_ID_PATTERN = Pattern.compile("^C(\\d+)$");
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final DataFormatter formatter = new DataFormatter();

    /**
     * Returns every cart line belonging to a buyer.
     */
    public List<CartItem> findByBuyerId(String buyerId) {
        List<CartItem> items = new ArrayList<>();
        if (buyerId == null) {
            return items;
        }
        synchronized (FILE_LOCK) {
            File file = DataPathUtil.getCartItemsFile();
            if (!file.exists()) {
                return items;
            }
            Workbook workbook = ExcelUtil.openOrCreate(file, SHEET_NAME, HEADERS);
            try {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    return items;
                }
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        continue;
                    }
                    if (buyerId.equals(formatter.formatCellValue(row.getCell(1)))) {
                        items.add(rowToItem(row));
                    }
                }
                return items;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    /**
     * Returns a buyer's line for a specific product, or null when absent.
     */
    public CartItem findItem(String buyerId, String productId) {
        if (buyerId == null || productId == null) {
            return null;
        }
        for (CartItem item : findByBuyerId(buyerId)) {
            if (productId.equals(item.getProductId())) {
                return item;
            }
        }
        return null;
    }

    /**
     * Appends a new cart line, assigning its id and timestamps.
     */
    public CartItem save(CartItem item) {
        synchronized (FILE_LOCK) {
            File file = DataPathUtil.getCartItemsFile();
            Workbook workbook = ExcelUtil.openOrCreate(file, SHEET_NAME, HEADERS);
            try {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    sheet = workbook.createSheet(SHEET_NAME);
                }
                String now = timestamp();
                item.setCartItemId(nextItemId(sheet));
                item.setAddedAt(now);
                item.setUpdatedAt(now);

                Row row = sheet.createRow(sheet.getLastRowNum() + 1);
                writeItem(row, item);

                ExcelUtil.save(file, workbook);
                return item;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    /**
     * Updates the quantity of a buyer's line. Returns true when a matching line existed.
     */
    public boolean updateQuantity(String buyerId, String productId, int quantity) {
        if (buyerId == null || productId == null) {
            return false;
        }
        synchronized (FILE_LOCK) {
            File file = DataPathUtil.getCartItemsFile();
            if (!file.exists()) {
                return false;
            }
            Workbook workbook = ExcelUtil.openOrCreate(file, SHEET_NAME, HEADERS);
            try {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    return false;
                }
                int rowIndex = findRowIndex(sheet, buyerId, productId);
                if (rowIndex < 0) {
                    return false;
                }
                Row row = sheet.getRow(rowIndex);
                setCell(row, 3, String.valueOf(quantity));
                setCell(row, 5, timestamp());
                ExcelUtil.save(file, workbook);
                return true;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    /**
     * Removes a single buyer's line.
     */
    public boolean deleteItem(String buyerId, String productId) {
        if (buyerId == null || productId == null) {
            return false;
        }
        synchronized (FILE_LOCK) {
            File file = DataPathUtil.getCartItemsFile();
            if (!file.exists()) {
                return false;
            }
            Workbook workbook = ExcelUtil.openOrCreate(file, SHEET_NAME, HEADERS);
            try {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    return false;
                }
                int rowIndex = findRowIndex(sheet, buyerId, productId);
                if (rowIndex < 0) {
                    return false;
                }
                removeRow(sheet, rowIndex);
                ExcelUtil.save(file, workbook);
                return true;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    /**
     * Empties a buyer's cart. Returns true when at least one line was removed.
     */
    public boolean deleteByBuyerId(String buyerId) {
        if (buyerId == null) {
            return false;
        }
        synchronized (FILE_LOCK) {
            File file = DataPathUtil.getCartItemsFile();
            if (!file.exists()) {
                return false;
            }
            Workbook workbook = ExcelUtil.openOrCreate(file, SHEET_NAME, HEADERS);
            try {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    return false;
                }
                boolean removed = false;
                for (int i = sheet.getLastRowNum(); i >= 1; i--) {
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        continue;
                    }
                    if (buyerId.equals(formatter.formatCellValue(row.getCell(1)))) {
                        removeRow(sheet, i);
                        removed = true;
                    }
                }
                if (removed) {
                    ExcelUtil.save(file, workbook);
                }
                return removed;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    private void writeItem(Row row, CartItem item) {
        setCell(row, 0, item.getCartItemId());
        setCell(row, 1, item.getBuyerId());
        setCell(row, 2, item.getProductId());
        setCell(row, 3, String.valueOf(item.getQuantity()));
        setCell(row, 4, item.getAddedAt());
        setCell(row, 5, item.getUpdatedAt());
    }

    private CartItem rowToItem(Row row) {
        CartItem item = new CartItem();
        item.setCartItemId(formatter.formatCellValue(row.getCell(0)));
        item.setBuyerId(formatter.formatCellValue(row.getCell(1)));
        item.setProductId(formatter.formatCellValue(row.getCell(2)));
        item.setQuantity(parseInt(formatter.formatCellValue(row.getCell(3))));
        item.setAddedAt(formatter.formatCellValue(row.getCell(4)));
        item.setUpdatedAt(formatter.formatCellValue(row.getCell(5)));
        return item;
    }

    private int findRowIndex(Sheet sheet, String buyerId, String productId) {
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            boolean sameBuyer = buyerId.equals(formatter.formatCellValue(row.getCell(1)));
            boolean sameProduct = productId.equals(formatter.formatCellValue(row.getCell(2)));
            if (sameBuyer && sameProduct) {
                return i;
            }
        }
        return -1;
    }

    private void removeRow(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row != null) {
            sheet.removeRow(row);
        }
        int lastRow = sheet.getLastRowNum();
        if (rowIndex < lastRow) {
            sheet.shiftRows(rowIndex + 1, lastRow, -1);
        }
    }

    private String nextItemId(Sheet sheet) {
        int highest = 0;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            String value = formatter.formatCellValue(row.getCell(0)).trim();
            Matcher matcher = ITEM_ID_PATTERN.matcher(value);
            if (matcher.matches()) {
                int current = Integer.parseInt(matcher.group(1));
                if (current > highest) {
                    highest = current;
                }
            }
        }
        return String.format("C%04d", highest + 1);
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

    private void setCell(Row row, int index, String value) {
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
