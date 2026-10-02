package com.beta.expedition.service;

import com.beta.expedition.exception.EntityNotFoundException;
import com.beta.expedition.model.Notification;
import com.beta.expedition.repository.NotificationRepository;

import java.util.List;

public class NotificationService {

    private final NotificationRepository notifications;

    public NotificationService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    /** @param changeRequestId связанный запрос на изменение/расторжение или null */
    public void notify(long userId, String message, Long changeRequestId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setChangeRequestId(changeRequestId);
        notification.setMessage(message);
        notifications.save(notification);
    }

    public List<Notification> list(long userId) {
        return notifications.findByUserId(userId);
    }

    public int unreadCount(long userId) {
        return notifications.countUnread(userId);
    }

    public void markRead(long userId, long notificationId) {
        if (!notifications.markRead(notificationId, userId)) {
            throw new EntityNotFoundException("Уведомление", notificationId);
        }
    }

    public int markAllRead(long userId) {
        return notifications.markAllRead(userId);
    }
}
