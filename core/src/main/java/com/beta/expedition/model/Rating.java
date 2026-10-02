package com.beta.expedition.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Rating {

    private Long id;
    private Long orderId;
    private Long fromUserId;
    private Long toUserId;
    private int score;
    private String comment;
    private OffsetDateTime createdAt;

    @Override
    public String toString() {
        return "Оценка #" + id + " по заявке #" + orderId + ": " + score + "/5 от пользователя #" + fromUserId
                + (comment != null ? " — " + comment : "");
    }
}
