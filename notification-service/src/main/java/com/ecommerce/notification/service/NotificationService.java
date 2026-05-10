package com.ecommerce.notification.service;

import com.ecommerce.notification.dto.NotificationResponse;
import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationResponse sendNotification(Notification.NotificationType type,
                                                  String recipient,
                                                  String subject,
                                                  String message,
                                                  String relatedEntityId) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setRecipient(recipient);
        notification.setSubject(subject);
        notification.setMessage(message);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setStatus(Notification.NotificationStatus.PENDING);

        // Simulate sending (90% success rate)
        boolean sendSuccessful = Math.random() > 0.1;

        if (sendSuccessful) {
            notification.setStatus(Notification.NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            log.info("Notification sent successfully to {} via {}", recipient, type);
        } else {
            notification.setStatus(Notification.NotificationStatus.FAILED);
            log.warn("Failed to send notification to {} via {}", recipient, type);
        }

        Notification savedNotification = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(savedNotification);
    }

    public NotificationResponse getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
        return NotificationResponse.fromEntity(notification);
    }

    public List<NotificationResponse> getNotificationsByRecipient(String recipient) {
        return notificationRepository.findByRecipient(recipient).stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getNotificationsByStatus(Notification.NotificationStatus status) {
        return notificationRepository.findByStatus(status).stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }
}