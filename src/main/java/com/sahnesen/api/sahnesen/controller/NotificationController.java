package com.sahnesen.api.sahnesen.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sahnesen.api.sahnesen.entities.Notification;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.repository.NotificationRepository;
import com.sahnesen.api.sahnesen.services.NotificationService;
import com.sahnesen.api.sahnesen.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Principal.getName() genellikle username döner
        // UserService'de username üzerinden ID bulan bir metodun olduğunu varsayıyoruz
        User user = userService.findByUsername(principal.getName());

        // NotificationService üzerinden kronolojik listeyi çekiyoruz
        List<Notification> notifications = notificationService.getUserNotifications(user.getId());

        return ResponseEntity.ok(notifications);

    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        long count = notificationRepository.countByUserIdAndIsReadFalse(user.getId());
        return ResponseEntity.ok(count);
    }

    // Tekil okundu işaretleme
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, Principal principal) {
        notificationService.markAsRead(id, principal.getName());
        return ResponseEntity.ok().build();
    }

    // Tümü okundu işaretleme
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok().build();
    }
}
