package com.todoteg.cerrajeria.controller;

import com.todoteg.cerrajeria.dto.*;
import com.todoteg.cerrajeria.service.FileStorageService;
import com.todoteg.cerrajeria.service.PublicationService;
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

    private final PublicationService publicationService;
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

    // ===== PUBLICATIONS MANAGEMENT =====

    @PostMapping("/publications")
    public ResponseEntity<PublicationDTO> createPublication(
            @Valid @RequestBody PublicationCreateRequest request,
            org.springframework.security.core.Authentication authentication) {
        com.todoteg.cerrajeria.model.UserProfile user = (com.todoteg.cerrajeria.model.UserProfile) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(publicationService.createPublication(request, user.getId()));
    }

    @PutMapping("/publications/{id}")
    public ResponseEntity<PublicationDTO> updatePublication(
            @PathVariable Long id,
            @RequestBody PublicationUpdateRequest request) {
        return ResponseEntity.ok(publicationService.updatePublication(id, request));
    }

    @DeleteMapping("/publications/{id}")
    public ResponseEntity<Void> deletePublication(@PathVariable Long id) {
        publicationService.deletePublication(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/publications/{publicationId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long publicationId,
            @PathVariable Long commentId) {
        publicationService.deleteComment(publicationId, commentId);
        return ResponseEntity.noContent().build();
    }

    // ===== TAGS MANAGEMENT =====

    @GetMapping("/tags")
    public ResponseEntity<List<TagDTO>> getAllTags() {
        return ResponseEntity.ok(publicationService.getAllTags());
    }

    @PostMapping("/tags")
    public ResponseEntity<TagDTO> createTag(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(publicationService.createTag(body.get("name")));
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

