package com.sahnesen.api.sahnesen.services;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sahnesen.api.sahnesen.entities.InviteCode;
import com.sahnesen.api.sahnesen.repository.InviteCodeRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InviteCodeService {

    private final InviteCodeRepository inviteCodeRepository;

    @Transactional
    public InviteCode validateAndUseCode(String rawCode) {
        if (rawCode == null || rawCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Kurucu Sahne davet kodu gereklidir.");
        }

        String formattedCode = rawCode.trim().toUpperCase();

        InviteCode inviteCode = inviteCodeRepository.findByCode(formattedCode)
                .orElseThrow(() -> new EntityNotFoundException("Geçersiz davet kodu."));

        if (!inviteCode.getIsActive()) {
            throw new IllegalStateException("Bu davet kodu artık aktif değil.");
        }

        if (inviteCode.getExpiresAt() != null && inviteCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Bu davet kodunun süresi dolmuş.");
        }

        if (inviteCode.getUsedCount() >= inviteCode.getMaxUses()) {
            throw new IllegalStateException("Bu davet kodunun kullanım limiti dolmuştur.");
        }

        // Kullanım sayısını 1 artırıyoruz
        inviteCode.setUsedCount(inviteCode.getUsedCount() + 1);
        return inviteCodeRepository.save(inviteCode);
    }
}
