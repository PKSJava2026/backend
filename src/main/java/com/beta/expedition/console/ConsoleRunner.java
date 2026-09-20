package com.beta.expedition.console;

import com.beta.expedition.model.AuthenticatedUser;
import com.beta.expedition.model.Order;
import com.beta.expedition.model.enums.ContractStatus;
import com.beta.expedition.model.enums.RoleName;
import com.beta.expedition.service.AuthException;
import com.beta.expedition.service.AuthService;
import com.beta.expedition.service.ForwarderViewService;
import com.beta.expedition.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class ConsoleRunner implements CommandLineRunner {

    private final AuthService authService;
    private final OrderService orderService;
    private final ForwarderViewService forwarderViewService;
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void run(String... args) {
        System.out.println("Chao! How are you today?");
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("Please, choose any option of the following");
            System.out.println("1. Log in");
            System.out.println("2. Register");
            System.out.println("0. Exit");

            switch (prompt("> ")) {
                case "0" -> running = false;
                case "1" -> authenticate(this::logIn);
                case "2" -> authenticate(this::register);
                default -> System.out.println("Incorrect choice");
            }
        }
        System.out.println("Bye!");
        scanner.close();
    }

    private void authenticate(Supplier<AuthenticatedUser> action) {
        AuthenticatedUser user;
        try {
            user = action.get();
        } catch (AuthException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }
        System.out.println();
        System.out.println("Welcome, " + user.username() + "!");
        openUserMenu(user);
    }

    private AuthenticatedUser logIn() {
        String username = prompt("Nickname: ");
        String password = prompt("Password: ");
        return authService.login(username, password);
    }

    private AuthenticatedUser register() {
        String username = prompt("Nickname: ");
        String email = prompt("Email: ");
        String password = prompt("Password: ");
        RoleName role = chooseRole();
        return authService.register(username, email, password, role);
    }

    private RoleName chooseRole() {
        System.out.println("Choose your role:");
        System.out.println("1. Customer");
        System.out.println("2. Carrier");
        return switch (prompt("> ")) {
            case "1" -> RoleName.CUSTOMER;
            case "2" -> RoleName.CARRIER;
            default -> throw new AuthException("Unknown role");
        };
    }

    private void openUserMenu(AuthenticatedUser user) {
        switch (user.role()) {
            case ADMIN -> adminMenu(user);
            case CUSTOMER -> customerMenu(user);
            case CARRIER -> stubMenu(user, "Carrier",
                    List.of("Open orders", "My applications", "My contracts", "Notifications"));
            case FORWARDER -> forwarderMenu(user);
        }
    }

    private void adminMenu(AuthenticatedUser admin) {
        while (true) {
            System.out.println();
            System.out.println("Logged in as " + admin.username() + " (Admin)");
            System.out.println("1. Add forwarder");
            System.out.println("0. Log out");

            switch (prompt("> ")) {
                case "0" -> {
                    return;
                }
                case "1" -> addForwarder(admin);
                default -> System.out.println("Incorrect choice");
            }
        }
    }

    private void addForwarder(AuthenticatedUser admin) {
        String username = prompt("Forwarder nickname: ");
        String email = prompt("Forwarder email: ");
        String password = prompt("Forwarder password: ");
        try {
            authService.createForwarder(admin, username, email, password);
            System.out.println("Forwarder '" + username + "' has been created");
        } catch (AuthException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void customerMenu(AuthenticatedUser customer) {
        while (true) {
            System.out.println();
            System.out.println("Logged in as " + customer.username() + " (Customer)");
            System.out.println("1. Создать заявку на перевозку");
            System.out.println("2. Мои заявки");
            System.out.println("3. Мои договоры");
            System.out.println("0. Log out");

            switch (prompt("> ")) {
                case "1" -> createOrder(customer);
                case "2" -> printList(orderService.getOrdersByCustomer(customer.id()));
                case "3" -> System.out.println("Раздел в разработке");
                case "0" -> {
                    return;
                }
                default -> System.out.println("Incorrect choice");
            }
        }
    }

    private void createOrder(AuthenticatedUser customer) {
        String cargo = prompt("Описание груза: ");
        String origin = prompt("Откуда: ");
        String destination = prompt("Куда: ");
        BigDecimal weight = parseBigDecimalOrNull(prompt("Вес, кг (можно пусто): "));
        BigDecimal volume = parseBigDecimalOrNull(prompt("Объём, м3 (можно пусто): "));

        try {
            Order order = orderService.createOrder(
                    customer.id(), cargo, weight, volume, origin, destination, null
            );
            System.out.println("Заявка создана: " + order);
        } catch (AuthException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void forwarderMenu(AuthenticatedUser forwarder) {
        while (true) {
            System.out.println();
            System.out.println("Logged in as " + forwarder.username() + " (Forwarder)");
            System.out.println("1. Все заявки");
            System.out.println("2. Актуальные заявки");
            System.out.println("3. Договоры с заказчиками (все)");
            System.out.println("4. Договоры с заказчиками (актуальные)");
            System.out.println("5. Договоры с заказчиками (расторгнутые)");
            System.out.println("6. Договоры с перевозчиками (все)");
            System.out.println("7. Договоры с перевозчиками (актуальные)");
            System.out.println("8. Договоры с перевозчиками (расторгнутые)");
            System.out.println("0. Log out");

            switch (prompt("> ")) {
                case "1" -> printList(forwarderViewService.getAllOrders());
                case "2" -> printList(forwarderViewService.getActiveOrders());
                case "3" -> printList(forwarderViewService.getCustomerContracts(null));
                case "4" -> printList(forwarderViewService.getCustomerContracts(ContractStatus.ACTIVE));
                case "5" -> printList(forwarderViewService.getCustomerContracts(ContractStatus.TERMINATED));
                case "6" -> printList(forwarderViewService.getCarrierContracts(null));
                case "7" -> printList(forwarderViewService.getCarrierContracts(ContractStatus.ACTIVE));
                case "8" -> printList(forwarderViewService.getCarrierContracts(ContractStatus.TERMINATED));
                case "0" -> {
                    return;
                }
                default -> System.out.println("Incorrect choice");
            }
        }
    }

    private void stubMenu(AuthenticatedUser user, String roleTitle, List<String> items) {
        while (true) {
            System.out.println();
            System.out.println("Logged in as " + user.username() + " (" + roleTitle + ")");
            for (int i = 0; i < items.size(); i++) {
                System.out.println((i + 1) + ". " + items.get(i));
            }
            System.out.println("0. Log out");

            String input = prompt("> ");
            if (input.equals("0")) {
                return;
            }
            int index = parseIndex(input, items.size());
            if (index >= 0) {
                System.out.println(items.get(index) + ": nothing so far");
            } else {
                System.out.println("Incorrect choice");
            }
        }
    }

    private void printList(List<?> items) {
        if (items.isEmpty()) {
            System.out.println("Список пуст");
            return;
        }
        items.forEach(System.out::println);
    }

    private int parseIndex(String input, int size) {
        try {
            int number = Integer.parseInt(input);
            return (number >= 1 && number <= size) ? number - 1 : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private BigDecimal parseBigDecimalOrNull(String input) {
        if (input == null || input.isBlank()) return null;
        try {
            return new BigDecimal(input.trim());
        } catch (NumberFormatException e) {
            System.out.println("Некорректное число, значение проигнорировано");
            return null;
        }
    }

    private String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }
}