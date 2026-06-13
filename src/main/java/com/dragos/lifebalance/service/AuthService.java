package com.dragos.lifebalance.service;

import com.dragos.lifebalance.dto.AuthResponseDto;
import com.dragos.lifebalance.dto.LoginRequestDto;
import com.dragos.lifebalance.dto.RegisterRequestDto;
import com.dragos.lifebalance.entity.User;
import com.dragos.lifebalance.exceptions.NotFoundException;
import com.dragos.lifebalance.repository.UserRepository;
import com.dragos.lifebalance.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponseDto register(RegisterRequestDto registerRequestDto){
        if(userRepository.existsByEmail(registerRequestDto.getEmail())){
            throw new RuntimeException("Email already exists");
        }

        User user = new User();

        user.setFullName(registerRequestDto.getFullName());
        user.setEmail(registerRequestDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequestDto.getPassword()));
        user.setAbout(registerRequestDto.getAbout());
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser.getEmail());

        return new AuthResponseDto(
                token,
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getAbout(),
                savedUser.getAvatarPath()
        );
    }

    public AuthResponseDto login(LoginRequestDto request){
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new RuntimeException("Invalid email or password"));

        boolean verify = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if(!verify){
            throw new NotFoundException("Invalid email or password");
        }
        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponseDto(
                token,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getAbout(),
                user.getAvatarPath()
        );
    }

}
