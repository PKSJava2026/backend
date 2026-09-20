package com.beta.expedition.console;

import com.beta.expedition.model.AuthenticatedUser;
import com.beta.expedition.model.enums.RoleName;
import com.beta.expedition.service.AuthException;
import com.beta.expedition.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class ConsoleRunner implements CommandLineRunner {

    private final AuthService authService;
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
            case CUSTOMER -> stubMenu(user, "Customer",
                    List.of("My orders", "My contracts", "Notifications"));
            case CARRIER -> stubMenu(user, "Carrier",
                    List.of("Open orders", "My applications", "My contracts", "Notifications"));
            case FORWARDER -> stubMenu(user, "Forwarder",
                    List.of("Orders", "Customer contracts", "Carrier applications", "Notifications"));
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

    private int parseIndex(String input, int size) {
        try {
            int number = Integer.parseInt(input);
            return (number >= 1 && number <= size) ? number - 1 : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }
}