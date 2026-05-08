package com.sahnesen.api.sahnesen.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BadgeType {
    // [ Kariyer Rozetleri ]
    FIRST_REHEARSAL("İlk Selam", "İlk provasını paylaşanlara verilir.", BadgeCategory.CONTENT_COUNT, 1),
    STAGE_DUST("Sahne Tozu", "10 içerik. Artık buralısın, ücretli sahne yetkisi kazandın!", BadgeCategory.CONTENT_COUNT, 10),
    MASTER_ACTOR("Usta Oyuncu", "50 içerik ve yüksek etkileşim.", BadgeCategory.CONTENT_COUNT, 50),
    MAIN_STAGE("Ana Sahne Oyuncusu", "50 içerik ve 5 biletli gösteri.", BadgeCategory.SPECIAL, 55), // Hibrit kontrol gerekecek
    BACKSTAGE_MASTER("Kulis Ustası", "100+ düzenli içerik.", BadgeCategory.CONTENT_COUNT, 100),

    // [ Etki Rozetleri ]
    LUMINOUS("Işık Saçan", "500+ Işık Tut etkileşimi.", BadgeCategory.LIGHT_COUNT, 500),
    SIGNATURE_MASTER("İmza Üstadı", "En çok imza (onay) alan yazar.", BadgeCategory.SIGNATURE_COUNT, 1000),
    STANDING_OVATION("Ayakta Alkışlanan", "Rekor etkileşimli biletli gösteri.", BadgeCategory.SPECIAL, 1),
    VERIFIED("Onay Rozeti", "100 takipçi.", BadgeCategory.FOLLOWER, 100),

    // [ Nadir Rozetler ]
    GOLDEN_TICKET("Altın Bilet", "En çok bilet satan ilk 10 yazar.", BadgeCategory.TICKET_SALES, 9999); // Top 10 kontrolü gerekecek

    private final String displayName;
    private final String description;
    private final BadgeCategory category;
    private final int requiredScore;
}
