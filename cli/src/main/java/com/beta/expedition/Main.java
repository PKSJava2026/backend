package com.beta.expedition;

import com.beta.expedition.console.CarrierMenu;
import com.beta.expedition.console.ConsoleApp;
import com.beta.expedition.console.ContractMenu;
import com.beta.expedition.console.CustomerMenu;
import com.beta.expedition.console.ForwarderMenu;
import com.beta.expedition.console.Input;
import com.beta.expedition.console.NotificationMenu;
import com.beta.expedition.exception.DatabaseException;
import com.beta.expedition.model.CarrierContract;
import com.beta.expedition.model.ContractKind;
import com.beta.expedition.model.CustomerContract;
import com.beta.expedition.model.Party;
import com.beta.expedition.model.Role;
import com.beta.expedition.repository.CarrierContractRepository;
import com.beta.expedition.repository.ContractChangeRequestRepository;
import com.beta.expedition.repository.CustomerContractRepository;
import com.beta.expedition.repository.NotificationRepository;
import com.beta.expedition.repository.OrderRepository;
import com.beta.expedition.repository.UserRepository;
import com.beta.expedition.service.AuthService;
import com.beta.expedition.service.CarrierDirectory;
import com.beta.expedition.service.ContractService;
import com.beta.expedition.service.NotificationService;
import com.beta.expedition.service.OrderService;
import com.beta.expedition.util.DatabaseManager;

public class Main {

    public static void main(String[] args) {
        DatabaseManager db = new DatabaseManager();
        try {
            db.runScript("/schema.sql");
        } catch (DatabaseException e) {
            System.out.println("Ошибка: " + e.getMessage());
            return;
        }

        UserRepository users = new UserRepository(db);
        AuthService authService = new AuthService(users);
        OrderService orderService = new OrderService(new OrderRepository(db));
        CarrierDirectory carriers = id -> users.findById(id)
                .filter(user -> user.isActive() && user.getRole() == Role.CARRIER)
                .isPresent();
        ContractChangeRequestRepository requests = new ContractChangeRequestRepository(db);
        NotificationService notificationService = new NotificationService(new NotificationRepository(db));

        ContractService<CustomerContract> customerContracts = new ContractService<>(
                ContractKind.CUSTOMER, new CustomerContractRepository(db), requests, orderService, carriers,
                notificationService,
                customerId -> {
                    CustomerContract contract = new CustomerContract();
                    contract.setCustomerId(customerId);
                    return contract;
                });
        ContractService<CarrierContract> carrierContracts = new ContractService<>(
                ContractKind.CARRIER, new CarrierContractRepository(db), requests, orderService, carriers,
                notificationService,
                carrierId -> {
                    CarrierContract contract = new CarrierContract();
                    contract.setCarrierId(carrierId);
                    return contract;
                });

        Input input = new Input();
        NotificationMenu notificationMenu = new NotificationMenu(input, notificationService);
        CustomerMenu customerMenu = new CustomerMenu(input, orderService,
                new ContractMenu(input, customerContracts, Party.COUNTERPARTY), notificationMenu);
        ForwarderMenu forwarderMenu = new ForwarderMenu(input, orderService,
                new ContractMenu(input, customerContracts, Party.FORWARDER),
                new ContractMenu(input, carrierContracts, Party.FORWARDER), notificationMenu);
        CarrierMenu carrierMenu = new CarrierMenu(input,
                new ContractMenu(input, carrierContracts, Party.COUNTERPARTY), notificationMenu);
        new ConsoleApp(input, authService, customerMenu, forwarderMenu, carrierMenu).run();
    }
}
