package com.ayeshamart.dao;

import com.ayeshamart.model.User;
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
 * UserDAO persists User objects in an Excel workbook (data/users.xlsx)
 * using Apache POI. All file access is synchronized to keep reads/writes consistent.
 */
public class UserDAO {

    private static final Object FILE_LOCK = new Object();

    private static final String SHEET_NAME = "Users";
    private static final String[] HEADERS = {"userId", "name", "email", "password", "phone", "address", "role", "createdAt"};
    private static final Pattern USER_ID_PATTERN = Pattern.compile("^U(\\d+)$");

    private final DataFormatter formatter = new DataFormatter();

    /**
     * Creates a new user row, assigning userId and createdAt automatically.
     */
    public User save(User user) {
        synchronized (FILE_LOCK) {
            File file = DataPathUtil.getUsersFile();
            Workbook workbook = ExcelUtil.openOrCreate(file, SHEET_NAME, HEADERS);
            try {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    sheet = workbook.createSheet(SHEET_NAME);
                }

                user.setUserId(nextUserId(sheet));
                user.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

                int rowIndex = sheet.getLastRowNum() + 1;
                Row row = sheet.createRow(rowIndex);
                writeCell(row, 0, user.getUserId());
                writeCell(row, 1, user.getName());
                writeCell(row, 2, user.getEmail());
                writeCell(row, 3, user.getPassword());
                writeCell(row, 4, user.getPhone());
                writeCell(row, 5, user.getAddress());
                writeCell(row, 6, user.getRole());
                writeCell(row, 7, user.getCreatedAt());

                ExcelUtil.save(file, workbook);
                return user;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    /**
     * Returns the user with the given email (case-insensitive), or null if not found.
     */
    public User findByEmail(String email) {
        if (email == null) {
            return null;
        }
        synchronized (FILE_LOCK) {
            for (User user : findAll()) {
                if (email.equalsIgnoreCase(user.getEmail())) {
                    return user;
                }
            }
            return null;
        }
    }

    /**
     * Returns true when a user already exists with the given email (case-insensitive).
     */
    public boolean emailExists(String email) {
        return findByEmail(email) != null;
    }

    /**
     * Reads every user from the workbook. Returns an empty list if no file exists.
     */
    public List<User> findAll() {
        synchronized (FILE_LOCK) {
            List<User> users = new ArrayList<>();
            File file = DataPathUtil.getUsersFile();
            if (!file.exists()) {
                return users;
            }
            Workbook workbook = ExcelUtil.openOrCreate(file, SHEET_NAME, HEADERS);
            try {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    return users;
                }
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        continue;
                    }
                    String email = formatter.formatCellValue(row.getCell(2));
                    if (email == null || email.trim().isEmpty()) {
                        continue;
                    }
                    users.add(rowToUser(row));
                }
                return users;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    private User rowToUser(Row row) {
        User user = new User();
        user.setUserId(formatter.formatCellValue(row.getCell(0)));
        user.setName(formatter.formatCellValue(row.getCell(1)));
        user.setEmail(formatter.formatCellValue(row.getCell(2)));
        user.setPassword(formatter.formatCellValue(row.getCell(3)));
        user.setPhone(formatter.formatCellValue(row.getCell(4)));
        user.setAddress(formatter.formatCellValue(row.getCell(5)));
        user.setRole(formatter.formatCellValue(row.getCell(6)));
        user.setCreatedAt(formatter.formatCellValue(row.getCell(7)));
        return user;
    }

    private String nextUserId(Sheet sheet) {
        int highest = 0;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            String value = formatter.formatCellValue(row.getCell(0));
            Matcher matcher = USER_ID_PATTERN.matcher(value.trim());
            if (matcher.matches()) {
                int current = Integer.parseInt(matcher.group(1));
                if (current > highest) {
                    highest = current;
                }
            }
        }
        return String.format("U%04d", highest + 1);
    }

    private void writeCell(Row row, int index, String value) {
        Cell cell = row.createCell(index);
        cell.setCellValue(value == null ? "" : value);
    }

    private void closeQuietly(Workbook workbook) {
        try {
            workbook.close();
        } catch (Exception ignored) {
        }
    }
}