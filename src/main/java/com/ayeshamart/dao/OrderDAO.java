package com.ayeshamart.dao;

import com.ayeshamart.model.Order;
import com.ayeshamart.model.OrderItem;
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
 * Persists orders and their line items in an Excel workbook (data/orders.xlsx).
 *
 * The workbook holds two sheets - "Orders" (header/address/totals/status) and
 * "OrderItems" (per-line snapshots with seller + item status). All file access is
 * synchronized so reads and writes stay consistent.
 */
public class OrderDAO {

    private static final Object FILE_LOCK = new Object();

    private static final String ORDER_SHEET = "Orders";
    private static final String ITEM_SHEET = "OrderItems";

    private static final String[] ORDER_HEADERS = {
            "orderId", "buyerId", "fullName", "phone", "address", "pincode",
            "subtotal", "deliveryFee", "total", "paymentMethod", "transactionId",
            "paymentStatus", "orderStatus", "placedAt", "updatedAt", "expectedDelivery"
    };

    private static final String[] ITEM_HEADERS = {
            "orderItemId", "orderId", "productId", "sellerId", "productName",
            "price", "quantity", "itemStatus"
    };

    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("^O(\\d+)$");
    private static final Pattern ITEM_ID_PATTERN = Pattern.compile("^OI(\\d+)$");
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final DataFormatter formatter = new DataFormatter();

