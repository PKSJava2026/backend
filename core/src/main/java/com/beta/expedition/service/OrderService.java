package com.beta.expedition.service;

import com.beta.expedition.exception.BusinessException;
import com.beta.expedition.exception.EntityNotFoundException;
import com.beta.expedition.model.Order;
import com.beta.expedition.model.OrderStatus;
import com.beta.expedition.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class OrderService {

    private final OrderRepository orders;

    public OrderService(OrderRepository orders) {
        this.orders = orders;
    }

    public Order create(long customerId, String cargoDescription, BigDecimal weightKg, BigDecimal volumeM3,
                        String origin, String destination, LocalDate desiredDate) {
        validate(cargoDescription, weightKg, volumeM3, origin, destination, desiredDate);
        Order order = new Order();
        order.setCustomerId(customerId);
        fill(order, cargoDescription, weightKg, volumeM3, origin, destination, desiredDate);
        return orders.save(order);
    }

    public List<Order> listByCustomer(long customerId) {
        return orders.findByCustomerId(customerId);
    }

    public Order getOwn(long customerId, long orderId) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Заявка", orderId));
        if (order.getCustomerId() != customerId) {
            throw new EntityNotFoundException("Заявка", orderId);
        }
        return order;
    }

    public Order getEditable(long customerId, long orderId) {
        Order order = getOwn(customerId, orderId);
        requireNew(order, "изменить");
        return order;
    }

    public Order update(long customerId, long orderId, String cargoDescription, BigDecimal weightKg,
                        BigDecimal volumeM3, String origin, String destination, LocalDate desiredDate) {
        Order order = getEditable(customerId, orderId);
        validate(cargoDescription, weightKg, volumeM3, origin, destination, desiredDate);
        fill(order, cargoDescription, weightKg, volumeM3, origin, destination, desiredDate);
        orders.update(order);
        return order;
    }

    public void cancel(long customerId, long orderId) {
        changeStatus(getOwn(customerId, orderId), OrderStatus.CANCELLED);
    }

    public void delete(long customerId, long orderId) {
        requireNew(getOwn(customerId, orderId), "удалить");
        orders.delete(orderId);
    }

    public void changeStatus(Order order, OrderStatus next) {
        if (!order.getStatus().canTransitionTo(next)) {
            throw new BusinessException("Нельзя перевести заявку из статуса " + order.getStatus()
                    + " в " + next);
        }
        order.setStatus(next);
        orders.update(order);
    }

    private void requireNew(Order order, String action) {
        if (order.getStatus() != OrderStatus.NEW) {
            throw new BusinessException("Нельзя " + action + " заявку в статусе " + order.getStatus()
                    + " (только NEW)");
        }
    }

    private void validate(String cargoDescription, BigDecimal weightKg, BigDecimal volumeM3,
                          String origin, String destination, LocalDate desiredDate) {
        if (cargoDescription == null || cargoDescription.isBlank()) {
            throw new BusinessException("Описание груза обязательно");
        }
        if (origin == null || origin.isBlank() || destination == null || destination.isBlank()) {
            throw new BusinessException("Пункты отправления и назначения обязательны");
        }
        if (origin.trim().equalsIgnoreCase(destination.trim())) {
            throw new BusinessException("Пункты отправления и назначения не должны совпадать");
        }
        if (weightKg != null && weightKg.signum() <= 0) {
            throw new BusinessException("Вес должен быть больше нуля");
        }
        if (volumeM3 != null && volumeM3.signum() <= 0) {
            throw new BusinessException("Объём должен быть больше нуля");
        }
        if (desiredDate != null && desiredDate.isBefore(LocalDate.now())) {
            throw new BusinessException("Желаемая дата не может быть в прошлом");
        }
    }

    private void fill(Order order, String cargoDescription, BigDecimal weightKg, BigDecimal volumeM3,
                      String origin, String destination, LocalDate desiredDate) {
        order.setCargoDescription(cargoDescription.trim());
        order.setWeightKg(weightKg);
        order.setVolumeM3(volumeM3);
        order.setOrigin(origin.trim());
        order.setDestination(destination.trim());
        order.setDesiredDate(desiredDate);
    }
}
