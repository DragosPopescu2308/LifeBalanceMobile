package com.dragos.lifebalance.controller;

import com.dragos.lifebalance.dto.ProfileResponseDto;
import com.dragos.lifebalance.dto.ProfileUpdateRequestDto;
import com.dragos.lifebalance.entity.User;
import com.dragos.lifebalance.service.ProfileService;
import jakarta.validation.Valid;
import jakarta.validation.executable.ValidateOnExecution;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService){
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ProfileResponseDto getCurrentUser(
            Authentication authentication
    ) {

        User user = (User) authentication.getPrincipal();
        return profileService.getProfile(user);
    }

    @PutMapping("/me")
    public ProfileResponseDto updateCurrentUser(Authentication authentication, @RequestBody @Valid ProfileUpdateRequestDto request){
        User user = (User) authentication.getPrincipal();
        return profileService.updateProfile(user, request);
    }
}