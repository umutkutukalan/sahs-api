package com.sahnesen.api.sahnesen.response;

import com.sahnesen.api.sahnesen.dto.UserDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterResponse {
    private UserDTO user; // Entity yerine Dto
    private String token;
}
