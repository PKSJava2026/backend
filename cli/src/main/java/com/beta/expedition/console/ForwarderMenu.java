package com.beta.expedition.console;

import com.beta.expedition.exception.BusinessException;
import com.beta.expedition.model.OrderStatus;
import com.beta.expedition.model.User;
import com.beta.expedition.service.OrderService;

public class ForwarderMenu extends Menu {

    private final OrderService orderService;
    private final ContractMenu customerContracts;
    private final ContractMenu carrierContracts;
    private final NotificationMenu notificationMenu;
    private final OrderQueryMenu orderQueryMenu;
    private final RatingMenu ratingMenu;
    private final CooperationMenu cooperationMenu;
    private final ReportMenu reportMenu;

    public ForwarderMenu(Input input, OrderService orderService, ContractMenu customerContracts,
                         ContractMenu carrierContracts, NotificationMenu notificationMenu,
                         RatingMenu ratingMenu, CooperationMenu cooperationMenu, ReportMenu reportMenu) {
        super(input);
        this.orderService = orderService;
        this.customerContracts = customerContracts;
        this.carrierContracts = carrierContracts;
        this.notificationMenu = notificationMenu;
        this.orderQueryMenu = new OrderQueryMenu(input, orderService);
        this.ratingMenu = ratingMenu;
        this.cooperationMenu = cooperationMenu;
        this.reportMenu = reportMenu;
    }

    public void run(User forwarder) {
        while (true) {
            System.out.println();
            System.out.println("==== ЭКСПЕДИТОР: " + forwarder.getNickname() + " ====");
            System.out.println("1. Все заявки");
            System.out.println("2. Актуальные заявки");
            System.out.println("3. Изменить статус заявки");
            System.out.println("4. Договоры с заказчиками");
            System.out.println("5. Договоры с перевозчиками");
            System.out.println("6. " + notificationMenu.label(forwarder));
            System.out.println("7. Поиск, фильтрация, сортировка заявок");
            System.out.println("8. Оценить доставку груза");
            System.out.println("9. Мой рейтинг");
            System.out.println("10. Заявки перевозчиков о сотрудничестве");
            System.out.println("11. Статистика и экспорт данных");
            System.out.println("0. Назад");

            switch (input.prompt("Выберите действие: ")) {
                case "1" -> safely(() -> printList(orderService.listAll()));
                case "2" -> safely(() -> printList(orderService.listActive()));
                case "3" -> safely(this::changeOrderStatus);
                case "4" -> customerContracts.run(forwarder);
                case "5" -> carrierContracts.run(forwarder);
                case "6" -> notificationMenu.run(forwarder);
                case "7" -> orderQueryMenu.run();
                case "8" -> ratingMenu.rateDeliveryAsForwarder(forwarder);
                case "9" -> ratingMenu.showOwnRating(forwarder);
                case "10" -> cooperationMenu.runForForwarder(forwarder);
                case "11" -> reportMenu.run();
                case "0" -> {
                    return;
                }
                default -> System.out.println("Ошибка: нет такого пункта меню");
            }
        }
    }

    private void changeOrderStatus() {
        long orderId = input.promptId("ID заявки: ");
        System.out.println("Новый статус:");
        System.out.println("1. В пути (IN_TRANSIT)");
        System.out.println("2. Доставлена (DELIVERED)");
        System.out.println("3. Отменена (CANCELLED)");
        OrderStatus status = switch (input.prompt("Выберите действие: ")) {
            case "1" -> OrderStatus.IN_TRANSIT;
            case "2" -> OrderStatus.DELIVERED;
            case "3" -> OrderStatus.CANCELLED;
            default -> throw new BusinessException("Нет такого пункта меню");
        };
        orderService.changeStatus(orderId, status);
        System.out.println("Статус заявки изменён на " + status);
    }
}
