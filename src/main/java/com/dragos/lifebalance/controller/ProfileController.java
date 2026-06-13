package com.dragos.lifebalance.controller;

import com.dragos.lifebalance.dto.ProfileResponseDto;
import com.dragos.lifebalance.dto.ProfileUpdateRequestDto;
import com.dragos.lifebalance.entity.User;
import com.dragos.lifebalance.exceptions.NotFoundException;
import com.dragos.lifebalance.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ProfileResponseDto getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return profileService.getProfile(user);
    }

    @PutMapping("/me")
    public ProfileResponseDto updateCurrentUser(
            Authentication authentication,
            @RequestBody @Valid ProfileUpdateRequestDto request
    ) {
        User user = (User) authentication.getPrincipal();
        return profileService.updateProfile(user, request);
    }

    @PostMapping("/avatar")
    public ResponseEntity<Void> uploadAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) {
        User user = (User) authentication.getPrincipal();
        profileService.uploadAvatar(user, file);

        return ResponseEntity.status(201).build();
    }

    @GetMapping("/avatar")
    public ResponseEntity<Resource> viewAvatar(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        ProfileService.AvatarFile avatarFile =
                profileService.getAvatarFile(user);

        if (avatarFile == null) {
            throw new NotFoundException("No avatar");
        }

        Resource resource = new FileSystemResource(avatarFile.path());

        if (!resource.exists()) {
            throw new NotFoundException("File not found");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(avatarFile.contentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + avatarFile.filename() + "\""
                )
                .body(resource);
    }

    @DeleteMapping("/avatar")
    public ResponseEntity<Void> deleteAvatar(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        profileService.deleteAvatar(user);

        return ResponseEntity.noContent().build();
    }
}