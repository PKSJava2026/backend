package com.beta.expedition;

import com.beta.expedition.exception.DatabaseException;
import com.beta.expedition.util.DatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        DatabaseManager db = new DatabaseManager();
        try (Connection ignored = db.getConnection()) {
            System.out.println("Подключение к базе данных установлено");
        } catch (DatabaseException | SQLException e) {
            System.out.println("Ошибка: " + e.getMessage());
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println();
                System.out.println("==== ЭКСПЕДИТОРСКАЯ КОМПАНИЯ ====");
                System.out.println("1. Войти");
                System.out.println("2. Зарегистрироваться");
                System.out.println("3. Выход");
                System.out.print("Выберите действие: ");

                switch (scanner.nextLine().trim()) {
                    case "1", "2" -> System.out.println("Раздел в разработке");
                    case "3" -> {
                        System.out.println("До свидания!");
                        return;
                    }
                    default -> System.out.println("Ошибка: нет такого пункта меню");
                }
            }
        }
    }
}
