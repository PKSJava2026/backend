package com.beta.expedition.console;

import com.beta.expedition.model.OrderSort;
import com.beta.expedition.model.OrderStatus;
import com.beta.expedition.service.OrderService;

public class OrderQueryMenu extends Menu {

    private final OrderService orderService;

    public OrderQueryMenu(Input input, OrderService orderService) {
        super(input);
        this.orderService = orderService;
    }

    public void run() {
        while (true) {
            System.out.println();
            System.out.println("==== ПОИСК ЗАЯВОК ====");
            System.out.println("1. Найти по ID заявки");
            System.out.println("2. Найти по тексту (груз, откуда, куда)");
            System.out.println("3. Найти по ID заказчика");
            System.out.println("4. Фильтр по статусу");
            System.out.println("5. Фильтр по периоду создания");
            System.out.println("6. Сортировка (дата, статус, вес)");
            System.out.println("0. Назад");

            switch (input.prompt("Выберите действие: ")) {
                case "1" -> safely(() -> System.out.println(orderService.getById(input.promptId("ID заявки: "))));
                case "2" -> safely(() -> printList(orderService.search(input.prompt("Текст: "))));
                case "3" -> safely(() -> printList(orderService.listByCustomer(input.promptId("ID заказчика: "))));
                case "4" -> safely(() -> printList(orderService.filter(
                        input.promptEnum("Статус", OrderStatus.class), null, null)));
                case "5" -> safely(() -> printList(orderService.filter(null,
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
        OrderSort field = input.promptEnum("Сортировать", OrderSort.class);
        printList(orderService.listSorted(field, input.promptDescending()));
    }
}
