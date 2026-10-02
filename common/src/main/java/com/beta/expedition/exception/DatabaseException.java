package com.beta.expedition.exception;

/** Ошибка подключения к БД или выполнения SQL-запроса. */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
