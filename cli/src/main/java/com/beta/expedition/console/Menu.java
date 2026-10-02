package com.beta.expedition.console;

import com.beta.expedition.exception.BusinessException;
import com.beta.expedition.exception.DatabaseException;
import com.beta.expedition.exception.EntityNotFoundException;
import com.beta.expedition.exception.ExportException;

import java.util.List;

public abstract class Menu {

    protected final Input input;

    protected Menu(Input input) {
        this.input = input;
    }

    protected void safely(Runnable action) {
        try {
            action.run();
        } catch (BusinessException | EntityNotFoundException | DatabaseException | ExportException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    protected void printList(List<?> items) {
        if (items.isEmpty()) {
            System.out.println("Список пуст");
            return;
        }
        items.forEach(System.out::println);
    }
}
