package com.beta.expedition.console;

import com.beta.expedition.exception.BusinessException;
import com.beta.expedition.model.Role;
import com.beta.expedition.model.User;
import com.beta.expedition.service.AuthService;

public class ConsoleApp extends Menu {

    private final AuthService authService;
    private final CustomerMenu customerMenu;
    private final ForwarderMenu forwarderMenu;
    private final CarrierMenu carrierMenu;
    private final ReportMenu reportMenu;

    public ConsoleApp(Input input, AuthService authService, CustomerMenu customerMenu,
                      ForwarderMenu forwarderMenu, CarrierMenu carrierMenu, ReportMenu reportMenu) {
        super(input);
        this.authService = authService;
        this.customerMenu = customerMenu;
        this.forwarderMenu = forwarderMenu;
        this.carrierMenu = carrierMenu;
        this.reportMenu = reportMenu;
    }

    public void run() {
        while (true) {
            System.out.println();
            System.out.println("==== ЭКСПЕДИТОРСКАЯ КОМПАНИЯ ====");
            System.out.println("1. Войти");
            System.out.println("2. Зарегистрироваться");
            System.out.println("3. Выход");

            switch (input.prompt("Выберите действие: ")) {
                case "1" -> safely(this::logIn);
                case "2" -> safely(this::register);
                case "3" -> {
                    System.out.println("До свидания!");
                    return;
                }
                default -> System.out.println("Ошибка: нет такого пункта меню");
            }
        }
    }

    private void logIn() {
        String nickname = input.prompt("Никнейм: ");
        String password = input.prompt("Пароль: ");
        openUserMenu(authService.login(nickname, password));
    }

    private void register() {
        System.out.println();
        System.out.println("Выберите тип аккаунта:");
        System.out.println("1. Заказчик");
        System.out.println("2. Перевозчик");
        System.out.println("3. Назад");
        Role role = switch (input.prompt("Выберите действие: ")) {
            case "1" -> Role.CUSTOMER;
            case "2" -> Role.CARRIER;
            case "3" -> null;
            default -> throw new BusinessException("Нет такого пункта меню");
        };
        if (role == null) {
            return;
        }
        String nickname = input.prompt("Никнейм: ");
        String email = input.prompt("Email: ");
        String password = input.prompt("Пароль: ");
        openUserMenu(authService.register(nickname, email, password, role));
    }

    private void openUserMenu(User user) {
        System.out.println();
        System.out.println("Добро пожаловать, " + user.getNickname() + "!");
        switch (user.getRole()) {
            case ADMIN -> adminMenu(user);
            case CUSTOMER -> customerMenu.run(user);
            case FORWARDER -> forwarderMenu.run(user);
            case CARRIER -> carrierMenu.run(user);
        }
    }

    private void adminMenu(User admin) {
        while (true) {
            System.out.println();
            System.out.println("==== АДМИНИСТРАТОР ====");
            System.out.println("1. Назначить экспедитора");
            System.out.println("2. Список экспедиторов");
            System.out.println("3. Удалить экспедитора");
            System.out.println("4. Функции экспедитора");
            System.out.println("5. Статистика и экспорт данных");
            System.out.println("0. Выйти из аккаунта");

            switch (input.prompt("Выберите действие: ")) {
                case "1" -> safely(() -> addForwarder(admin));
                case "2" -> safely(() -> printList(authService.listForwarders(admin)));
                case "3" -> safely(() -> removeForwarder(admin));
                case "4" -> forwarderMenu.run(admin);
                case "5" -> reportMenu.run();
                case "0" -> {
                    return;
                }
                default -> System.out.println("Ошибка: нет такого пункта меню");
            }
        }
    }

    private void addForwarder(User admin) {
        String nickname = input.prompt("Никнейм экспедитора: ");
        String email = input.prompt("Email экспедитора: ");
        String password = input.prompt("Пароль экспедитора: ");
        User forwarder = authService.createForwarder(admin, nickname, email, password);
        System.out.println("Экспедитор создан: " + forwarder);
    }

    private void removeForwarder(User admin) {
        authService.removeForwarder(admin, input.promptId("ID экспедитора: "));
        System.out.println("Экспедитор удалён");
    }
}
