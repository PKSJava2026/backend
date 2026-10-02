package com.beta.expedition.console;

import com.beta.expedition.model.User;
import com.beta.expedition.service.CooperationService;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CooperationMenu extends Menu {

    private final CooperationService service;

    public CooperationMenu(Input input, CooperationService service) {
        super(input);
        this.service = service;
    }

    public void runForCarrier(User carrier) {
        while (true) {
            System.out.println();
            System.out.println("==== ЗАЯВКИ О СОТРУДНИЧЕСТВЕ ====");
            System.out.println("1. Создать заявку");
            System.out.println("2. Мои заявки");
            System.out.println("3. Отозвать заявку");
            System.out.println("0. Назад");

            switch (input.prompt("Выберите действие: ")) {
                case "1" -> safely(() -> createRequest(carrier));
                case "2" -> safely(() -> printList(service.listByCarrier(carrier.getId())));
                case "3" -> safely(() -> cancelRequest(carrier));
                case "0" -> {
                    return;
                }
                default -> System.out.println("Ошибка: нет такого пункта меню");
            }
        }
    }

    public void runForForwarder(User forwarder) {
        while (true) {
            System.out.println();
            System.out.println("==== ЗАЯВКИ ПЕРЕВОЗЧИКОВ О СОТРУДНИЧЕСТВЕ ====");
            System.out.println("1. Необработанные заявки");
            System.out.println("2. Заключить договор по заявке");
            System.out.println("0. Назад");

            switch (input.prompt("Выберите действие: ")) {
                case "1" -> safely(() -> printList(service.listNew()));
                case "2" -> safely(() -> createContract(forwarder));
                case "0" -> {
                    return;
                }
                default -> System.out.println("Ошибка: нет такого пункта меню");
            }
        }
    }

    private void createRequest(User carrier) {
        String description = input.prompt("О себе (опыт, регионы, условия): ");
        String vehicle = input.prompt("Транспорт (тип, грузоподъёмность): ");
        System.out.println("Заявка создана: " + service.create(carrier.getId(), description, vehicle));
    }

    private void cancelRequest(User carrier) {
        service.cancel(carrier.getId(), input.promptId("ID заявки о сотрудничестве: "));
        System.out.println("Заявка отозвана");
    }

    private void createContract(User forwarder) {
        long requestId = input.promptId("ID заявки о сотрудничестве: ");
        long orderId = input.promptId("ID заявки на перевозку: ");
        BigDecimal price = input.promptDecimalOrNull("Цена: ");
        String terms = input.prompt("Условия (Enter — пропустить): ");
        LocalDate start = input.promptDateOrNull("Дата начала гггг-мм-дд (Enter — пропустить): ");
        LocalDate end = input.promptDateOrNull("Дата окончания гггг-мм-дд (Enter — пропустить): ");
        System.out.println("Договор предложен перевозчику, ждёт подтверждения: "
                + service.createContract(forwarder.getId(), requestId, orderId, price, terms, start, end));
    }
}
