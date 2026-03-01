package com.todoteg.cerrajeria.service;

import com.todoteg.cerrajeria.dto.LoginRequest;
import com.todoteg.cerrajeria.dto.TokenResponse;
import com.todoteg.cerrajeria.model.UserProfile;
import com.todoteg.cerrajeria.repository.UserProfileRepository;
import com.todoteg.cerrajeria.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserProfileRepository userRepository;
    private final JwtUtil jwtUtil;

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserProfile user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        String accessToken = jwtUtil.generateAccessToken(user, user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user, user.getId());

        TokenResponse.UserDTO userDTO = new TokenResponse.UserDTO(
                user.getId(), user.getName(), user.getEmail(),
                user.getIsStaff(), user.getIsSuperuser()
        );

        return new TokenResponse(accessToken, refreshToken, userDTO);
    }

    public TokenResponse refresh(String refreshToken) {
        String email = jwtUtil.extractUsername(refreshToken);
        UserProfile user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        if (!jwtUtil.validateToken(refreshToken, user)) {
            throw new RuntimeException("Refresh token inválido");
        }

        String newAccessToken = jwtUtil.generateAccessToken(user, user.getId());

        TokenResponse.UserDTO userDTO = new TokenResponse.UserDTO(
                user.getId(), user.getName(), user.getEmail(),
                user.getIsStaff(), user.getIsSuperuser()
        );

        return new TokenResponse(newAccessToken, refreshToken, userDTO);
    }
}
