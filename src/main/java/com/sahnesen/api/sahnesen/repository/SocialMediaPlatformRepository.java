package com.sahnesen.api.sahnesen.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sahnesen.api.sahnesen.entities.SocialMediaPlatform;
import com.sahnesen.api.sahnesen.enums.PlatformType;

public interface SocialMediaPlatformRepository extends JpaRepository<SocialMediaPlatform, Long> {

    // Kullanıcının belirli bir platformu var mı? (Mükerrer kaydı önlemek için)
    boolean existsByUser_UsernameAndPlatform(String username, PlatformType platform);

    // Kullanıcının tüm sosyal medya hesaplarını getir
    List<SocialMediaPlatform> findAllByUser_Username(String username);

    // Sadece public olanları getir (Başkaları profiline baktığında)
    List<SocialMediaPlatform> findAllByUser_UsernameAndIsPublicTrue(String username);

}
