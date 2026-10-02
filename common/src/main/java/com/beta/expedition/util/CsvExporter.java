package com.beta.expedition.util;

import com.beta.expedition.exception.ExportException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CsvExporter {

    private static final String BOM = "﻿";

    public void export(Path directory, Map<String, String> statistics, List<TableData> tables) {
        try {
            Files.createDirectories(directory);
            List<List<Object>> statisticRows = new ArrayList<>();
            statistics.forEach((name, value) -> statisticRows.add(List.of(name, value)));
            write(directory.resolve("statistics.csv"), List.of("Показатель", "Значение"), statisticRows);
            for (TableData table : tables) {
                write(directory.resolve(table.name() + ".csv"), table.columns(), table.rows());
            }
        } catch (IOException e) {
            throw new ExportException("Не удалось записать файлы в " + directory + ": " + e.getMessage(), e);
        }
    }

    private void write(Path file, List<String> columns, List<List<Object>> rows) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(line(new ArrayList<>(columns)));
        rows.forEach(row -> lines.add(line(row)));
        Files.writeString(file, BOM + String.join("\r\n", lines) + "\r\n", StandardCharsets.UTF_8);
    }

    private String line(List<?> values) {
        return values.stream().map(this::escape).collect(Collectors.joining(";"));
    }

    private String escape(Object value) {
        String text = value == null ? "" : value.toString();
        if (text.contains(";") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
