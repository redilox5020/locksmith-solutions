package com.todoteg.cerrajeria.repository;

import com.todoteg.cerrajeria.model.Publication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PublicationRepository extends JpaRepository<Publication, Long> {

    Optional<Publication> findBySlug(String slug);

    @Query("SELECT p FROM Publication p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "EXISTS (SELECT t FROM p.tags t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Publication> search(@Param("search") String search, Pageable pageable);
}

