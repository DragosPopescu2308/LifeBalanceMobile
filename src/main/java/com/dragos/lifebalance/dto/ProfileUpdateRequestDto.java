package com.dragos.lifebalance.dto;


import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequestDto {
    @Size(max = 100, message = "Full name must be at most 100 characters")
    private String fullname;

    @Size(max = 100, message = "About must be at most 100 characters")
    private String about;

    @Size(max = 500, message = "Avatar path must be at most 500 characters")
    private String avatarPath;

}
