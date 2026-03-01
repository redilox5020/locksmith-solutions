package com.todoteg.cerrajeria.repository;

import com.todoteg.cerrajeria.model.PromotionImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionImageRepository extends JpaRepository<PromotionImage, Long> {
    List<PromotionImage> findByPromotionId(Long promotionId);
    void deleteByPromotionId(Long promotionId);
}
