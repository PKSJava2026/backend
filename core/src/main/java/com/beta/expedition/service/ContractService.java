package com.beta.expedition.service;

import com.beta.expedition.exception.BusinessException;
import com.beta.expedition.exception.EntityNotFoundException;
import com.beta.expedition.model.AbstractContract;
import com.beta.expedition.model.ChangeRequestStatus;
import com.beta.expedition.model.ChangeRequestType;
import com.beta.expedition.model.ContractChangeRequest;
import com.beta.expedition.model.ContractKind;
import com.beta.expedition.model.ContractStatus;
import com.beta.expedition.model.Order;
import com.beta.expedition.model.OrderStatus;
import com.beta.expedition.model.Party;
import com.beta.expedition.repository.ContractChangeRequestRepository;
import com.beta.expedition.repository.ContractRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.LongFunction;

public class ContractService<T extends AbstractContract> {

    private static final BigDecimal MAX_PRICE = new BigDecimal("10000000000"); // numeric(12, 2)

    private final ContractKind kind;
    private final ContractRepository<T> contracts;
    private final ContractChangeRequestRepository requests;
    private final OrderService orders;
    private final CarrierDirectory carriers;
    private final LongFunction<T> newContract;

    public ContractService(ContractKind kind, ContractRepository<T> contracts,
                           ContractChangeRequestRepository requests, OrderService orders,
                           CarrierDirectory carriers, LongFunction<T> newContract) {
        this.kind = kind;
        this.contracts = contracts;
        this.requests = requests;
        this.orders = orders;
        this.carriers = carriers;
        this.newContract = newContract;
    }

    public ContractKind getKind() {
        return kind;
    }

    public T create(long forwarderId, long orderId, long counterpartyId, BigDecimal price, String terms,
                    LocalDate startDate, LocalDate endDate) {
        validate(price, startDate, endDate);
        Order order = orders.getById(orderId);
        if (kind == ContractKind.CUSTOMER) {
            if (order.getCustomerId() != counterpartyId) {
                throw new BusinessException("Заявка #" + orderId + " принадлежит другому заказчику");
            }
            requireOrderStatus(order, OrderStatus.NEW);
        } else {
            if (!carriers.isActiveCarrier(counterpartyId)) {
                throw new BusinessException("Пользователь #" + counterpartyId + " не является активным перевозчиком");
            }
            requireOrderStatus(order, OrderStatus.CONTRACTED);
        }
        boolean hasOpenContract = contracts.findByOrderId(orderId).stream()
                .anyMatch(c -> c.getStatus() == ContractStatus.PENDING || c.getStatus() == ContractStatus.ACTIVE);
        if (hasOpenContract) {
            throw new BusinessException("По заявке #" + orderId
                    + " уже есть действующий или ожидающий подтверждения договор " + kind.getTitle());
        }

        T contract = newContract.apply(counterpartyId);
        contract.setOrderId(orderId);
        contract.setForwarderId(forwarderId);
        contract.setCreatedBy(forwarderId);
        contract.setPrice(price);
        contract.setTerms(blankToNull(terms));
        contract.setStartDate(startDate);
        contract.setEndDate(endDate);
        contract.setStatus(ContractStatus.PENDING);
        return contracts.save(contract);
    }

    public void accept(long actorId, long contractId) {
        T contract = getOwnPending(actorId, contractId);
        if (kind == ContractKind.CUSTOMER) {
            orders.changeStatus(contract.getOrderId(), OrderStatus.CONTRACTED);
        }
        contract.setStatus(ContractStatus.ACTIVE);
        contracts.update(contract);
    }

    public void reject(long actorId, long contractId) {
        T contract = getOwnPending(actorId, contractId);
        contract.setStatus(ContractStatus.REJECTED);
        contracts.update(contract);
    }

    public T get(long contractId) {
        return contracts.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Договор " + kind.getTitle(), contractId));
    }

    public T get(long actorId, Party party, long contractId) {
        T contract = get(contractId);
        if (party == Party.COUNTERPARTY && contract.getCounterpartyId() != actorId) {
            throw new EntityNotFoundException("Договор " + kind.getTitle(), contractId);
        }
        return contract;
    }

    public List<T> listAll() {
        return contracts.findAll();
    }

    public List<T> listByStatus(ContractStatus status) {
        return contracts.findByStatus(status);
    }

    public List<T> listForCounterparty(long counterpartyId) {
        return contracts.findByCounterpartyId(counterpartyId);
    }

    public ContractChangeRequest requestAmendment(long actorId, Party party, long contractId, BigDecimal price,
                                                  String terms, LocalDate startDate, LocalDate endDate) {
        T contract = requireActive(get(actorId, party, contractId));
        if (price == null && blankToNull(terms) == null && startDate == null && endDate == null) {
            throw new BusinessException("Укажите хотя бы одно новое значение");
        }
        validate(price != null ? price : contract.getPrice(),
                startDate != null ? startDate : contract.getStartDate(),
                endDate != null ? endDate : contract.getEndDate());
        ContractChangeRequest request = newRequest(actorId, contract, ChangeRequestType.AMEND);
        request.setProposedPrice(price);
        request.setProposedTerms(blankToNull(terms));
        request.setProposedStartDate(startDate);
        request.setProposedEndDate(endDate);
        return requests.save(request);
    }

