package com.todoteg.cerrajeria.service;

import com.todoteg.cerrajeria.dto.VideoCreateRequest;
import com.todoteg.cerrajeria.dto.VideoReelDTO;
import com.todoteg.cerrajeria.dto.VideoUpdateRequest;
import com.todoteg.cerrajeria.exception.ResourceNotFoundException;
import com.todoteg.cerrajeria.model.Promotion;
import com.todoteg.cerrajeria.model.VideoReel;
import com.todoteg.cerrajeria.repository.PromotionRepository;
import com.todoteg.cerrajeria.repository.VideoReelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoReelRepository videoRepository;
    private final PromotionRepository promotionRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<VideoReelDTO> getAllVideos() {
        return videoRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public VideoReelDTO createVideo(VideoCreateRequest request) {
        Promotion promotion = promotionRepository.findById(request.getPromotionId())
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada"));

        VideoReel video = new VideoReel();
        video.setVideoUrl(request.getVideoUrl());
        video.setThumbnailUrl(request.getThumbnailUrl());
        video.setUsername(request.getUsername());
        video.setPromotion(promotion);

        video = videoRepository.save(video);
        return toDTO(video);
    }

    @Transactional
    public VideoReelDTO updateVideo(Long id, VideoUpdateRequest request) {
        VideoReel video = findById(id);

        if (request.getVideoUrl() != null) video.setVideoUrl(request.getVideoUrl());
        if (request.getThumbnailUrl() != null) video.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getUsername() != null) video.setUsername(request.getUsername());

        if (request.getPromotionId() != null) {
            Promotion promotion = promotionRepository.findById(request.getPromotionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada"));
            video.setPromotion(promotion);
        }

        video = videoRepository.save(video);
        return toDTO(video);
    }

    @Transactional
    public void deleteVideo(Long id) {
        VideoReel video = findById(id);
        // Limpiar archivos locales de thumbnail y video
        fileStorageService.deleteIfLocal(video.getThumbnailUrl());
        fileStorageService.deleteIfLocal(video.getVideoUrl());
        videoRepository.delete(video);
    }

    private VideoReel findById(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video no encontrado con ID: " + id));
    }

    private VideoReelDTO toDTO(VideoReel v) {
        return new VideoReelDTO(
                v.getId(),
                v.getVideoUrl(),
                v.getThumbnailUrl(),
                v.getUsername(),
                v.getPromotion().getId(),
                v.getCreatedAt() != null ? v.getCreatedAt().toString() : null
        );
    }
}
