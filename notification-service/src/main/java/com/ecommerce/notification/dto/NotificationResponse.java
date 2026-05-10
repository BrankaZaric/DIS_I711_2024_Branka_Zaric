package com.ecommerce.notification.dto;

import com.ecommerce.notification.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private Notification.NotificationType type;
    private String recipient;
    private String subject;
    private String message;
    private Notification.NotificationStatus status;
    private String relatedEntityId;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    public static NotificationResponse fromEntity(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setType(notification.getType());
        response.setRecipient(notification.getRecipient());
        response.setSubject(notification.getSubject());
        response.setMessage(notification.getMessage());
        response.setStatus(notification.getStatus());
        response.setRelatedEntityId(notification.getRelatedEntityId());
        response.setCreatedAt(notification.getCreatedAt());
        response.setSentAt(notification.getSentAt());
        return response;
    }
}