package com.sahnesen.api.sahnesen.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sahnesen.api.sahnesen.entities.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Kullanıcının bildirimlerini kronolojik sırayla getirmek için
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Okunmamış bildirims sayısını getirmek için (Sağ üstte kırmızı nokta belirteceğiz)
    long countByUserIdAndIsReadFalse(Long userId);

}
