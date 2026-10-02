package com.beta.expedition.repository;

import com.beta.expedition.exception.BusinessException;
import com.beta.expedition.exception.DatabaseException;
import com.beta.expedition.util.DatabaseManager;
import com.beta.expedition.util.TableData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TableRepository {

    private static final List<String> TABLES = List.of("users", "orders", "cooperation_requests",
            "customer_contracts", "carrier_contracts", "contract_change_requests", "ratings", "notifications");
    private static final String HIDDEN_COLUMN = "password_hash";
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DatabaseManager db;

    public TableRepository(DatabaseManager db) {
        this.db = db;
    }

    public List<String> tableNames() {
        return TABLES;
    }

    public TableData read(String table) {
        if (!TABLES.contains(table)) {
            throw new BusinessException("Неизвестная таблица: " + table);
        }
        String sql = "SELECT * FROM " + table + " ORDER BY 1";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            List<Integer> indexes = new ArrayList<>();
            List<String> columns = new ArrayList<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                if (!meta.getColumnName(i).equals(HIDDEN_COLUMN)) {
                    indexes.add(i);
                    columns.add(meta.getColumnName(i));
                }
            }
            List<List<Object>> rows = new ArrayList<>();
            while (rs.next()) {
                List<Object> row = new ArrayList<>();
                for (int index : indexes) {
                    row.add(convert(rs.getObject(index)));
                }
                rows.add(row);
            }
            return new TableData(table, columns, rows);
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось прочитать таблицу " + table + ": " + e.getMessage(), e);
        }
    }

    private Object convert(Object value) {
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().format(TIMESTAMP);
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate().toString();
        }
        return value;
    }
}
