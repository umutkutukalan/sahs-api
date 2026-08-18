package com.sahnesen.api.sahnesen.util;

import java.security.SecureRandom;

public class InviteCodeGenerator {

    // Okunması kolay karakter kümesi
    private static final String CHARACTERS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateCode(String prefix, int length) {
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString(); // Örn: SAHNE-K8X9P2
    }
}
