package com.beta.expedition.console;

import com.beta.expedition.model.User;

public class CarrierMenu extends Menu {

    private final ContractMenu contracts;
    private final NotificationMenu notificationMenu;
    private final RatingMenu ratingMenu;
    private final CooperationMenu cooperationMenu;

    public CarrierMenu(Input input, ContractMenu contracts, NotificationMenu notificationMenu,
                       RatingMenu ratingMenu, CooperationMenu cooperationMenu) {
        super(input);
        this.contracts = contracts;
        this.notificationMenu = notificationMenu;
        this.ratingMenu = ratingMenu;
        this.cooperationMenu = cooperationMenu;
    }

    public void run(User carrier) {
        while (true) {
            System.out.println();
            System.out.println("==== ПЕРЕВОЗЧИК: " + carrier.getNickname() + " ====");
            System.out.println("1. Заявки о сотрудничестве");
            System.out.println("2. Мои договоры");
            System.out.println("3. " + notificationMenu.label(carrier));
            System.out.println("4. Мой рейтинг");
            System.out.println("0. Выйти из аккаунта");

            switch (input.prompt("Выберите действие: ")) {
                case "1" -> cooperationMenu.runForCarrier(carrier);
                case "2" -> contracts.run(carrier);
                case "3" -> notificationMenu.run(carrier);
                case "4" -> ratingMenu.showOwnRating(carrier);
                case "0" -> {
                    return;
                }
                default -> System.out.println("Ошибка: нет такого пункта меню");
            }
        }
    }
}
