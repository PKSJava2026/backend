package com.beta.expedition.console;

import com.beta.expedition.model.Party;
import com.beta.expedition.model.User;
import com.beta.expedition.service.ContractService;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ContractMenu extends Menu {

    private final ContractService<?> service;
    private final Party party;
    private final ContractQueryMenu queryMenu;

    public ContractMenu(Input input, ContractService<?> service, Party party) {
        super(input);
        this.service = service;
        this.party = party;
        this.queryMenu = new ContractQueryMenu(input, service);
    }

    public void run(User user) {
        boolean forwarder = party == Party.FORWARDER;
        while (true) {
            System.out.println();
            System.out.println("==== ДОГОВОРЫ " + service.getKind().getTitle().toUpperCase() + " ====");
            System.out.println("1. Список договоров");
            System.out.println("2. Договор по ID");
            System.out.println("3. Предложить изменение договора");
            System.out.println("4. Предложить расторжение договора");
            System.out.println("5. Открытые запросы на изменение/расторжение");
            System.out.println("6. Принять запрос");
            System.out.println("7. Отклонить запрос");
            System.out.println("8. Отозвать мой запрос");
            if (forwarder) {
                System.out.println("9. Заключить договор (предложить)");
                System.out.println("10. Поиск, фильтрация, сортировка");
            } else {
                System.out.println("9. Подтвердить предложенный договор");
                System.out.println("10. Отклонить предложенный договор");
            }
            System.out.println("0. Назад");

            switch (input.prompt("Выберите действие: ")) {
                case "1" -> safely(() -> printList(forwarder ? service.listAll()
                        : service.listForCounterparty(user.getId())));
                case "2" -> safely(() -> System.out.println(
                        service.get(user.getId(), party, input.promptId("ID договора: "))));
                case "3" -> safely(() -> requestAmendment(user));
                case "4" -> safely(() -> requestTermination(user));
                case "5" -> safely(() -> printList(service.listPendingRequests(user.getId(), party)));
                case "6" -> safely(() -> respond(user, true));
                case "7" -> safely(() -> respond(user, false));
                case "8" -> safely(() -> cancelRequest(user));
                case "9" -> safely(() -> {
                    if (forwarder) {
                        create(user);
                    } else {
                        acceptContract(user);
                    }
                });
                case "10" -> safely(() -> {
                    if (forwarder) {
                        queryMenu.run();
                    } else {
                        rejectContract(user);
                    }
                });
                case "0" -> {
                    return;
                }
                default -> System.out.println("Ошибка: нет такого пункта меню");
            }
        }
    }

    private void create(User forwarder) {
        long orderId = input.promptId("ID заявки: ");
        long counterpartyId = input.promptId("ID " + service.getKind().getCounterparty().toLowerCase() + "а: ");
        BigDecimal price = input.promptDecimalOrNull("Цена: ");
        String terms = input.prompt("Условия (Enter — пропустить): ");
        LocalDate start = input.promptDateOrNull("Дата начала гггг-мм-дд (Enter — пропустить): ");
        LocalDate end = input.promptDateOrNull("Дата окончания гггг-мм-дд (Enter — пропустить): ");
        System.out.println("Договор предложен, ждёт подтверждения: "
                + service.create(forwarder.getId(), orderId, counterpartyId, price, terms, start, end));
    }

    private void acceptContract(User user) {
        service.accept(user.getId(), input.promptId("ID договора: "));
        System.out.println("Договор подтверждён");
    }

    private void rejectContract(User user) {
        service.reject(user.getId(), input.promptId("ID договора: "));
        System.out.println("Договор отклонён");
    }

    private void requestAmendment(User user) {
        long contractId = input.promptId("ID договора: ");
        System.out.println("Новые значения (Enter — не менять):");
        BigDecimal price = input.promptDecimalOrNull("Цена: ");
        String terms = input.prompt("Условия: ");
        LocalDate start = input.promptDateOrNull("Дата начала гггг-мм-дд: ");
        LocalDate end = input.promptDateOrNull("Дата окончания гггг-мм-дд: ");
        System.out.println("Запрос отправлен второй стороне: "
                + service.requestAmendment(user.getId(), party, contractId, price, terms, start, end));
    }

    private void requestTermination(User user) {
        System.out.println("Запрос отправлен второй стороне: "
                + service.requestTermination(user.getId(), party, input.promptId("ID договора: ")));
    }

    private void respond(User user, boolean accept) {
        service.respond(user.getId(), party, input.promptId("ID запроса: "), accept);
        System.out.println(accept ? "Запрос принят" : "Запрос отклонён");
    }

    private void cancelRequest(User user) {
        service.cancelRequest(user.getId(), input.promptId("ID запроса: "));
        System.out.println("Запрос отозван");
    }
}
