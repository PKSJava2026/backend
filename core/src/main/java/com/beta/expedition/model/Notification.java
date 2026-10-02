package com.beta.expedition.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class Notification {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private Long id;
    private Long userId;
    private Long changeRequestId;
    private String message;
    private boolean read;
    private OffsetDateTime createdAt;

    @Override
    public String toString() {
        return (read ? "      " : "[НОВОЕ] ") + "#" + id + " "
                + createdAt.atZoneSameInstant(ZoneId.systemDefault()).format(FORMAT) + " " + message;
    }
}
