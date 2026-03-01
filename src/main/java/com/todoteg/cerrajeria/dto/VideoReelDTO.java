package com.todoteg.cerrajeria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoReelDTO {
    private Long id;
    private String videoUrl;
    private String thumbnailUrl;
    private String username;
    private Long promotionId;
    private String createdAt;
}
