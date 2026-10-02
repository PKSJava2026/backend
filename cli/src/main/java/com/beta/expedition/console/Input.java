package com.beta.expedition.console;

import com.beta.expedition.exception.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class Input {

    private final Scanner scanner = new Scanner(System.in);

    public String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    public long promptId(String message) {
        String text = prompt(message);
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            throw new BusinessException("ID должен быть целым числом");
        }
    }

    public int promptInt(String message) {
        String text = prompt(message);
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new BusinessException("Нужно ввести целое число, а не '" + text + "'");
        }
    }

    public BigDecimal promptDecimalOrNull(String message) {
        String text = prompt(message);
        if (text.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(text.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new BusinessException("Нужно ввести число, а не '" + text + "'");
        }
    }

    public <E extends Enum<E>> E promptEnumOrNull(String title, Class<E> type) {
        E[] values = type.getEnumConstants();
        System.out.println(title + " (Enter — любое):");
        for (int i = 0; i < values.length; i++) {
            System.out.println((i + 1) + ". " + values[i]);
        }
        String text = prompt("Выберите вариант: ");
        if (text.isEmpty()) {
            return null;
        }
        try {
            int number = Integer.parseInt(text);
            if (number >= 1 && number <= values.length) {
                return values[number - 1];
            }
        } catch (NumberFormatException ignored) {}
        throw new BusinessException("Нет такого варианта");
    }

    public <E extends Enum<E>> E promptEnum(String title, Class<E> type) {
        E value = promptEnumOrNull(title, type);
        if (value == null) {
            throw new BusinessException("Нужно выбрать вариант");
        }
        return value;
    }

    public boolean promptDescending() {
        System.out.println("1. По возрастанию");
        System.out.println("2. По убыванию");
        return switch (prompt("Выберите порядок: ")) {
            case "1" -> false;
            case "2" -> true;
            default -> throw new BusinessException("Нет такого варианта");
        };
    }

    public LocalDate promptDateOrNull(String message) {
        String text = prompt(message);
        if (text.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new BusinessException("Дата должна быть в формате гггг-мм-дд");
        }
    }
}
