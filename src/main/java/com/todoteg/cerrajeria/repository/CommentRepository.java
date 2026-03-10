package com.todoteg.cerrajeria.repository;

import com.todoteg.cerrajeria.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPublicationIdOrderByIdDesc(Long publicationId);
}

