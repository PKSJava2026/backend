package com.beta.expedition.service;

import com.beta.expedition.model.CarrierContract;
import com.beta.expedition.model.CustomerContract;
import com.beta.expedition.model.Order;
import com.beta.expedition.model.enums.ContractStatus;
import com.beta.expedition.model.enums.OrderStatus;
import com.beta.expedition.repository.CarrierContractRepository;
import com.beta.expedition.repository.CustomerContractRepository;
import com.beta.expedition.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ForwarderViewService {

    private static final Set<OrderStatus> CLOSED_ORDER_STATUSES = Set.of(
            OrderStatus.DELIVERED,
            OrderStatus.CANCELLED
    );

    private final OrderRepository orders;
    private final CustomerContractRepository customerContracts;
    private final CarrierContractRepository carrierContracts;

    public ForwarderViewService(OrderRepository orders,
                                CustomerContractRepository customerContracts,
                                CarrierContractRepository carrierContracts) {
        this.orders = orders;
        this.customerContracts = customerContracts;
        this.carrierContracts = carrierContracts;
    }

    public List<Order> getAllOrders() {
        return orders.findAll();
    }

    public List<Order> getActiveOrders() {
        return orders.findAll().stream()
                .filter(order -> !CLOSED_ORDER_STATUSES.contains(order.getStatus()))
                .toList();
    }

    public List<CustomerContract> getCustomerContracts(ContractStatus status) {
        if (status == null) {
            return customerContracts.findAll();
        }
        return customerContracts.findByStatus(status);
    }

    public List<CarrierContract> getCarrierContracts(ContractStatus status) {
        if (status == null) {
            return carrierContracts.findAll();
        }
        return carrierContracts.findByStatus(status);
    }
}
