package com.beta.expedition.console;

import com.beta.expedition.model.Order;
import com.beta.expedition.model.User;
import com.beta.expedition.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CustomerMenu extends Menu {

    private final OrderService orderService;
    private final ContractMenu contracts;
    private final NotificationMenu notificationMenu;

    public CustomerMenu(Input input, OrderService orderService, ContractMenu contracts,
                        NotificationMenu notificationMenu) {
        super(input);
        this.orderService = orderService;
        this.contracts = contracts;
        this.notificationMenu = notificationMenu;
    }

    public void run(User customer) {
        while (true) {
            System.out.println();
            System.out.println("==== ЗАКАЗЧИК: " + customer.getNickname() + " ====");
            System.out.println("1. Создать заявку на перевозку");
            System.out.println("2. Мои заявки");
            System.out.println("3. Просмотр заявки и её статуса");
            System.out.println("4. Изменить заявку");
            System.out.println("5. Отменить заявку");
            System.out.println("6. Удалить заявку");
            System.out.println("7. Мои договоры");
            System.out.println("8. " + notificationMenu.label(customer));
            System.out.println("0. Выйти из аккаунта");

            switch (input.prompt("Выберите действие: ")) {
                case "1" -> safely(() -> createOrder(customer));
                case "2" -> safely(() -> printList(orderService.listByCustomer(customer.getId())));
                case "3" -> safely(() -> showOrder(customer));
                case "4" -> safely(() -> updateOrder(customer));
                case "5" -> safely(() -> cancelOrder(customer));
                case "6" -> safely(() -> deleteOrder(customer));
                case "7" -> contracts.run(customer);
                case "8" -> notificationMenu.run(customer);
                case "0" -> {
                    return;
                }
                default -> System.out.println("Ошибка: нет такого пункта меню");
            }
        }
    }

    private void createOrder(User customer) {
        String cargo = input.prompt("Описание груза: ");
        String origin = input.prompt("Откуда: ");
        String destination = input.prompt("Куда: ");
        BigDecimal weight = input.promptDecimalOrNull("Вес, кг (Enter — пропустить): ");
        BigDecimal volume = input.promptDecimalOrNull("Объём, м3 (Enter — пропустить): ");
        LocalDate date = input.promptDateOrNull("Желаемая дата гггг-мм-дд (Enter — пропустить): ");
        Order order = orderService.create(customer.getId(), cargo, weight, volume, origin, destination, date);
        System.out.println("Заявка создана: " + order);
    }

    private void showOrder(User customer) {
        System.out.println(orderService.getOwn(customer.getId(), input.promptId("ID заявки: ")));
    }

    private void updateOrder(User customer) {
        Order order = orderService.getEditable(customer.getId(), input.promptId("ID заявки: "));
        System.out.println(order);
        System.out.println("Новые значения (Enter — оставить прежнее):");
        String cargo = keep(input.prompt("Описание груза: "), order.getCargoDescription());
        String origin = keep(input.prompt("Откуда: "), order.getOrigin());
        String destination = keep(input.prompt("Куда: "), order.getDestination());
        BigDecimal weight = keep(input.promptDecimalOrNull("Вес, кг: "), order.getWeightKg());
        BigDecimal volume = keep(input.promptDecimalOrNull("Объём, м3: "), order.getVolumeM3());
        LocalDate date = keep(input.promptDateOrNull("Желаемая дата гггг-мм-дд: "), order.getDesiredDate());
        Order updated = orderService.update(customer.getId(), order.getId(), cargo, weight, volume,
                origin, destination, date);
        System.out.println("Заявка изменена: " + updated);
    }

    private void cancelOrder(User customer) {
        orderService.cancel(customer.getId(), input.promptId("ID заявки: "));
        System.out.println("Заявка отменена");
    }

    private void deleteOrder(User customer) {
        orderService.delete(customer.getId(), input.promptId("ID заявки: "));
        System.out.println("Заявка удалена");
    }

    private String keep(String entered, String current) {
        return entered.isEmpty() ? current : entered;
    }

    private <T> T keep(T entered, T current) {
        return entered != null ? entered : current;
    }
}
