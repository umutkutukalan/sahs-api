package com.sahnesen.api.sahnesen.request;

import lombok.Data;

@Data
public class UserUpdateRequest {
    // Temel Bilgiler
    private String name;
    private String surname;
    private String bio;
    private String motto;
    private String username; // Slug otomatik güncellenmeli

    // Görsel Alanlar
    private String profileImg;
    private String coverImg;

    // Konum
    private String city;
    private String district;

    // Alt Modüller (DTO olarak alıyoruz)
    private ProfessionalUpdate professional;
    private PreferencesUpdate preferences;

    @Data
    public static class ProfessionalUpdate {
        private String job;
        private String availableFor;
        private String portfolioUrl;
        private String skills;
    }

    @Data
    public static class PreferencesUpdate {
        private String theme; // String alıp Enum'a çevireceğiz
        private Boolean isPrivate;
        private Boolean allowDirectMessages;
        private String language;
    }

}
