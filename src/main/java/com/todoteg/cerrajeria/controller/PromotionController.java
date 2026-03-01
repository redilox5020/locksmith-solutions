package com.todoteg.cerrajeria.controller;

import com.todoteg.cerrajeria.dto.*;
import com.todoteg.cerrajeria.model.UserProfile;
import com.todoteg.cerrajeria.service.PromotionService;
import com.todoteg.cerrajeria.service.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;
    private final VideoService videoService;

    // === PROMOTIONS (público) ===

    @GetMapping("/promotions")
    public ResponseEntity<List<PromotionDTO>> getAllPromotions() {
        return ResponseEntity.ok(promotionService.getAllPromotionsList());
    }

    @GetMapping("/promotions/paged")
    public ResponseEntity<Page<PromotionDTO>> getPromotionsPaged(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "page_size", defaultValue = "6") int pageSize,
            @RequestParam(name = "sort_by", defaultValue = "date") String sortBy) {
        Sort sort = switch (sortBy) {
            case "likes" -> Sort.by(Sort.Direction.DESC, "likes");
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "priceValue");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "priceValue");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        return ResponseEntity.ok(promotionService.getAllPromotions(search, pageable));
    }

    @GetMapping("/promotions/{id}")
    public ResponseEntity<PromotionDTO> getPromotion(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.getPromotionById(id));
    }

    @PostMapping("/promotions/{id}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long id) {
        UserProfile user = (UserProfile) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(promotionService.toggleLike(id, user.getId()));
    }

    @PostMapping("/promotions/{id}/comments")
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentCreateRequest request) {
        UserProfile user = (UserProfile) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(promotionService.addComment(id, user.getId(), request));
    }

    // === TAGS (público) ===

    @GetMapping("/tags")
    public ResponseEntity<List<TagDTO>> getAllTags() {
        return ResponseEntity.ok(promotionService.getAllTags());
    }

    // === VIDEOS (público) ===

    @GetMapping("/videos")
    public ResponseEntity<List<VideoReelDTO>> getAllVideos() {
        return ResponseEntity.ok(videoService.getAllVideos());
    }
}
