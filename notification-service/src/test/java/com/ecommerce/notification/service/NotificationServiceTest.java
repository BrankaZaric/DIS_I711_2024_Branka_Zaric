package com.ecommerce.notification.service;

import com.ecommerce.notification.dto.NotificationResponse;
import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setId(1L);
        notification.setType(Notification.NotificationType.EMAIL);
        notification.setRecipient("test@example.com");
        notification.setSubject("Test Notification");
        notification.setMessage("This is a test message");
        notification.setStatus(Notification.NotificationStatus.SENT);
        notification.setRelatedEntityId("ORD-12345678");
    }

    @Test
    void sendNotification_ShouldCreateNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationResponse response = notificationService.sendNotification(
                Notification.NotificationType.EMAIL,
                "test@example.com",
                "Test Subject",
                "Test Message",
                "ORD-12345678"
        );

        assertThat(response).isNotNull();
        assertThat(response.getRecipient()).isEqualTo("test@example.com");
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void getNotificationById_ShouldReturnNotification() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        NotificationResponse response = notificationService.getNotificationById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRecipient()).isEqualTo("test@example.com");
    }

    @Test
    void getNotificationById_ShouldThrowException_WhenNotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> notificationService.getNotificationById(999L));
    }

    @Test
    void getNotificationsByRecipient_ShouldReturnNotifications() {
        when(notificationRepository.findByRecipient("test@example.com"))
                .thenReturn(Arrays.asList(notification));

        List<NotificationResponse> responses = notificationService
                .getNotificationsByRecipient("test@example.com");

        assertThat(responses).isNotEmpty();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getRecipient()).isEqualTo("test@example.com");
    }

    @Test
    void getAllNotifications_ShouldReturnAllNotifications() {
        when(notificationRepository.findAll()).thenReturn(Arrays.asList(notification));

        List<NotificationResponse> responses = notificationService.getAllNotifications();

        assertThat(responses).isNotEmpty();
        assertThat(responses).hasSize(1);
    }

    @Test
    void getNotificationsByStatus_ShouldReturnNotifications() {
        when(notificationRepository.findByStatus(Notification.NotificationStatus.SENT))
                .thenReturn(Arrays.asList(notification));

        List<NotificationResponse> responses = notificationService
                .getNotificationsByStatus(Notification.NotificationStatus.SENT);

        assertThat(responses).isNotEmpty();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo(Notification.NotificationStatus.SENT);
    }
}