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
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionImageRepository imageRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;
    private final PromotionLikeRepository likeRepository;
    private final UserProfileRepository userRepository;
    private final FileStorageService fileStorageService;

    // === PUBLIC ===

    @Transactional(readOnly = true)
    public Page<PromotionDTO> getAllPromotions(String search, Pageable pageable) {
        Page<Promotion> promotions;
        if (search != null && !search.isEmpty()) {
            promotions = promotionRepository.search(search, pageable);
        } else {
            promotions = promotionRepository.findAll(pageable);
        }
        return promotions.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<PromotionDTO> getAllPromotionsList() {
        return promotionRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PromotionDTO getPromotionById(Long id) {
        Promotion promotion = findById(id);
        return toDTO(promotion);
    }

    @Transactional
    public Map<String, Object> toggleLike(Long promotionId, Long userId) {
        Promotion promotion = findById(promotionId);
        Optional<PromotionLike> existingLike = likeRepository.findByPromotionIdAndUserId(promotionId, userId);

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            promotion.setLikes(Math.max(0, promotion.getLikes() - 1));
        } else {
            UserProfile user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            PromotionLike like = new PromotionLike();
            like.setUser(user);
            like.setPromotion(promotion);
            likeRepository.save(like);
            promotion.setLikes(promotion.getLikes() + 1);
        }
        promotionRepository.save(promotion);

        Map<String, Object> result = new HashMap<>();
        result.put("likes", promotion.getLikes());
        result.put("liked", existingLike.isEmpty());
        return result;
    }

    @Transactional
    public CommentDTO addComment(Long promotionId, Long userId, CommentCreateRequest request) {
        Promotion promotion = findById(promotionId);
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Comment comment = new Comment();
        comment.setAuthor(user.getName());
        comment.setText(request.getText());
        comment.setDate(LocalDate.now().toString());
        comment.setUser(user);
        comment.setPromotion(promotion);

        comment = commentRepository.save(comment);

        return new CommentDTO(comment.getId(), comment.getAuthor(), comment.getText(), comment.getDate(), user.getId());
    }

    // === ADMIN ===

    @Transactional
    public PromotionDTO createPromotion(PromotionCreateRequest request) {
        Promotion promotion = new Promotion();
        promotion.setTitle(request.getTitle());
        promotion.setDescription(request.getDescription());
        promotion.setPrice(request.getPrice());
        promotion.setOriginalPrice(request.getOriginalPrice());
        promotion.setDiscount(request.getDiscount());
        promotion.setWhatsappMessage(request.getWhatsappMessage() != null
                ? request.getWhatsappMessage()
                : "Hola! Me interesa: " + request.getTitle());
        promotion.setIsNew(request.getIsNew() != null && request.getIsNew());
        promotion.setLikes(0);

        // Tags
        if (request.getTags() != null) {
            Set<Tag> tags = resolveOrCreateTags(request.getTags());
            promotion.setTags(tags);
        }

        promotion = promotionRepository.save(promotion);

        // Images
        if (request.getImages() != null) {
            for (String url : request.getImages()) {
                PromotionImage img = new PromotionImage();
                img.setImageUrl(url);
                img.setPromotion(promotion);
                imageRepository.save(img);
            }
        }

        return toDTO(promotionRepository.findById(promotion.getId()).orElse(promotion));
    }

    @Transactional
    public PromotionDTO updatePromotion(Long id, PromotionUpdateRequest request) {
        Promotion promotion = findById(id);

        if (request.getTitle() != null) promotion.setTitle(request.getTitle());
        if (request.getDescription() != null) promotion.setDescription(request.getDescription());
        if (request.getPrice() != null) promotion.setPrice(request.getPrice());
        if (request.getOriginalPrice() != null) promotion.setOriginalPrice(request.getOriginalPrice());
        if (request.getDiscount() != null) promotion.setDiscount(request.getDiscount());
        if (request.getWhatsappMessage() != null) promotion.setWhatsappMessage(request.getWhatsappMessage());
        if (request.getIsNew() != null) promotion.setIsNew(request.getIsNew());

        if (request.getTags() != null) {
            Set<Tag> tags = resolveOrCreateTags(request.getTags());
            promotion.setTags(tags);
        }

        if (request.getImages() != null) {
            promotion.getImages().clear();
            promotionRepository.save(promotion);
            for (String url : request.getImages()) {
                PromotionImage img = new PromotionImage();
                img.setImageUrl(url);
                img.setPromotion(promotion);
                imageRepository.save(img);
            }
        }

        return toDTO(promotionRepository.save(promotion));
    }

    @Transactional
    public void deletePromotion(Long id) {
        Promotion promotion = findById(id);
        // Limpiar archivos locales de imágenes
        for (PromotionImage img : promotion.getImages()) {
            fileStorageService.deleteIfLocal(img.getImageUrl());
        }
        promotionRepository.delete(promotion);
    }

    @Transactional
    public void deleteComment(Long promotionId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado"));
        if (!comment.getPromotion().getId().equals(promotionId)) {
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

    private Promotion findById(Long id) {
        return promotionRepository.findById(id)
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

    private PromotionDTO toDTO(Promotion p) {
        PromotionDTO dto = new PromotionDTO();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setDescription(p.getDescription());
        dto.setPrice(p.getPrice());
        dto.setOriginalPrice(p.getOriginalPrice());
        dto.setDiscount(p.getDiscount());
        dto.setWhatsappMessage(p.getWhatsappMessage());
        dto.setIsNew(p.getIsNew());
        dto.setLikes(p.getLikes());
        dto.setCreatedAt(p.getCreatedAt() != null ? p.getCreatedAt().toString() : null);
        dto.setUpdatedAt(p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : null);

        // Images
        dto.setImages(p.getImages().stream()
                .map(PromotionImage::getImageUrl)
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
