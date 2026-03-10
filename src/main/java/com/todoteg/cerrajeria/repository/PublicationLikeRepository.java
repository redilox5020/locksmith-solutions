package com.todoteg.cerrajeria.repository;

import com.todoteg.cerrajeria.model.PublicationLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PublicationLikeRepository extends JpaRepository<PublicationLike, Long> {
    Optional<PublicationLike> findByPublicationIdAndUserId(Long publicationId, Long userId);
    long countByPublicationId(Long publicationId);
}

