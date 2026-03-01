package com.todoteg.cerrajeria.controller;

import com.todoteg.cerrajeria.dto.*;
import com.todoteg.cerrajeria.service.FileStorageService;
import com.todoteg.cerrajeria.service.PromotionService;
import com.todoteg.cerrajeria.service.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final PromotionService promotionService;
    private final VideoService videoService;
    private final FileStorageService fileStorageService;

    // ===== FILE UPLOAD =====

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        String filename = fileStorageService.store(file);
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(filename)
                .toUriString();
        return ResponseEntity.ok(Map.of("url", url));
    }

    // ===== PROMOTIONS MANAGEMENT =====

    @PostMapping("/promotions")
    public ResponseEntity<PromotionDTO> createPromotion(@Valid @RequestBody PromotionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promotionService.createPromotion(request));
    }

    @PutMapping("/promotions/{id}")
    public ResponseEntity<PromotionDTO> updatePromotion(
            @PathVariable Long id,
            @RequestBody PromotionUpdateRequest request) {
        return ResponseEntity.ok(promotionService.updatePromotion(id, request));
    }

    @DeleteMapping("/promotions/{id}")
    public ResponseEntity<Void> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/promotions/{promotionId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long promotionId,
            @PathVariable Long commentId) {
        promotionService.deleteComment(promotionId, commentId);
        return ResponseEntity.noContent().build();
    }

    // ===== TAGS MANAGEMENT =====

    @GetMapping("/tags")
    public ResponseEntity<List<TagDTO>> getAllTags() {
        return ResponseEntity.ok(promotionService.getAllTags());
    }

    @PostMapping("/tags")
    public ResponseEntity<TagDTO> createTag(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(promotionService.createTag(body.get("name")));
    }

    // ===== VIDEOS MANAGEMENT =====

    @GetMapping("/videos")
    public ResponseEntity<List<VideoReelDTO>> getAllVideos() {
        return ResponseEntity.ok(videoService.getAllVideos());
    }

    @PostMapping("/videos")
    public ResponseEntity<VideoReelDTO> createVideo(@Valid @RequestBody VideoCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(videoService.createVideo(request));
    }

    @PutMapping("/videos/{id}")
    public ResponseEntity<VideoReelDTO> updateVideo(
            @PathVariable Long id,
            @RequestBody VideoUpdateRequest request) {
        return ResponseEntity.ok(videoService.updateVideo(id, request));
    }

    @DeleteMapping("/videos/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
        videoService.deleteVideo(id);
        return ResponseEntity.noContent().build();
    }
}
