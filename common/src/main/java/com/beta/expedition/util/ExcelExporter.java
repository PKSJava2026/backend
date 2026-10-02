package com.beta.expedition.util;

import com.beta.expedition.exception.ExportException;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ExcelExporter {

    private static final int COLUMN_WIDTH = 22 * 256;

    public void export(Path file, Map<String, String> statistics, List<TableData> tables) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle header = workbook.createCellStyle();
            Font bold = workbook.createFont();
            bold.setBold(true);
            header.setFont(bold);

            Sheet stats = workbook.createSheet("Статистика");
            writeRow(stats.createRow(0), List.of("Показатель", "Значение"), header);
            int rowIndex = 1;
            for (Map.Entry<String, String> entry : statistics.entrySet()) {
                writeRow(stats.createRow(rowIndex++), List.of(entry.getKey(), entry.getValue()), null);
            }
            stats.setColumnWidth(0, 55 * 256);
            stats.setColumnWidth(1, 30 * 256);

            for (TableData table : tables) {
                Sheet sheet = workbook.createSheet(table.name());
                writeRow(sheet.createRow(0), table.columns(), header);
                int index = 1;
                for (List<Object> values : table.rows()) {
                    writeRow(sheet.createRow(index++), values, null);
                }
                for (int i = 0; i < table.columns().size(); i++) {
                    sheet.setColumnWidth(i, COLUMN_WIDTH);
                }
                sheet.createFreezePane(0, 1);
            }

            Files.createDirectories(file.toAbsolutePath().getParent());
            try (OutputStream out = Files.newOutputStream(file)) {
                workbook.write(out);
            }
        } catch (IOException e) {
            throw new ExportException("Не удалось записать файл " + file + ": " + e.getMessage(), e);
        }
    }

    private void writeRow(Row row, List<?> values, CellStyle style) {
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            var cell = row.createCell(i);
            if (value instanceof Number number) {
                cell.setCellValue(number instanceof BigDecimal decimal ? decimal.doubleValue() : number.doubleValue());
            } else if (value instanceof Boolean flag) {
                cell.setCellValue(flag);
            } else if (value != null) {
                cell.setCellValue(value.toString());
            }
            if (style != null) {
                cell.setCellStyle(style);
            }
        }
    }
}
