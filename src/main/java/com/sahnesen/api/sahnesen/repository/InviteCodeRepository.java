package com.sahnesen.api.sahnesen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sahnesen.api.sahnesen.entities.InviteCode;

import java.time.LocalDateTime;
import java.util.Optional;

public interface InviteCodeRepository extends JpaRepository<InviteCode, Long> {

    /**
     * Kod metnine göre ilgili davet kodunu getirir.
     * Küçük/büyük harf duyarlılığını ortadan kaldırmak için kod mantığı servis
     * katmanında toUpperCase yapılır.
     */
    Optional<InviteCode> findByCode(String code);

    /**
     * Kodun veri tabanında daha önce tanımlanıp tanımlanmadığını kontrol eder.
     * Yeni kişiye özel kodlar üretirken çakışma (collision) olmaması için
     * kullanılır.
     */
    boolean existsByCode(String code);

    /**
     * Kodun o an geçerli, aktif, kullanım limiti dolmamış ve süresi geçmemiş
     * olduğunu
     * veritabanı seviyesinde hızlıca doğrulamak için özel JPQL sorgusu.
     */
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM InviteCode i " +
            "WHERE i.code = :code " +
            "AND i.isActive = true " +
            "AND i.usedCount < i.maxUses " +
            "AND (i.expiresAt IS NULL OR i.expiresAt > :now)")
    boolean isCodeValid(@Param("code") String code, @Param("now") LocalDateTime now);
}
