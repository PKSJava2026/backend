package com.beta.expedition.console;

import com.beta.expedition.exception.BusinessException;
import com.beta.expedition.model.ContractSort;
import com.beta.expedition.model.ContractStatus;
import com.beta.expedition.service.ContractService;

import java.time.LocalDate;

public class ContractQueryMenu extends Menu {

    private final ContractService<?> service;

    public ContractQueryMenu(Input input, ContractService<?> service) {
        super(input);
        this.service = service;
    }

    public void run() {
        while (true) {
            System.out.println();
            System.out.println("==== ПОИСК ДОГОВОРОВ " + service.getKind().getTitle().toUpperCase() + " ====");
            System.out.println("1. Найти по ID договора");
            System.out.println("2. Найти по дате создания");
            System.out.println("3. Найти по ID заявки");
            System.out.println("4. Фильтр по статусу");
            System.out.println("5. Фильтр по периоду создания");
            System.out.println("6. Сортировка (дата, статус, сумма)");
            System.out.println("0. Назад");

            switch (input.prompt("Выберите действие: ")) {
                case "1" -> safely(() -> System.out.println(service.get(input.promptId("ID договора: "))));
                case "2" -> safely(() -> printList(service.searchByCreatedDate(requireDate("Дата создания гггг-мм-дд: "))));
                case "3" -> safely(() -> printList(service.searchByOrder(input.promptId("ID заявки: "))));
                case "4" -> safely(() -> printList(service.filter(
                        input.promptEnum("Статус", ContractStatus.class), null, null)));
                case "5" -> safely(() -> printList(service.filter(null,
                        input.promptDateOrNull("С даты гггг-мм-дд (Enter — без ограничения): "),
                        input.promptDateOrNull("По дату гггг-мм-дд (Enter — без ограничения): "))));
                case "6" -> safely(this::sort);
                case "0" -> {
                    return;
                }
                default -> System.out.println("Ошибка: нет такого пункта меню");
            }
        }
    }

    private void sort() {
        ContractSort field = input.promptEnum("Сортировать", ContractSort.class);
        printList(service.listSorted(field, input.promptDescending()));
    }

    private LocalDate requireDate(String message) {
        LocalDate date = input.promptDateOrNull(message);
        if (date == null) {
            throw new BusinessException("Дата обязательна");
        }
        return date;
    }
}
