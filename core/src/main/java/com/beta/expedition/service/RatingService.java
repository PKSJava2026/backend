package com.beta.expedition.service;

import com.beta.expedition.exception.BusinessException;
import com.beta.expedition.model.AbstractContract;
import com.beta.expedition.model.ContractStatus;
import com.beta.expedition.model.Order;
import com.beta.expedition.model.OrderStatus;
import com.beta.expedition.model.Rating;
import com.beta.expedition.model.RatingSummary;
import com.beta.expedition.repository.RatingRepository;

import java.util.List;
import java.util.Optional;

public class RatingService {

    private static final int MIN_SCORE = 1;
    private static final int MAX_SCORE = 5;
    private static final int MAX_COMMENT_LENGTH = 500;

    private final RatingRepository ratings;
    private final OrderService orders;
    private final ContractService<?> customerContracts;
    private final ContractService<?> carrierContracts;

    public RatingService(RatingRepository ratings, OrderService orders, ContractService<?> customerContracts,
                         ContractService<?> carrierContracts) {
        this.ratings = ratings;
        this.orders = orders;
        this.customerContracts = customerContracts;
        this.carrierContracts = carrierContracts;
    }

    public Rating rateService(long customerId, long orderId, int score, String comment) {
        Order order = orders.getOwn(customerId, orderId);
        AbstractContract contract = findExecutedContract(customerContracts, orderId)
                .orElseThrow(() -> new BusinessException("По заявке #" + orderId + " нет договора с экспедитором"));
        return rate(order, customerId, contract.getForwarderId(), score, comment);
    }

    public Rating rateDelivery(long forwarderId, long orderId, int score, String comment) {
        Order order = orders.getById(orderId);
        AbstractContract contract = findExecutedContract(carrierContracts, orderId)
                .orElseThrow(() -> new BusinessException("По заявке #" + orderId + " нет договора с перевозчиком"));
        return rate(order, forwarderId, contract.getCounterpartyId(), score, comment);
    }

    public List<Order> listUnratedForCustomer(long customerId) {
        return orders.listByCustomer(customerId).stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED && !ratings.exists(o.getId(), customerId))
                .toList();
    }

    public List<Order> listUnratedForForwarder(long forwarderId) {
        return orders.listAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .filter(o -> findExecutedContract(carrierContracts, o.getId()).isPresent())
                .filter(o -> !ratings.exists(o.getId(), forwarderId))
                .toList();
    }

    public List<Rating> listReceived(long userId) {
        return ratings.findByToUserId(userId);
    }

    public RatingSummary summary(long userId) {
        List<Rating> received = ratings.findByToUserId(userId);
        double average = received.stream().mapToInt(Rating::getScore).average().orElse(0);
        return new RatingSummary(received.size(), average);
    }

    private Rating rate(Order order, long fromUserId, long toUserId, int score, String comment) {
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException("Оценить можно только доставленную заявку (сейчас " + order.getStatus() + ")");
        }
        if (score < MIN_SCORE || score > MAX_SCORE) {
            throw new BusinessException("Оценка должна быть от " + MIN_SCORE + " до " + MAX_SCORE);
        }
        if (comment != null && comment.length() > MAX_COMMENT_LENGTH) {
            throw new BusinessException("Комментарий не длиннее " + MAX_COMMENT_LENGTH + " символов");
        }
        if (ratings.exists(order.getId(), fromUserId)) {
            throw new BusinessException("Вы уже оценили заявку #" + order.getId());
        }
        Rating rating = new Rating();
        rating.setOrderId(order.getId());
        rating.setFromUserId(fromUserId);
        rating.setToUserId(toUserId);
        rating.setScore(score);
        rating.setComment(comment == null || comment.isBlank() ? null : comment.trim());
        return ratings.save(rating);
    }

    private Optional<AbstractContract> findExecutedContract(ContractService<?> service, long orderId) {
        return service.searchByOrder(orderId).stream()
                .filter(c -> c.getStatus() == ContractStatus.ACTIVE || c.getStatus() == ContractStatus.COMPLETED)
                .map(c -> (AbstractContract) c)
                .findFirst();
    }
}
