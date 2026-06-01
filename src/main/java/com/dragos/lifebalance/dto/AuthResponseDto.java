package com.dragos.lifebalance.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDto {
    private String token;
    private Integer userId;
    private String fullName;
    private String email;
    private String about;
    private String avatarUrl;
}
