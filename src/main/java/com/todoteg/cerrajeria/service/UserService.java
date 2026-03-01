package com.todoteg.cerrajeria.service;

import com.todoteg.cerrajeria.dto.UserCreateRequest;
import com.todoteg.cerrajeria.model.UserProfile;
import com.todoteg.cerrajeria.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserProfileRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
    }

    @Transactional
    public UserProfile createUser(UserCreateRequest request) {
        if (Boolean.TRUE.equals(userRepository.existsByEmail(request.getEmail()))) {
            throw new RuntimeException("Ya existe una cuenta con este correo electrónico");
        }

        UserProfile user = new UserProfile();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setIsActive(true);
        user.setIsStaff(false);
        user.setIsSuperuser(false);
        user.setCreatedAt(LocalDateTime.now().toString());

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserProfile findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}
