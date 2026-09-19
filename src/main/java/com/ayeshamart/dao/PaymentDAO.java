package com.ayeshamart.dao;

import com.ayeshamart.model.Payment;
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
 * Persists demo payment records in an Excel workbook (data/payments.xlsx).
 * Only masked payment details are ever stored - never real card numbers, CVV or PINs.
 */
public class PaymentDAO {

    private static final Object FILE_LOCK = new Object();

    private static final String SHEET_NAME = "Payments";
    private static final String[] HEADERS = {
            "paymentId", "orderId", "buyerId", "method", "transactionId",
            "amount", "maskedDetails", "status", "createdAt"
    };
    private static final Pattern PAYMENT_ID_PATTERN = Pattern.compile("^PAY(\\d+)$");
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final DataFormatter formatter = new DataFormatter();

    /**
     * Appends a payment record, assigning its id and timestamps.
     */
    public Payment save(Payment payment) {
        synchronized (FILE_LOCK) {
            File file = DataPathUtil.getPaymentsFile();
            Workbook workbook = ExcelUtil.openOrCreate(file, SHEET_NAME, HEADERS);
            try {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    sheet = workbook.createSheet(SHEET_NAME);
                }
                String now = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
                payment.setPaymentId(nextPaymentId(sheet));
                payment.setCreatedAt(now);

                Row row = sheet.createRow(sheet.getLastRowNum() + 1);
                setCell(row, 0, payment.getPaymentId());
                setCell(row, 1, payment.getOrderId());
                setCell(row, 2, payment.getBuyerId());
                setCell(row, 3, payment.getMethod());
                setCell(row, 4, payment.getTransactionId());
                setCell(row, 5, String.valueOf(payment.getAmount()));
                setCell(row, 6, payment.getMaskedDetails());
                setCell(row, 7, payment.getStatus());
                setCell(row, 8, payment.getCreatedAt());

                ExcelUtil.save(file, workbook);
                return payment;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    /**
     * Returns the payment record for an order, or null when none exists.
     */
    public Payment findByOrderId(String orderId) {
        if (orderId == null) {
            return null;
        }
        for (Payment payment : findAll()) {
            if (orderId.equals(payment.getOrderId())) {
                return payment;
            }
        }
        return null;
    }

    /**
     * Returns every payment record, newest first.
     */
    public List<Payment> findAll() {
        synchronized (FILE_LOCK) {
            List<Payment> payments = new ArrayList<>();
            File file = DataPathUtil.getPaymentsFile();
            if (!file.exists()) {
                return payments;
            }
            Workbook workbook = ExcelUtil.openOrCreate(file, SHEET_NAME, HEADERS);
            try {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    return payments;
                }
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        continue;
                    }
                    String paymentId = formatter.formatCellValue(row.getCell(0));
                    if (paymentId == null || paymentId.trim().isEmpty()) {
                        continue;
                    }
                    Payment payment = new Payment();
                    payment.setPaymentId(paymentId);
                    payment.setOrderId(formatter.formatCellValue(row.getCell(1)));
                    payment.setBuyerId(formatter.formatCellValue(row.getCell(2)));
                    payment.setMethod(formatter.formatCellValue(row.getCell(3)));
                    payment.setTransactionId(formatter.formatCellValue(row.getCell(4)));
                    payment.setAmount(parseDouble(formatter.formatCellValue(row.getCell(5))));
                    payment.setMaskedDetails(formatter.formatCellValue(row.getCell(6)));
                    payment.setStatus(formatter.formatCellValue(row.getCell(7)));
                    payment.setCreatedAt(formatter.formatCellValue(row.getCell(8)));
                    payments.add(payment);
                }
                payments.sort((a, b) -> (b.getCreatedAt() == null ? "" : b.getCreatedAt())
                        .compareTo(a.getCreatedAt() == null ? "" : a.getCreatedAt()));
                return payments;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    /**
     * Updates the recorded payment status for an order (e.g. marking a COD payment as
     * received when it is collected on delivery).
     */
    public boolean updateStatus(String orderId, String status) {
        synchronized (FILE_LOCK) {
            File file = DataPathUtil.getPaymentsFile();
            if (!file.exists()) {
                return false;
            }
            Workbook workbook = ExcelUtil.openOrCreate(file, SHEET_NAME, HEADERS);
            try {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    return false;
                }
                int rowIndex = findRowIndex(sheet, orderId);
                if (rowIndex < 0) {
                    return false;
                }
                Row row = sheet.getRow(rowIndex);
                setCell(row, 7, status);
                setCell(row, 8, new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date()));
                ExcelUtil.save(file, workbook);
                return true;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    private int findRowIndex(Sheet sheet, String orderId) {
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            if (orderId.equals(formatter.formatCellValue(row.getCell(1)))) {
                return i;
            }
        }
        return -1;
    }

    private String nextPaymentId(Sheet sheet) {
        int highest = 0;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            String value = formatter.formatCellValue(row.getCell(0)).trim();
            Matcher matcher = PAYMENT_ID_PATTERN.matcher(value);
            if (matcher.matches()) {
                int current = Integer.parseInt(matcher.group(1));
                if (current > highest) {
                    highest = current;
                }
            }
        }
        return String.format("PAY%04d", highest + 1);
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return 0d;
        }
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