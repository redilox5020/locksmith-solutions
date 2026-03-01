package com.todoteg.cerrajeria.repository;

import com.todoteg.cerrajeria.model.PromotionLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PromotionLikeRepository extends JpaRepository<PromotionLike, Long> {
    Optional<PromotionLike> findByPromotionIdAndUserId(Long promotionId, Long userId);
    long countByPromotionId(Long promotionId);
}
