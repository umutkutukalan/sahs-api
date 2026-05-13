package com.sahnesen.api.sahnesen.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.sahnesen.api.sahnesen.entities.Notification;

import io.lettuce.core.dynamic.annotation.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Kullanıcının bildirimlerini kronolojik sırayla getirmek için
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Okunmamış bildirims sayısını getirmek için (Sağ üstte kırmızı nokta
    // belirteceğiz)
    long countByUserIdAndIsReadFalse(Long userId);

    // 
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);

}
