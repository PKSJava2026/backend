package com.beta.expedition.console;

import com.beta.expedition.exception.BusinessException;
import com.beta.expedition.service.ReportService;
import com.beta.expedition.util.TableData;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ReportMenu extends Menu {

    private static final Path EXPORT_DIRECTORY = Path.of("exports");
    private static final int MAX_CELL_WIDTH = 30;

    private final ReportService service;

    public ReportMenu(Input input, ReportService service) {
        super(input);
        this.service = service;
    }

    public void run() {
        while (true) {
            System.out.println();
            System.out.println("==== СТАТИСТИКА И ЭКСПОРТ ====");
            System.out.println("1. Статистика");
            System.out.println("2. Экспорт данных в Excel (.xlsx)");
            System.out.println("3. Экспорт данных в CSV");
            System.out.println("4. Вывести таблицы базы данных");
            System.out.println("0. Назад");

            switch (input.prompt("Выберите действие: ")) {
                case "1" -> safely(this::showStatistics);
                case "2" -> safely(() -> System.out.println("Данные сохранены: " + service.exportExcel(EXPORT_DIRECTORY)));
                case "3" -> safely(() -> System.out.println("Данные сохранены в папку: " + service.exportCsv(EXPORT_DIRECTORY)));
                case "4" -> safely(this::showTable);
                case "0" -> {
                    return;
                }
                default -> System.out.println("Ошибка: нет такого пункта меню");
            }
        }
    }

    private void showTable() {
        List<String> names = service.tableNames();
        for (int i = 0; i < names.size(); i++) {
            System.out.println((i + 1) + ". " + names.get(i));
        }
        int number = input.promptInt("Номер таблицы: ");
        if (number < 1 || number > names.size()) {
            throw new BusinessException("Нет такой таблицы");
        }
        print(service.readTable(names.get(number - 1)));
    }

    private void print(TableData table) {
        List<List<String>> cells = new ArrayList<>();
        cells.add(table.columns());
        for (List<Object> row : table.rows()) {
            cells.add(row.stream().map(this::cell).toList());
        }
        int[] widths = new int[table.columns().size()];
        for (List<String> row : cells) {
            for (int i = 0; i < widths.length; i++) {
                widths[i] = Math.max(widths[i], row.get(i).length());
            }
        }
        System.out.println();
        System.out.println("Таблица " + table.name() + " (записей: " + table.rows().size() + ")");
        for (int r = 0; r < cells.size(); r++) {
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < widths.length; i++) {
                line.append(String.format("%-" + widths[i] + "s", cells.get(r).get(i)));
                line.append(i < widths.length - 1 ? " | " : "");
            }
            System.out.println(line);
            if (r == 0) {
                System.out.println("-".repeat(line.length()));
            }
        }
    }

    private String cell(Object value) {
        String text = value == null ? "" : value.toString().replace('\n', ' ');
        return text.length() > MAX_CELL_WIDTH ? text.substring(0, MAX_CELL_WIDTH - 1) + "…" : text;
    }

    private void showStatistics() {
        System.out.println();
        service.statistics().forEach((name, value) -> System.out.println(name + ": " + value));
    }
}
