package com.todoteg.cerrajeria.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "ck_promotion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String price;

    @Column(name = "original_price")
    private String originalPrice;

    private String discount;

    @Column(name = "whatsapp_message")
    private String whatsappMessage;

    @Column(name = "is_new")
    private Boolean isNew = false;

    @Column(nullable = false)
    private Integer likes = 0;

    @Column(name = "price_value")
    private Double priceValue;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PromotionImage> images = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "ck_promotion_tag",
        joinColumns = @JoinColumn(name = "promotion_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PromotionLike> likesDetail = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (slug == null || slug.isEmpty()) {
            slug = title.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "")
                    + "-" + System.currentTimeMillis();
        }
        computePriceValue();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        computePriceValue();
    }

    private void computePriceValue() {
        if (price != null) {
            try {
                String cleaned = price.replaceAll("[^0-9]", "");
                priceValue = cleaned.isEmpty() ? 0.0 : Double.parseDouble(cleaned);
            } catch (NumberFormatException e) {
                priceValue = 0.0;
            }
        }
    }
}