    /**
     * Persists a new order header plus all its line items in one atomic save.
     */
    public Order save(Order order, List<OrderItem> items) {
        synchronized (FILE_LOCK) {
            File file = DataPathUtil.getOrdersFile();
            Workbook workbook = ExcelUtil.openOrCreate(file, ORDER_SHEET, ORDER_HEADERS);
            try {
                Sheet orderSheet = workbook.getSheet(ORDER_SHEET);
                Sheet itemSheet = workbook.getSheet(ITEM_SHEET);
                if (orderSheet == null) {
                    orderSheet = workbook.createSheet(ORDER_SHEET);
                    ExcelUtil.writeHeaderRow(orderSheet, ORDER_HEADERS);
                }
                if (itemSheet == null) {
                    itemSheet = workbook.createSheet(ITEM_SHEET);
                    ExcelUtil.writeHeaderRow(itemSheet, ITEM_HEADERS);
                }

                String now = timestamp();
                order.setOrderId(nextOrderId(orderSheet));
                order.setPlacedAt(now);
                order.setUpdatedAt(now);

                Row orderRow = orderSheet.createRow(orderSheet.getLastRowNum() + 1);
                writeOrder(orderRow, order);

                if (items != null) {
                    for (OrderItem item : items) {
                        item.setOrderItemId(nextItemId(itemSheet));
                        item.setOrderId(order.getOrderId());
                        Row itemRow = itemSheet.createRow(itemSheet.getLastRowNum() + 1);
                        writeItem(itemRow, item);
                    }
                }

                ExcelUtil.save(file, workbook);
                return order;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    /**
     * Returns every order, newest first, with line items attached.
     */
    public List<Order> findAll() {
        synchronized (FILE_LOCK) {
            List<Order> orders = new ArrayList<>();
            File file = DataPathUtil.getOrdersFile();
            if (!file.exists()) {
                return orders;
            }
            Workbook workbook = ExcelUtil.openOrCreate(file, ORDER_SHEET, ORDER_HEADERS);
            try {
                Sheet orderSheet = workbook.getSheet(ORDER_SHEET);
                if (orderSheet == null) {
                    return orders;
                }
                for (int i = 1; i <= orderSheet.getLastRowNum(); i++) {
                    Row row = orderSheet.getRow(i);
                    if (row == null) {
                        continue;
                    }
                    String orderId = formatter.formatCellValue(row.getCell(0));
                    if (orderId == null || orderId.trim().isEmpty()) {
                        continue;
                    }
                    Order order = rowToOrder(row);
                    order.setItems(readItems(workbook, orderId));
                    orders.add(order);
                }
                orders.sort((a, b) -> (b.getPlacedAt() == null ? "" : b.getPlacedAt())
                        .compareTo(a.getPlacedAt() == null ? "" : a.getPlacedAt()));
                return orders;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    /**
     * Returns the order with the given id (with items), or null when not found.
     */
    public Order findById(String orderId) {
        if (orderId == null) {
            return null;
        }
        for (Order order : findAll()) {
            if (orderId.equals(order.getOrderId())) {
                return order;
            }
        }
        return null;
    }

    /**
     * Returns all orders belonging to a buyer, newest first.
     */
    public List<Order> findByBuyerId(String buyerId) {
        List<Order> result = new ArrayList<>();
        if (buyerId == null) {
            return result;
        }
        for (Order order : findAll()) {
            if (buyerId.equals(order.getBuyerId())) {
                result.add(order);
            }
        }
        return result;
    }

    /**
     * Updates the header status and timestamp of an order. Returns true when updated.
     */
    public boolean updateStatus(String orderId, String status) {
        synchronized (FILE_LOCK) {
            File file = DataPathUtil.getOrdersFile();
            if (!file.exists()) {
                return false;
            }
            Workbook workbook = ExcelUtil.openOrCreate(file, ORDER_SHEET, ORDER_HEADERS);
            try {
                Sheet orderSheet = workbook.getSheet(ORDER_SHEET);
                if (orderSheet == null) {
                    return false;
                }
                int rowIndex = findOrderRow(orderSheet, orderId);
                if (rowIndex < 0) {
                    return false;
                }
                Row row = orderSheet.getRow(rowIndex);
                setCell(row, 12, status);
                setCell(row, 14, timestamp());
                ExcelUtil.save(file, workbook);
                return true;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    /**
     * Updates the payment status stored on the order header.
     */
    public boolean updatePaymentStatus(String orderId, String paymentStatus) {
        synchronized (FILE_LOCK) {
            File file = DataPathUtil.getOrdersFile();
            if (!file.exists()) {
                return false;
            }
            Workbook workbook = ExcelUtil.openOrCreate(file, ORDER_SHEET, ORDER_HEADERS);
            try {
                Sheet orderSheet = workbook.getSheet(ORDER_SHEET);
                if (orderSheet == null) {
                    return false;
                }
                int rowIndex = findOrderRow(orderSheet, orderId);
                if (rowIndex < 0) {
                    return false;
                }
                Row row = orderSheet.getRow(rowIndex);
                setCell(row, 11, paymentStatus);
                setCell(row, 14, timestamp());
                ExcelUtil.save(file, workbook);
                return true;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    /**
     * Updates a single line item's fulfillment status.
     */
    public boolean updateItemStatus(String orderItemId, String status) {
        synchronized (FILE_LOCK) {
            File file = DataPathUtil.getOrdersFile();
            if (!file.exists()) {
                return false;
            }
            Workbook workbook = ExcelUtil.openOrCreate(file, ORDER_SHEET, ORDER_HEADERS);
            try {
                Sheet itemSheet = workbook.getSheet(ITEM_SHEET);
                if (itemSheet == null) {
                    return false;
                }
                int rowIndex = findItemRow(itemSheet, orderItemId);
                if (rowIndex < 0) {
                    return false;
                }
                Row row = itemSheet.getRow(rowIndex);
                setCell(row, 7, status);
                ExcelUtil.save(file, workbook);
                return true;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    /**
     * Reads all line items belonging to an order from the open workbook.
     */
    private List<OrderItem> readItems(Workbook workbook, String orderId) {
        List<OrderItem> items = new ArrayList<>();
        Sheet itemSheet = workbook.getSheet(ITEM_SHEET);
        if (itemSheet == null) {
            return items;
        }
        for (int i = 1; i <= itemSheet.getLastRowNum(); i++) {
            Row row = itemSheet.getRow(i);
            if (row == null) {
                continue;
            }
            if (orderId.equals(formatter.formatCellValue(row.getCell(1)))) {
                items.add(rowToItem(row));
            }
        }
        return items;
    }

    private void writeOrder(Row row, Order order) {
        setCell(row, 0, order.getOrderId());
        setCell(row, 1, order.getBuyerId());
        setCell(row, 2, order.getFullName());
        setCell(row, 3, order.getPhone());
        setCell(row, 4, order.getAddress());
        setCell(row, 5, order.getPincode());
        setCell(row, 6, String.valueOf(order.getSubtotal()));
        setCell(row, 7, String.valueOf(order.getDeliveryFee()));
        setCell(row, 8, String.valueOf(order.getTotal()));
        setCell(row, 9, order.getPaymentMethod());
        setCell(row, 10, order.getTransactionId());
        setCell(row, 11, order.getPaymentStatus());
        setCell(row, 12, order.getOrderStatus());
        setCell(row, 13, order.getPlacedAt());
        setCell(row, 14, order.getUpdatedAt());
        setCell(row, 15, order.getExpectedDelivery());
    }

    private Order rowToOrder(Row row) {
        Order order = new Order();
        order.setOrderId(formatter.formatCellValue(row.getCell(0)));
        order.setBuyerId(formatter.formatCellValue(row.getCell(1)));
        order.setFullName(formatter.formatCellValue(row.getCell(2)));
        order.setPhone(formatter.formatCellValue(row.getCell(3)));
        order.setAddress(formatter.formatCellValue(row.getCell(4)));
        order.setPincode(formatter.formatCellValue(row.getCell(5)));
        order.setSubtotal(parseDouble(formatter.formatCellValue(row.getCell(6))));
        order.setDeliveryFee(parseDouble(formatter.formatCellValue(row.getCell(7))));
        order.setTotal(parseDouble(formatter.formatCellValue(row.getCell(8))));
        order.setPaymentMethod(formatter.formatCellValue(row.getCell(9)));
        order.setTransactionId(formatter.formatCellValue(row.getCell(10)));
        order.setPaymentStatus(formatter.formatCellValue(row.getCell(11)));
        order.setOrderStatus(formatter.formatCellValue(row.getCell(12)));
        order.setPlacedAt(formatter.formatCellValue(row.getCell(13)));
        order.setUpdatedAt(formatter.formatCellValue(row.getCell(14)));
        order.setExpectedDelivery(formatter.formatCellValue(row.getCell(15)));
        return order;
    }

    private void writeItem(Row row, OrderItem item) {
        setCell(row, 0, item.getOrderItemId());
        setCell(row, 1, item.getOrderId());
        setCell(row, 2, item.getProductId());
        setCell(row, 3, item.getSellerId());
        setCell(row, 4, item.getProductName());
        setCell(row, 5, String.valueOf(item.getPrice()));
        setCell(row, 6, String.valueOf(item.getQuantity()));
        setCell(row, 7, item.getItemStatus());
    }

    private OrderItem rowToItem(Row row) {
        OrderItem item = new OrderItem();
        item.setOrderItemId(formatter.formatCellValue(row.getCell(0)));
        item.setOrderId(formatter.formatCellValue(row.getCell(1)));
        item.setProductId(formatter.formatCellValue(row.getCell(2)));
        item.setSellerId(formatter.formatCellValue(row.getCell(3)));
        item.setProductName(formatter.formatCellValue(row.getCell(4)));
        item.setPrice(parseDouble(formatter.formatCellValue(row.getCell(5))));
        item.setQuantity(parseInt(formatter.formatCellValue(row.getCell(6))));
        item.setItemStatus(formatter.formatCellValue(row.getCell(7)));
        return item;
    }

    private int findOrderRow(Sheet sheet, String orderId) {
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            if (orderId.equals(formatter.formatCellValue(row.getCell(0)))) {
                return i;
            }
        }
        return -1;
    }

    private int findItemRow(Sheet sheet, String orderItemId) {
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            if (orderItemId.equals(formatter.formatCellValue(row.getCell(0)))) {
                return i;
            }
        }
        return -1;
    }

    private String nextOrderId(Sheet sheet) {
        int highest = 0;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            String value = formatter.formatCellValue(row.getCell(0)).trim();
            Matcher matcher = ORDER_ID_PATTERN.matcher(value);
            if (matcher.matches()) {
                int current = Integer.parseInt(matcher.group(1));
                if (current > highest) {
                    highest = current;
                }
            }
        }
        return String.format("O%04d", highest + 1);
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
        return String.format("OI%04d", highest + 1);
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