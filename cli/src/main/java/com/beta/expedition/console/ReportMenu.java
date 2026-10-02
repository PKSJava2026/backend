package com.beta.expedition.console;

import com.beta.expedition.service.ReportService;

import java.nio.file.Path;

public class ReportMenu extends Menu {

    private static final Path EXPORT_DIRECTORY = Path.of("exports");

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
            System.out.println("0. Назад");

            switch (input.prompt("Выберите действие: ")) {
                case "1" -> safely(this::showStatistics);
                case "2" -> safely(() -> System.out.println("Данные сохранены: " + service.exportExcel(EXPORT_DIRECTORY)));
                case "3" -> safely(() -> System.out.println("Данные сохранены в папку: " + service.exportCsv(EXPORT_DIRECTORY)));
                case "0" -> {
                    return;
                }
                default -> System.out.println("Ошибка: нет такого пункта меню");
            }
        }
    }

    private void showStatistics() {
        System.out.println();
        service.statistics().forEach((name, value) -> System.out.println(name + ": " + value));
    }
}
