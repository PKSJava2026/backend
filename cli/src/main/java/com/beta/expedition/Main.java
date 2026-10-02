package com.beta.expedition;

import com.beta.expedition.console.ConsoleApp;
import com.beta.expedition.console.CustomerMenu;
import com.beta.expedition.console.Input;
import com.beta.expedition.exception.DatabaseException;
import com.beta.expedition.repository.OrderRepository;
import com.beta.expedition.repository.UserRepository;
import com.beta.expedition.service.AuthService;
import com.beta.expedition.service.OrderService;
import com.beta.expedition.util.DatabaseManager;

public class Main {

    public static void main(String[] args) {
        DatabaseManager db = new DatabaseManager();
        try {
            db.runScript("/schema.sql");
        } catch (DatabaseException e) {
            System.out.println("Ошибка: " + e.getMessage());
            return;
        }

        Input input = new Input();
        AuthService authService = new AuthService(new UserRepository(db));
        OrderService orderService = new OrderService(new OrderRepository(db));
        new ConsoleApp(input, authService, new CustomerMenu(input, orderService)).run();
    }
}
