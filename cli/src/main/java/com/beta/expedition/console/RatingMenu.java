package com.beta.expedition.console;

import com.beta.expedition.model.Rating;
import com.beta.expedition.model.User;
import com.beta.expedition.service.RatingService;

public class RatingMenu extends Menu {

    private final RatingService ratingService;

    public RatingMenu(Input input, RatingService ratingService) {
        super(input);
        this.ratingService = ratingService;
    }

    public void rateServiceAsCustomer(User customer) {
        safely(() -> {
            System.out.println("Заявки, ожидающие вашей оценки:");
            printList(ratingService.listUnratedForCustomer(customer.getId()));
            long orderId = input.promptId("ID заявки: ");
            int score = input.promptInt("Оценка услуги (1-5): ");
            String comment = input.prompt("Комментарий (Enter — пропустить): ");
            print(ratingService.rateService(customer.getId(), orderId, score, comment));
        });
    }

    public void rateDeliveryAsForwarder(User forwarder) {
        safely(() -> {
            System.out.println("Доставленные заявки, ожидающие вашей оценки перевозчика:");
            printList(ratingService.listUnratedForForwarder(forwarder.getId()));
            long orderId = input.promptId("ID заявки: ");
            int score = input.promptInt("Оценка доставки (1-5): ");
            String comment = input.prompt("Комментарий (Enter — пропустить): ");
            print(ratingService.rateDelivery(forwarder.getId(), orderId, score, comment));
        });
    }

    public void showOwnRating(User user) {
        safely(() -> {
            System.out.println("Ваш рейтинг: " + ratingService.summary(user.getId()));
            printList(ratingService.listReceived(user.getId()));
        });
    }

    private void print(Rating rating) {
        System.out.println("Оценка сохранена: " + rating);
    }
}
