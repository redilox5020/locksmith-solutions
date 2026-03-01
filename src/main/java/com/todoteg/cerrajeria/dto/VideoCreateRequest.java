package com.todoteg.cerrajeria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoCreateRequest {
    private String videoUrl;

    @NotBlank
    private String thumbnailUrl;

    @NotBlank
    private String username;

    @NotNull
    private Long promotionId;
}
