package com.ayeshamart.util;

import com.ayeshamart.model.User;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Excel (.xlsx) storage layer.
 * <p>
 * File: {@code <catalina.base>/data/users.xlsx} (falls back to user.dir when
 * not running under Tomcat, e.g. unit tests).
 * <p>
 * Columns: user_id, name, email, password, phone, address, created_at
 */
public final class ExcelUtil {

    private static final String SHEET_NAME = "users";
    private static final String FILE_NAME = "users.xlsx";
    private static final String[] HEADERS =
            {"user_id", "name", "email", "password", "phone", "address", "created_at"};

    private ExcelUtil() {
    }

    /** Absolute path of the users.xlsx file. */
    public static String getUsersFilePath() {
        return new File(getDataDir(), FILE_NAME).getAbsolutePath();
    }

    private static File getDataDir() {
        String base = System.getProperty("catalina.base");
        if (base == null || base.trim().isEmpty()) {
            base = System.getProperty("user.dir", ".");
        }
        File dir = new File(base, "data");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private static File getUsersFile() {
        File file = new File(getDataDir(), FILE_NAME);
        if (!file.exists()) {
            createWorkbook(file);
        }
        return file;
    }

    /** Creates a fresh workbook with the header row, if the file does not exist. */
    private static void createWorkbook(File file) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(SHEET_NAME);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }
            writeWorkbook(file, workbook);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create " + FILE_NAME, e);
        }
    }

    /** Reads every user from the sheet. Returns an empty list when none exist. */
    public static synchronized List<User> readUsers() {
        List<User> users = new ArrayList<>();
        File file = getUsersFile();
        try (Workbook workbook = new XSSFWorkbook(Files.newInputStream(file.toPath()))) {
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null || sheet.getLastRowNum() < 1) {
                return users;
            }
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                User user = new User();
                user.setId(parseLong(cellAsString(row.getCell(0))));
                user.setName(cellAsString(row.getCell(1)));
                user.setEmail(cellAsString(row.getCell(2)));
                user.setPasswordHash(cellAsString(row.getCell(3)));
                user.setPhone(cellAsString(row.getCell(4)));
                user.setAddress(cellAsString(row.getCell(5)));
                user.setCreatedAt(cellAsString(row.getCell(6)));

                if (!user.getEmail().isEmpty()) {
                    users.add(user);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + FILE_NAME, e);
        }
        return users;
    }

    /** Appends a single user as a new row at the end of the sheet. */
    public static synchronized void appendUser(User user) {
        File file = getUsersFile();
        try (Workbook workbook = new XSSFWorkbook(Files.newInputStream(file.toPath()))) {
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            int rowNumber = sheet.getLastRowNum() + 1;
            Row row = sheet.createRow(rowNumber);

            Object[] values = {
                    user.getId(), user.getName(), user.getEmail(), user.getPasswordHash(),
                    user.getPhone(), user.getAddress(), user.getCreatedAt()
            };
            for (int i = 0; i < values.length; i++) {
                Object value = values[i];
                if (value instanceof Long) {
                    row.createCell(i).setCellValue((Long) value);
                } else {
                    row.createCell(i).setCellValue(value == null ? "" : value.toString());
                }
            }
            writeWorkbook(file, workbook);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write user to " + FILE_NAME, e);
        }
    }

    /** Next available user id = max existing id + 1. */
    public static synchronized long nextUserId() {
        long max = 0L;
        for (User user : readUsers()) {
            max = Math.max(max, user.getId());
        }
        return max + 1;
    }

    /** Current timestamp formatted as yyyy-MM-dd HH:mm:ss. */
    public static String nowTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    private static void writeWorkbook(File file, Workbook workbook) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (OutputStream out = Files.newOutputStream(file.toPath())) {
            workbook.write(out);
        }
    }

    private static String cellAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                double value = cell.getNumericCellValue();
                if (value == Math.floor(value) && !Double.isInfinite(value)) {
                    return String.valueOf((long) value);
                }
                return String.valueOf(value);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private static long parseLong(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
