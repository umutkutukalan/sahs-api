package com.sahnesen.api.sahnesen.enums;

public enum BadgeCategory {
    CONTENT_COUNT, // Genel içerik üretme sayısı (Her türden toplam)
    POST_TYPE_EXPERT, // Belirli bir türde (örn: 10 adet YANYANA veya SAHNE) uzmanlaşma
    FOLLOWER, // Takipçi sayısı
    ENGAGEMENT_BY_TYPE, // Mod bazlı alınan etkileşimler (Örn: SHINE_YANYANA veya SHINE_SAHNE barajları)
    SIGNATURE_COUNT, // "İmza At" (Onay)
    TICKET_SALES, // Bilet satış sayısı
    SPECIAL // Manuel veya hibrit şartlar
}