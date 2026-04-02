package com.sahnesen.api.sahnesen.enums;

public enum FollowStatus {
    PENDING, // Takip isteği atıldı, onay bekliyor (Gizli hesaplar için)
    ACCEPTED, // Takip işlemi onaylandı, şu an takipleşiyorlar
    REJECTED, // Takip isteği reddedildi
    MUTED, // Takip ediyor ama ana sayfasında içeriklerini görmek istemiyor
    BLOCKED // Tamamen engelledi
}
