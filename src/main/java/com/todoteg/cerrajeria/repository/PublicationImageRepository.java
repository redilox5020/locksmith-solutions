package com.todoteg.cerrajeria.repository;

import com.todoteg.cerrajeria.model.PublicationImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicationImageRepository extends JpaRepository<PublicationImage, Long> {
    List<PublicationImage> findByPublicationId(Long publicationId);
    void deleteByPublicationId(Long publicationId);
}

