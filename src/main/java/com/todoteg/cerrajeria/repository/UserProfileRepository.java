package com.todoteg.cerrajeria.repository;

import com.todoteg.cerrajeria.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByEmail(String email);
    Boolean existsByEmail(String email);
}
