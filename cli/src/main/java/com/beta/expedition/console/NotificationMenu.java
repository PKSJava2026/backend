package com.beta.expedition.console;

import com.beta.expedition.model.User;
import com.beta.expedition.service.NotificationService;

public class NotificationMenu extends Menu {

    private final NotificationService notifications;

    public NotificationMenu(Input input, NotificationService notifications) {
        super(input);
        this.notifications = notifications;
    }

    public String label(User user) {
        return "Уведомления (новых: " + notifications.unreadCount(user.getId()) + ")";
    }

    public void run(User user) {
        while (true) {
            System.out.println();
            System.out.println("==== УВЕДОМЛЕНИЯ ====");
            safely(() -> printList(notifications.list(user.getId())));
            System.out.println("1. Отметить прочитанным");
            System.out.println("2. Отметить все прочитанными");
            System.out.println("0. Назад");

            switch (input.prompt("Выберите действие: ")) {
                case "1" -> safely(() -> notifications.markRead(user.getId(), input.promptId("ID уведомления: ")));
                case "2" -> safely(() -> notifications.markAllRead(user.getId()));
                case "0" -> {
                    return;
                }
                default -> System.out.println("Ошибка: нет такого пункта меню");
            }
        }
    }
}
