package com.beta.expedition.util;

import com.beta.expedition.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Выдаёт JDBC-соединения; настройки читает из db.properties в classpath. */
public class DatabaseManager {

    private final String url;
    private final String user;
    private final String password;

    public DatabaseManager() {
        Properties props = new Properties();
        try (InputStream in = DatabaseManager.class.getResourceAsStream("/db.properties")) {
            if (in == null) {
                throw new DatabaseException("Файл db.properties не найден", null);
            }
            props.load(in);
        } catch (IOException e) {
            throw new DatabaseException("Не удалось прочитать db.properties", e);
        }
        this.url = props.getProperty("db.url");
        this.user = props.getProperty("db.user");
        this.password = props.getProperty("db.password");
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка подключения к базе данных: " + e.getMessage(), e);
        }
    }
}
