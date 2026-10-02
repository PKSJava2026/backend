package com.beta.expedition.service;

import com.beta.expedition.model.AbstractContract;
import com.beta.expedition.model.ContractStatus;
import com.beta.expedition.model.Order;
import com.beta.expedition.model.OrderStatus;
import com.beta.expedition.repository.TableRepository;
import com.beta.expedition.util.CsvExporter;
import com.beta.expedition.util.ExcelExporter;
import com.beta.expedition.util.TableData;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportService {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final OrderService orders;
    private final ContractService<?> customerContracts;
    private final ContractService<?> carrierContracts;
    private final RatingService ratings;
    private final TableRepository tables;
    private final ExcelExporter excelExporter = new ExcelExporter();
    private final CsvExporter csvExporter = new CsvExporter();

    public ReportService(OrderService orders, ContractService<?> customerContracts,
                         ContractService<?> carrierContracts, RatingService ratings, TableRepository tables) {
        this.orders = orders;
        this.customerContracts = customerContracts;
        this.carrierContracts = carrierContracts;
        this.ratings = ratings;
        this.tables = tables;
    }

    public Map<String, String> statistics() {
        List<Order> allOrders = orders.listAll();
        Map<String, String> result = new LinkedHashMap<>();
        result.put("Заявок на перевозку (всего)", String.valueOf(allOrders.size()));
        result.put("Заявок в работе", String.valueOf(allOrders.stream()
                .filter(o -> o.getStatus() != OrderStatus.DELIVERED && o.getStatus() != OrderStatus.CANCELLED)
                .count()));
        result.put("Успешно выполнено заказов (доставлено)", String.valueOf(countOrders(allOrders, OrderStatus.DELIVERED)));
        result.put("Отменено заявок", String.valueOf(countOrders(allOrders, OrderStatus.CANCELLED)));

        List<? extends AbstractContract> customer = customerContracts.listAll();
        List<? extends AbstractContract> carrier = carrierContracts.listAll();
        result.put("Договоров с заказчиками (всего)", String.valueOf(customer.size()));
        result.put("Расторгнуто договоров с заказчиками", String.valueOf(countContracts(customer, ContractStatus.TERMINATED)));
        result.put("Договоров с перевозчиками (всего)", String.valueOf(carrier.size()));
        result.put("Расторгнуто договоров с перевозчиками", String.valueOf(countContracts(carrier, ContractStatus.TERMINATED)));
        result.put("Общий рейтинг", ratings.overallSummary().toString());
        return result;
    }

    public List<String> tableNames() {
        return tables.tableNames();
    }

    public TableData readTable(String name) {
        return tables.read(name);
    }

    public Path exportExcel(Path directory) {
        Path file = directory.resolve("expedition_" + LocalDateTime.now().format(STAMP) + ".xlsx");
        excelExporter.export(file, statistics(), readAllTables());
        return file.toAbsolutePath();
    }

    public Path exportCsv(Path directory) {
        Path target = directory.resolve("csv_" + LocalDateTime.now().format(STAMP));
        csvExporter.export(target, statistics(), readAllTables());
        return target.toAbsolutePath();
    }

    private List<TableData> readAllTables() {
        return tables.tableNames().stream().map(tables::read).toList();
    }

    private long countOrders(List<Order> list, OrderStatus status) {
        return list.stream().filter(o -> o.getStatus() == status).count();
    }

    private long countContracts(List<? extends AbstractContract> list, ContractStatus status) {
        return list.stream().filter(c -> c.getStatus() == status).count();
    }
}
