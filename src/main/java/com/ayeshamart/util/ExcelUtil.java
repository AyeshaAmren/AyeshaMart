package com.ayeshamart.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Small helper for reading and writing Excel (XLSX) workbooks shared by the DAO layer.
 */
public final class ExcelUtil {

    private ExcelUtil() {
    }

    /**
     * Opens an existing workbook, or creates a new one (with a styled header row)
     * if the file does not exist yet.
     */
    public static Workbook openOrCreate(File file, String sheetName, String[] headers) {
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                return new XSSFWorkbook(in);
            } catch (IOException e) {
                throw new IllegalStateException("Could not read Excel file: " + file.getAbsolutePath(), e);
            }
        }
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);
        writeHeaderRow(sheet, headers);
        save(file, workbook);
        return workbook;
    }

    /**
     * Writes the workbook to the given file, keeping the parent folders created.
     */
    public static void save(File file, Workbook workbook) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(file)) {
            workbook.write(out);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write Excel file: " + file.getAbsolutePath(), e);
        }
    }

    public static void writeHeaderRow(Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 18 * 256);
        }
        sheet.setColumnWidth(headers.length > 2 ? 3 : 0, 22 * 256);
    }
}