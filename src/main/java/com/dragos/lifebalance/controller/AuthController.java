package com.dragos.lifebalance.controller;

import com.dragos.lifebalance.dto.AuthResponseDto;
import com.dragos.lifebalance.dto.LoginRequestDto;
import com.dragos.lifebalance.dto.RegisterRequestDto;
import com.dragos.lifebalance.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@RequestBody @Valid RegisterRequestDto request){
        AuthResponseDto response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid LoginRequestDto request){
        AuthResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
