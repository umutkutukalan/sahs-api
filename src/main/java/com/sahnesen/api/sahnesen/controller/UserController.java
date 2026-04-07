package com.sahnesen.api.sahnesen.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sahnesen.api.sahnesen.dto.UserDTO;
import com.sahnesen.api.sahnesen.request.UserUpdateRequest;
import com.sahnesen.api.sahnesen.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Kendi profilini güncelle
    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateMyProfile(@Valid @RequestBody UserUpdateRequest updateRequest) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        UserDTO updatedUser = userService.updateMyProfile(email, updateRequest);
        return ResponseEntity.ok(updatedUser);
    }
}