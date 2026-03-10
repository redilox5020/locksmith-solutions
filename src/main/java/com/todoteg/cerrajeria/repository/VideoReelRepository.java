package com.todoteg.cerrajeria.repository;

import com.todoteg.cerrajeria.model.VideoReel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoReelRepository extends JpaRepository<VideoReel, Long> {
    List<VideoReel> findAllByOrderByCreatedAtDesc();
    List<VideoReel> findByPublicationId(Long publicationId);
}

