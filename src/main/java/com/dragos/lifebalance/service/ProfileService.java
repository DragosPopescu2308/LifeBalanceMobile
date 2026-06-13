package com.dragos.lifebalance.service;

import com.dragos.lifebalance.dto.ProfileResponseDto;
import com.dragos.lifebalance.dto.ProfileUpdateRequestDto;
import com.dragos.lifebalance.entity.User;
import com.dragos.lifebalance.exceptions.NotFoundException;
import com.dragos.lifebalance.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

@Service
public class ProfileService {

    public record AvatarFile(Path path, String contentType, String filename) {}

    private final UserRepository userRepository;
    private final Path uploadRoot;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    public ProfileService(
            UserRepository userRepository,
            @Value("${app.upload-dir:uploads}") String uploadDir
    ) {
        this.userRepository = userRepository;
        this.uploadRoot = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    public ProfileResponseDto getProfile(User user) {
        return mapToProfileResponse(user);
    }

    @Transactional
    public ProfileResponseDto updateProfile(
            User user,
            ProfileUpdateRequestDto request
    ) {
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getAbout() != null) {
            user.setAbout(request.getAbout());
        }

        User savedUser = userRepository.save(user);

        return mapToProfileResponse(savedUser);
    }

    @Transactional
    public void uploadAvatar(User user, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only jpg/png/webp allowed");
        }

        Path directory = uploadRoot
                .resolve("avatars")
                .resolve(String.valueOf(user.getId()));

        try {
            Files.createDirectories(directory);
        } catch (Exception e) {
            throw new RuntimeException("Could not create upload directory", e);
        }

        String extension = switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> "";
        };

        Path target = directory
                .resolve("avatar" + extension)
                .normalize();

        deleteExistingAvatarFile(user);

        try {
            Files.copy(
                    file.getInputStream(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (Exception e) {
            throw new RuntimeException("Could not save file", e);
        }

        String relativePath = uploadRoot
                .relativize(target)
                .toString()
                .replace("\\", "/");

        user.setAvatarPath(relativePath);

        userRepository.save(user);
    }

    public AvatarFile getAvatarFile(User user) {
        if (user.getAvatarPath() == null || user.getAvatarPath().isBlank()) {
            return null;
        }

        Path path = uploadRoot
                .resolve(user.getAvatarPath())
                .normalize();

        String contentType = "application/octet-stream";

        try {
            String detectedContentType = Files.probeContentType(path);

            if (detectedContentType != null && !detectedContentType.isBlank()) {
                contentType = detectedContentType;
            }
        } catch (Exception ignored) {
        }

        return new AvatarFile(
                path,
                contentType,
                path.getFileName().toString()
        );
    }

    @Transactional
    public void deleteAvatar(User user) {
        deleteExistingAvatarFile(user);

        user.setAvatarPath(null);

        userRepository.save(user);
    }

    private void deleteExistingAvatarFile(User user) {
        if (user.getAvatarPath() == null || user.getAvatarPath().isBlank()) {
            return;
        }

        Path existingPath = uploadRoot
                .resolve(user.getAvatarPath())
                .normalize();

        try {
            Files.deleteIfExists(existingPath);
        } catch (Exception ignored) {
        }
    }

    private ProfileResponseDto mapToProfileResponse(User user) {
        return new ProfileResponseDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getAbout(),
                user.getAvatarPath() == null ? null : "/api/profile/avatar"
        );
    }
}