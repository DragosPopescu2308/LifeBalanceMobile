package com.dragos.lifebalance.service;

import com.dragos.lifebalance.dto.ProfileResponseDto;
import com.dragos.lifebalance.dto.ProfileUpdateRequestDto;
import com.dragos.lifebalance.entity.User;
import com.dragos.lifebalance.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {
    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ProfileResponseDto getProfile(User user){
        return mapToProfileResponse(user);
    }

    public ProfileResponseDto updateProfile(User user, ProfileUpdateRequestDto request){
        if(request.getFullname() != null){
            user.setFullName(request.getFullname());
        }

        if (request.getAbout() != null) {
            user.setAbout(request.getAbout());
        }

        if (request.getAvatarPath() != null) {
            user.setAvatarPath(request.getAvatarPath());
        }

        User savedUser = userRepository.save(user);

        return mapToProfileResponse(savedUser);
    }

    private ProfileResponseDto mapToProfileResponse(User user){
        return new ProfileResponseDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getAbout(),
                user.getAvatarPath()
        );
    }
}
