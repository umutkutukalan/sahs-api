package com.sahnesen.api.sahnesen.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtil {
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final Pattern EDGES = Pattern.compile("(^-+)|(-+$)");

    public static String generateSlug(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Başlık boş olamaz.");
        }

        // 1. Türkçe karakterleri standardize et (Ş->S, Ğ->G vb.)
        String result = normalizeTurkish(title);

        // 2. Küçük harfe çevir (İngilizce locale ile)
        result = result.toLowerCase(Locale.ENGLISH);

        // 3. Boşlukları tireye çevir
        result = WHITESPACE.matcher(result).replaceAll("-");

        // 4. Latin olmayan karakterleri sil (Harf, rakam ve tire dışındakiler)
        result = NONLATIN.matcher(result).replaceAll("");

        // 5. Başta ve sondaki tireleri temizle
        result = EDGES.matcher(result).replaceAll("");

        // 6. Birden fazla tireyi teke indir (Örn: bu--yazi -> bu-yazi)
        result = result.replaceAll("-+", "-");

        return result;
    }

    private static String normalizeTurkish(String text) {
        text = text.replace("ş", "s").replace("Ş", "S")
                .replace("ğ", "g").replace("Ğ", "G")
                .replace("ç", "c").replace("Ç", "C")
                .replace("ö", "o").replace("Ö", "O")
                .replace("ü", "u").replace("Ü", "U")
                .replace("ı", "i").replace("İ", "I");

        // Diğer aksanlı karakterleri (â, ê vb.) temizle
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
