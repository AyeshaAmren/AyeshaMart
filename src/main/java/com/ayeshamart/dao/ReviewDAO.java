package com.ayeshamart.dao;

import com.ayeshamart.model.Review;
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
 * Persists product reviews/ratings in an Excel workbook (data/reviews.xlsx).
 * Each (buyerId, productId) pair may appear only once.
 */
public class ReviewDAO {

    private static final Object FILE_LOCK = new Object();

    private static final String SHEET_NAME = "Reviews";
    private static final String[] HEADERS = {
            "reviewId", "productId", "buyerId", "orderId", "rating", "title", "comment", "createdAt"
    };
    private static final Pattern REVIEW_ID_PATTERN = Pattern.compile("^R(\\d+)$");
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final DataFormatter formatter = new DataFormatter();

    /**
     * Appends a review, assigning its id and timestamp.
     */
    public Review save(Review review) {
        synchronized (FILE_LOCK) {
            File file = DataPathUtil.getReviewsFile();
            Workbook workbook = ExcelUtil.openOrCreate(file, SHEET_NAME, HEADERS);
            try {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    sheet = workbook.createSheet(SHEET_NAME);
                }
                String now = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
                review.setReviewId(nextReviewId(sheet));
                review.setCreatedAt(now);

                Row row = sheet.createRow(sheet.getLastRowNum() + 1);
                setCell(row, 0, review.getReviewId());
                setCell(row, 1, review.getProductId());
                setCell(row, 2, review.getBuyerId());
                setCell(row, 3, review.getOrderId());
                setCell(row, 4, String.valueOf(review.getRating()));
                setCell(row, 5, review.getTitle());
                setCell(row, 6, review.getComment());
                setCell(row, 7, review.getCreatedAt());

                ExcelUtil.save(file, workbook);
                return review;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    /**
     * Returns the review a buyer left for a product, or null when they have not reviewed it.
     */
    public Review findByBuyerAndProduct(String buyerId, String productId) {
        if (buyerId == null || productId == null) {
            return null;
        }
        for (Review review : findAll()) {
            if (buyerId.equals(review.getBuyerId()) && productId.equals(review.getProductId())) {
                return review;
            }
        }
        return null;
    }

    /**
     * Returns all reviews for a product, newest first.
     */
    public List<Review> findByProductId(String productId) {
        List<Review> result = new ArrayList<>();
        if (productId == null) {
            return result;
        }
        for (Review review : findAll()) {
            if (productId.equals(review.getProductId())) {
                result.add(review);
            }
        }
        return result;
    }

    /**
     * Returns every review, newest first.
     */
    public List<Review> findAll() {
        synchronized (FILE_LOCK) {
            List<Review> reviews = new ArrayList<>();
            File file = DataPathUtil.getReviewsFile();
            if (!file.exists()) {
                return reviews;
            }
            Workbook workbook = ExcelUtil.openOrCreate(file, SHEET_NAME, HEADERS);
            try {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    return reviews;
                }
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        continue;
                    }
                    String reviewId = formatter.formatCellValue(row.getCell(0));
                    if (reviewId == null || reviewId.trim().isEmpty()) {
                        continue;
                    }
                    Review review = new Review();
                    review.setReviewId(reviewId);
                    review.setProductId(formatter.formatCellValue(row.getCell(1)));
                    review.setBuyerId(formatter.formatCellValue(row.getCell(2)));
                    review.setOrderId(formatter.formatCellValue(row.getCell(3)));
                    review.setRating(parseInt(formatter.formatCellValue(row.getCell(4))));
                    review.setTitle(formatter.formatCellValue(row.getCell(5)));
                    review.setComment(formatter.formatCellValue(row.getCell(6)));
                    review.setCreatedAt(formatter.formatCellValue(row.getCell(7)));
                    reviews.add(review);
                }
                reviews.sort((a, b) -> (b.getCreatedAt() == null ? "" : b.getCreatedAt())
                        .compareTo(a.getCreatedAt() == null ? "" : a.getCreatedAt()));
                return reviews;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    private String nextReviewId(Sheet sheet) {
        int highest = 0;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            String value = formatter.formatCellValue(row.getCell(0)).trim();
            Matcher matcher = REVIEW_ID_PATTERN.matcher(value);
            if (matcher.matches()) {
                int current = Integer.parseInt(matcher.group(1));
                if (current > highest) {
                    highest = current;
                }
            }
        }
        return String.format("R%04d", highest + 1);
    }

    private int parseInt(String value) {
        try {
            return (int) Double.parseDouble(value.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return 0;
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