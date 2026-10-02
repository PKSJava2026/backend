package com.beta.expedition.console;

import com.beta.expedition.model.User;

public class CarrierMenu extends Menu {

    private final ContractMenu contracts;
    private final NotificationMenu notificationMenu;

    public CarrierMenu(Input input, ContractMenu contracts, NotificationMenu notificationMenu) {
        super(input);
        this.contracts = contracts;
        this.notificationMenu = notificationMenu;
    }

    public void run(User carrier) {
        while (true) {
            System.out.println();
            System.out.println("==== ПЕРЕВОЗЧИК: " + carrier.getNickname() + " ====");
            System.out.println("1. Мои договоры");
            System.out.println("2. " + notificationMenu.label(carrier));
            System.out.println("0. Выйти из аккаунта");

            switch (input.prompt("Выберите действие: ")) {
                case "1" -> contracts.run(carrier);
                case "2" -> notificationMenu.run(carrier);
                case "0" -> {
                    return;
                }
                default -> System.out.println("Ошибка: нет такого пункта меню");
            }
        }
    }
}
