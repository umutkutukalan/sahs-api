package com.sahnesen.api.sahnesen.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sahnesen.api.sahnesen.entities.InviteCode;
import com.sahnesen.api.sahnesen.services.InviteCodeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/invite-codes")
@RequiredArgsConstructor
public class AdminInviteCodeController {

    private final InviteCodeService inviteCodeService;

    @PostMapping("/generate")
    public ResponseEntity<InviteCode> createInviteCode(
            @RequestParam(defaultValue = "Kurucu Sahne Davetiyesi") String description) {
        // İleride Security Context'ten admin ID'si çekilebilir
        InviteCode code = inviteCodeService.createPersonalInviteCode(description, null);
        return ResponseEntity.ok(code);
    }
}
