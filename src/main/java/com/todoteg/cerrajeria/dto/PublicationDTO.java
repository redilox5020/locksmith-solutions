package com.todoteg.cerrajeria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicationDTO {
    private Long id;
    private String title;
    private String description;
    private List<String> images;
    private String price;
    private String originalPrice;
    private String discount;
    private String whatsappMessage;
    private Boolean isNew;
    private Integer likes;
    private String author;
    private List<String> likedBy;
    private List<CommentDTO> comments;
    private List<String> tags;
    private String createdAt;
    private String updatedAt;
}

