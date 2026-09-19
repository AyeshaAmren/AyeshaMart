package com.ayeshamart.dao;

import com.ayeshamart.model.Category;
import com.ayeshamart.util.DataPathUtil;
import com.ayeshamart.util.ExcelUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * CategoryDAO persists the product categories in an Excel workbook (data/categories.xlsx).
 */
public class CategoryDAO {

    private static final Object FILE_LOCK = new Object();

    private static final String SHEET_NAME = "Categories";
    private static final String[] HEADERS = {"categoryId", "name", "description"};

    private final DataFormatter formatter = new DataFormatter();

    /**
     * Inserts a category, assigning the next categoryId automatically.
     */
    public Category save(Category category) {
        synchronized (FILE_LOCK) {
            File file = DataPathUtil.getCategoriesFile();
            Workbook workbook = ExcelUtil.openOrCreate(file, SHEET_NAME, HEADERS);
            try {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    sheet = workbook.createSheet(SHEET_NAME);
                }
                category.setCategoryId(nextCategoryId(sheet));

                int rowIndex = sheet.getLastRowNum() + 1;
                Row row = sheet.createRow(rowIndex);
                row.createCell(0).setCellValue(category.getCategoryId());
                row.createCell(1).setCellValue(category.getName() == null ? "" : category.getName());
                row.createCell(2).setCellValue(category.getDescription() == null ? "" : category.getDescription());

                ExcelUtil.save(file, workbook);
                return category;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    /**
     * Returns every category, or an empty list when none are stored yet.
     */
    public List<Category> findAll() {
        synchronized (FILE_LOCK) {
            List<Category> categories = new ArrayList<>();
            File file = DataPathUtil.getCategoriesFile();
            if (!file.exists()) {
                return categories;
            }
            Workbook workbook = ExcelUtil.openOrCreate(file, SHEET_NAME, HEADERS);
            try {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    return categories;
                }
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        continue;
                    }
                    String name = formatter.formatCellValue(row.getCell(1));
                    if (name == null || name.trim().isEmpty()) {
                        continue;
                    }
                    Category category = new Category();
                    category.setCategoryId(formatter.formatCellValue(row.getCell(0)));
                    category.setName(name.trim());
                    category.setDescription(formatter.formatCellValue(row.getCell(2)));
                    categories.add(category);
                }
                return categories;
            } finally {
                closeQuietly(workbook);
            }
        }
    }

    /**
     * Finds a category by name (case-insensitive), or null when not found.
     */
    public Category findByName(String name) {
        if (name == null) {
            return null;
        }
        for (Category category : findAll()) {
            if (name.equalsIgnoreCase(category.getName())) {
                return category;
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return findAll().isEmpty();
    }

    private String nextCategoryId(Sheet sheet) {
        return String.format("C%03d", sheet.getLastRowNum() + 1);
    }

    private void closeQuietly(Workbook workbook) {
        try {
            workbook.close();
        } catch (Exception ignored) {
        }
    }
}