    public ContractChangeRequest requestTermination(long actorId, Party party, long contractId) {
        T contract = requireActive(get(actorId, party, contractId));
        return requests.save(newRequest(actorId, contract, ChangeRequestType.TERMINATE));
    }

    public List<ContractChangeRequest> listPendingRequests(long actorId, Party party) {
        if (party == Party.COUNTERPARTY) {
            return requests.findPendingByCounterparty(kind, actorId);
        }
        return requests.findPending(kind);
    }

    public void respond(long actorId, Party party, long requestId, boolean accept) {
        ContractChangeRequest request = getPendingRequest(requestId);
        T contract = get(actorId, party, request.getContractId());
        boolean initiatedByCounterparty = request.getInitiatedBy().equals(contract.getCounterpartyId());
        Party responder = initiatedByCounterparty ? Party.FORWARDER : Party.COUNTERPARTY;
        if (party != responder) {
            throw new BusinessException("Запрос должна подтвердить другая сторона договора");
        }
        if (accept) {
            apply(request, requireActive(contract));
        }
        requests.resolve(requestId, accept ? ChangeRequestStatus.ACCEPTED : ChangeRequestStatus.REJECTED);
    }

    public void cancelRequest(long actorId, long requestId) {
        ContractChangeRequest request = getPendingRequest(requestId);
        if (!request.getInitiatedBy().equals(actorId)) {
            throw new BusinessException("Отозвать запрос может только его автор");
        }
        requests.resolve(requestId, ChangeRequestStatus.CANCELLED);
    }

    private void apply(ContractChangeRequest request, T contract) {
        if (request.getType() == ChangeRequestType.TERMINATE) {
            contract.setStatus(ContractStatus.TERMINATED);
            if (kind == ContractKind.CUSTOMER) {
                Order order = orders.getById(contract.getOrderId());
                if (order.getStatus().canTransitionTo(OrderStatus.CANCELLED)) {
                    orders.changeStatus(order, OrderStatus.CANCELLED);
                }
            }
        } else {
            if (request.getProposedPrice() != null) contract.setPrice(request.getProposedPrice());
            if (request.getProposedTerms() != null) contract.setTerms(request.getProposedTerms());
            if (request.getProposedStartDate() != null) contract.setStartDate(request.getProposedStartDate());
            if (request.getProposedEndDate() != null) contract.setEndDate(request.getProposedEndDate());
            validate(contract.getPrice(), contract.getStartDate(), contract.getEndDate());
        }
        contracts.update(contract);
    }

    private ContractChangeRequest newRequest(long actorId, T contract, ChangeRequestType type) {
        if (requests.findPendingByContract(kind, contract.getId()).isPresent()) {
            throw new BusinessException("По договору #" + contract.getId() + " уже есть неподтверждённый запрос");
        }
        ContractChangeRequest request = new ContractChangeRequest();
        request.setKind(kind);
        request.setContractId(contract.getId());
        request.setType(type);
        request.setInitiatedBy(actorId);
        return request;
    }

    private ContractChangeRequest getPendingRequest(long requestId) {
        ContractChangeRequest request = requests.findById(requestId)
                .filter(r -> r.getKind() == kind)
                .orElseThrow(() -> new EntityNotFoundException("Запрос", requestId));
        if (request.getStatus() != ChangeRequestStatus.PENDING) {
            throw new BusinessException("Запрос #" + requestId + " уже обработан (" + request.getStatus() + ")");
        }
        return request;
    }

    private T getOwnPending(long actorId, long contractId) {
        T contract = get(actorId, Party.COUNTERPARTY, contractId);
        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new BusinessException("Договор #" + contractId + " не ожидает подтверждения (статус "
                    + contract.getStatus() + ")");
        }
        return contract;
    }

    private T requireActive(T contract) {
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new BusinessException("Изменить или расторгнуть можно только действующий договор (статус "
                    + contract.getStatus() + ")");
        }
        return contract;
    }

    private void requireOrderStatus(Order order, OrderStatus required) {
        if (order.getStatus() != required) {
            throw new BusinessException("Договор " + kind.getTitle() + " можно заключить только по заявке в статусе "
                    + required + " (сейчас " + order.getStatus() + ")");
        }
    }

    private void validate(BigDecimal price, LocalDate startDate, LocalDate endDate) {
        if (price == null || price.signum() <= 0) {
            throw new BusinessException("Цена должна быть больше нуля");
        }
        if (price.scale() > 2 || price.compareTo(MAX_PRICE) >= 0) {
            throw new BusinessException("Цена: не более двух знаков после запятой и меньше " + MAX_PRICE);
        }
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException("Дата окончания раньше даты начала");
        }
    }

    private String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }
}
