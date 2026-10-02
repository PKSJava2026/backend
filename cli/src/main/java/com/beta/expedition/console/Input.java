package com.beta.expedition.console;

import com.beta.expedition.exception.BusinessException;

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
}
