package com.beta.expedition.service;

import com.beta.expedition.model.Order;
import com.beta.expedition.model.enums.OrderStatus;
import com.beta.expedition.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orders;

    public OrderService(OrderRepository orders) {
        this.orders = orders;
    }

    public Order createOrder(UUID customerId, String cargoDescription, BigDecimal weightKg,
                              BigDecimal volumeM3, String origin, String destination, LocalDate desiredDate) {
        if (cargoDescription == null || cargoDescription.isBlank()) {
            throw new AuthException("Описание груза обязательно");
        }
        if (origin == null || origin.isBlank() || destination == null || destination.isBlank()) {
            throw new AuthException("Пункт отправления и назначения обязательны");
        }
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setCargoDescription(cargoDescription);
        order.setWeightKg(weightKg);
        order.setVolumeM3(volumeM3);
        order.setOrigin(origin);
        order.setDestination(destination);
        order.setDesiredDate(desiredDate);
        order.setStatus(OrderStatus.NEW);
        return orders.save(order);
    }

    public List<Order> getOrdersByCustomer(UUID customerId) {
        return orders.findAll().stream()
                .filter(o -> o.getCustomerId().equals(customerId))
                .toList();
    }
}