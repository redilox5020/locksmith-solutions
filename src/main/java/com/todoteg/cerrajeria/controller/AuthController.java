package com.todoteg.cerrajeria.controller;

import com.todoteg.cerrajeria.dto.*;
import com.todoteg.cerrajeria.service.AuthService;
import com.todoteg.cerrajeria.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefresh()));
    }

    @PostMapping("/user/")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody UserCreateRequest request) {
        userService.createUser(request);
        LoginRequest loginRequest = new LoginRequest(request.getEmail(), request.getPassword());
        TokenResponse tokens = authService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(tokens);
    }
}
