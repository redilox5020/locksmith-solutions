package com.todoteg.cerrajeria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicationCreateRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotEmpty
    private List<String> images;

    private String price;

    private String originalPrice;
    private String discount;
    private String whatsappMessage;
    private Boolean isNew;
    private List<String> tags;
}

