package com.todoteg.cerrajeria.service;

import com.todoteg.cerrajeria.dto.*;
import com.todoteg.cerrajeria.exception.ResourceNotFoundException;
import com.todoteg.cerrajeria.model.*;
import com.todoteg.cerrajeria.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicationService {

    private final PublicationRepository publicationRepository;
    private final PublicationImageRepository imageRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;
    private final PublicationLikeRepository likeRepository;
    private final UserProfileRepository userRepository;
    private final FileStorageService fileStorageService;

    // === PUBLIC ===

    @Transactional(readOnly = true)
    public Page<PublicationDTO> getAllPublications(String search, Pageable pageable) {
        Page<Publication> publications;
        if (search != null && !search.isEmpty()) {
            publications = publicationRepository.search(search, pageable);
        } else {
            publications = publicationRepository.findAll(pageable);
        }
        return publications.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<PublicationDTO> getAllPublicationsList() {
        return publicationRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PublicationDTO getPublicationById(Long id) {
        Publication publication = findById(id);
        return toDTO(publication);
    }

    @Transactional
    public Map<String, Object> toggleLike(Long publicationId, Long userId) {
        Publication publication = findById(publicationId);
        Optional<PublicationLike> existingLike = likeRepository.findByPublicationIdAndUserId(publicationId, userId);

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            publication.setLikes(Math.max(0, publication.getLikes() - 1));
        } else {
            UserProfile user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            PublicationLike like = new PublicationLike();
            like.setUser(user);
            like.setPublication(publication);
            likeRepository.save(like);
            publication.setLikes(publication.getLikes() + 1);
        }
        publicationRepository.save(publication);

        Map<String, Object> result = new HashMap<>();
        result.put("likes", publication.getLikes());
        result.put("liked", existingLike.isEmpty());
        return result;
    }

    @Transactional
    public CommentDTO addComment(Long publicationId, Long userId, CommentCreateRequest request) {
        Publication publication = findById(publicationId);
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Comment comment = new Comment();
        comment.setAuthor(user.getName());
        comment.setText(request.getText());
        comment.setDate(LocalDate.now().toString());
        comment.setUser(user);
        comment.setPublication(publication);

        comment = commentRepository.save(comment);

        return new CommentDTO(comment.getId(), comment.getAuthor(), comment.getText(), comment.getDate(), user.getId());
    }

    // === ADMIN ===

    @Transactional
    public PublicationDTO createPublication(PublicationCreateRequest request, Long userId) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Publication publication = new Publication();
        publication.setUser(user);
        publication.setTitle(request.getTitle());
        publication.setDescription(request.getDescription());
        publication.setPrice(request.getPrice());
        publication.setOriginalPrice(request.getOriginalPrice());
        publication.setDiscount(request.getDiscount());
        publication.setWhatsappMessage(request.getWhatsappMessage() != null
                ? request.getWhatsappMessage()
                : "Hola! Me interesa: " + request.getTitle());
        publication.setIsNew(request.getIsNew() != null && request.getIsNew());
        publication.setLikes(0);

        // Tags
        if (request.getTags() != null) {
            Set<Tag> tags = resolveOrCreateTags(request.getTags());
            publication.setTags(tags);
        }

        publication = publicationRepository.save(publication);

        // Images
        if (request.getImages() != null) {
            for (String url : request.getImages()) {
                PublicationImage img = new PublicationImage();
                img.setImageUrl(url);
                img.setPublication(publication);
                imageRepository.save(img);
            }
        }

        return toDTO(publicationRepository.findById(publication.getId()).orElse(publication));
    }

    @Transactional
    public PublicationDTO updatePublication(Long id, PublicationUpdateRequest request) {
        Publication publication = findById(id);

        if (request.getTitle() != null) publication.setTitle(request.getTitle());
        if (request.getDescription() != null) publication.setDescription(request.getDescription());
        if (request.getPrice() != null) publication.setPrice(request.getPrice());
        if (request.getOriginalPrice() != null) publication.setOriginalPrice(request.getOriginalPrice());
        if (request.getDiscount() != null) publication.setDiscount(request.getDiscount());
        if (request.getWhatsappMessage() != null) publication.setWhatsappMessage(request.getWhatsappMessage());
        if (request.getIsNew() != null) publication.setIsNew(request.getIsNew());

        if (request.getTags() != null) {
            Set<Tag> tags = resolveOrCreateTags(request.getTags());
            publication.setTags(tags);
        }

        if (request.getImages() != null) {
            publication.getImages().clear();
            publicationRepository.save(publication);
            for (String url : request.getImages()) {
                PublicationImage img = new PublicationImage();
                img.setImageUrl(url);
                img.setPublication(publication);
                imageRepository.save(img);
            }
        }

        return toDTO(publicationRepository.save(publication));
    }

    @Transactional
    public void deletePublication(Long id) {
        Publication publication = findById(id);
        // Limpiar archivos locales de imágenes
        for (PublicationImage img : publication.getImages()) {
            fileStorageService.deleteIfLocal(img.getImageUrl());
        }
        publicationRepository.delete(publication);
    }

    @Transactional
    public void deleteComment(Long publicationId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado"));
        if (!comment.getPublication().getId().equals(publicationId)) {
            throw new IllegalArgumentException("El comentario no pertenece a esta promoción");
        }
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public List<TagDTO> getAllTags() {
        return tagRepository.findAll().stream()
                .map(t -> new TagDTO(t.getId(), t.getName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public TagDTO createTag(String name) {
        Optional<Tag> existing = tagRepository.findByNameIgnoreCase(name);
        if (existing.isPresent()) {
            Tag tag = existing.get();
            return new TagDTO(tag.getId(), tag.getName());
        }
        Tag tag = new Tag();
        tag.setName(name.toLowerCase());
        tag = tagRepository.save(tag);
        return new TagDTO(tag.getId(), tag.getName());
    }

    // === HELPERS ===

    private Publication findById(Long id) {
        return publicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada con ID: " + id));
    }

    private Set<Tag> resolveOrCreateTags(List<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        for (String name : tagNames) {
            String normalized = name.trim().toLowerCase();
            Tag tag = tagRepository.findByNameIgnoreCase(normalized)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(normalized);
                        return tagRepository.save(newTag);
                    });
            tags.add(tag);
        }
        return tags;
    }

    private PublicationDTO toDTO(Publication p) {
        PublicationDTO dto = new PublicationDTO();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setDescription(p.getDescription());
        dto.setPrice(p.getPrice());
        dto.setOriginalPrice(p.getOriginalPrice());
        dto.setDiscount(p.getDiscount());
        dto.setWhatsappMessage(p.getWhatsappMessage());
        dto.setIsNew(p.getIsNew());
        dto.setLikes(p.getLikes());
        dto.setAuthor(p.getUser() != null ? p.getUser().getName() : "AutoKeys");
        dto.setCreatedAt(p.getCreatedAt() != null ? p.getCreatedAt().toString() : null);
        dto.setUpdatedAt(p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : null);

        // Images
        dto.setImages(p.getImages().stream()
                .map(PublicationImage::getImageUrl)
                .collect(Collectors.toList()));

        // Tags
        dto.setTags(p.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList()));

        // Comments
        dto.setComments(p.getComments().stream()
                .map(c -> new CommentDTO(c.getId(), c.getAuthor(), c.getText(), c.getDate(),
                        c.getUser() != null ? c.getUser().getId() : null))
                .collect(Collectors.toList()));

        // LikedBy (user IDs)
        dto.setLikedBy(p.getLikesDetail().stream()
                .map(like -> String.valueOf(like.getUser().getId()))
                .collect(Collectors.toList()));

        return dto;
    }
}

