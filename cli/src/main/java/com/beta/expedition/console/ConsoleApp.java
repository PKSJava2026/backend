package com.beta.expedition.console;

import com.beta.expedition.exception.BusinessException;
import com.beta.expedition.exception.DatabaseException;
import com.beta.expedition.exception.EntityNotFoundException;
import com.beta.expedition.model.Role;
import com.beta.expedition.model.User;
import com.beta.expedition.service.AuthService;

import java.util.List;

public class ConsoleApp {

    private final AuthService authService;
    private final Input input;

    public ConsoleApp(AuthService authService, Input input) {
        this.authService = authService;
        this.input = input;
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
        if (user.getRole() == Role.ADMIN) {
            adminMenu(user);
        } else {
            // ponytail: меню ролей добавляются по мере реализации функций (заявки, договоры и т.д.)
            while (true) {
                System.out.println();
                System.out.println("Вы вошли как " + user.getNickname() + " (" + user.getRole() + ")");
                System.out.println("0. Выйти из аккаунта");
                if (input.prompt("Выберите действие: ").equals("0")) {
                    return;
                }
                System.out.println("Ошибка: нет такого пункта меню");
            }
        }
    }

    private void adminMenu(User admin) {
        while (true) {
            System.out.println();
            System.out.println("==== АДМИНИСТРАТОР ====");
            System.out.println("1. Назначить экспедитора");
            System.out.println("2. Список экспедиторов");
            System.out.println("3. Удалить экспедитора");
            System.out.println("0. Выйти из аккаунта");

            switch (input.prompt("Выберите действие: ")) {
                case "1" -> safely(() -> addForwarder(admin));
                case "2" -> safely(() -> printList(authService.listForwarders(admin)));
                case "3" -> safely(() -> removeForwarder(admin));
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

    private void printList(List<?> items) {
        if (items.isEmpty()) {
            System.out.println("Список пуст");
            return;
        }
        items.forEach(System.out::println);
    }

    /** Ошибки бизнес-правил и БД не должны завершать программу. */
    private void safely(Runnable action) {
        try {
            action.run();
        } catch (BusinessException | EntityNotFoundException | DatabaseException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
