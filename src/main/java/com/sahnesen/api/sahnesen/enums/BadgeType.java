package com.sahnesen.api.sahnesen.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BadgeType {
    // [ Kariyer ve Genel İçerik Rozetleri ]
    FIRST_REHEARSAL("İlk Selam", "İlk provasını paylaşanlara verilir.", BadgeCategory.CONTENT_COUNT, 1),
    STAGE_DUST("Sahne Tozu", "10 içerik. Artık buralısın, ücretli sahne yetkisi kazandın!", BadgeCategory.CONTENT_COUNT,
            10),
    MASTER_ACTOR("Usta Oyuncu", "50 içerik ve yüksek etkileşim.", BadgeCategory.CONTENT_COUNT, 50),
    MAIN_STAGE("Ana Sahne Oyuncusu", "50 içerik ve 5 biletli gösteri.", BadgeCategory.SPECIAL, 55),
    BACKSTAGE_MASTER("Kulis Ustası", "100+ düzenli içerik.", BadgeCategory.CONTENT_COUNT, 100),

    // [ Mod Bazlı Uzmanlık Rozetleri (POST_TYPE_EXPERT) ]
    SAHNE_EXPERT("Sahne Tozu Yutan", "20 adet Sahne içeriği üretti.", BadgeCategory.POST_TYPE_EXPERT, 20),
    MONOLOG_EXPERT("İç Ses Ustası", "20 adet Monolog içeriği üretti.", BadgeCategory.POST_TYPE_EXPERT, 20),
    YANYANA_EXPERT("Kahve Dostu", "20 adet Yan Yana içeriği üretti.", BadgeCategory.POST_TYPE_EXPERT, 20),
    TERSYUZ_EXPERT("Ayna Efendisi", "20 adet Tersyüz içeriği üretti.", BadgeCategory.POST_TYPE_EXPERT, 20),

    // [ Mod Bazlı Etkileşim Rozetleri (ENGAGEMENT_BY_TYPE) ]
    OVATION_SAHNE("Coşkulu Alkış", "Sahne modunda 100 adet alkış (SHINE_SAHNE) topladı.",
            BadgeCategory.ENGAGEMENT_BY_TYPE, 100),
    FEATHER_MONOLOG("Hafifleten Tüy", "Monolog modunda 100 adet tüy (SHINE_MONOLOG) topladı.",
            BadgeCategory.ENGAGEMENT_BY_TYPE, 100),
    COFFEE_YANYANA("Bol Köpüklü", "Yan Yana modunda 100 adet kahve (SHINE_YANYANA) topladı.",
            BadgeCategory.ENGAGEMENT_BY_TYPE, 100),
    SMILE_TERSYUZ("Bulaşıcı Gülüş", "Tersyüz modunda 100 adet gülümseme (SHINE_TERSYUZ) topladı.",
            BadgeCategory.ENGAGEMENT_BY_TYPE, 100),

    // [ Genel Etki ve Topluluk Rozetleri ]
    LUMINOUS("Işık Saçan", "500+ Toplam etkileşim.", BadgeCategory.ENGAGEMENT_BY_TYPE, 500),
    SIGNATURE_MASTER("İmza Üstadı", "En çok imza (onay) alan yazar.", BadgeCategory.SIGNATURE_COUNT, 1000),
    STANDING_OVATION("Ayakta Alkışlanan", "Rekor etkileşimli biletli gösteri.", BadgeCategory.SPECIAL, 1),
    VERIFIED("Onay Rozeti", "100 takipçi.", BadgeCategory.FOLLOWER, 100),

    // [ Nadir Rozetler ]
    GOLDEN_TICKET("Altın Bilet", "En çok bilet satan ilk 10 yazar.", BadgeCategory.TICKET_SALES, 9999);

    private final String displayName;
    private final String description;
    private final BadgeCategory category;
    private final int requiredScore;
}