package com.ecommerce.notification.controller;

import com.ecommerce.notification.dto.NotificationResponse;
import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "Notification management API")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @GetMapping("/recipient/{recipient}")
    @Operation(summary = "Get all notifications for a recipient")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByRecipient(@PathVariable String recipient) {
        return ResponseEntity.ok(notificationService.getNotificationsByRecipient(recipient));
    }

    @GetMapping
    @Operation(summary = "Get all notifications")
    public ResponseEntity<List<NotificationResponse>> getAllNotifications(
            @RequestParam(required = false) Notification.NotificationStatus status) {
        if (status != null) {
            return ResponseEntity.ok(notificationService.getNotificationsByStatus(status));
        }
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }
}