package com.todoteg.cerrajeria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoUpdateRequest {
    private String videoUrl;
    private String thumbnailUrl;
    private String username;
    private Long publicationId;
}

