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
