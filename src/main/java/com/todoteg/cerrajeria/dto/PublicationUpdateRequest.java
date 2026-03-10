package com.todoteg.cerrajeria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicationUpdateRequest {
    private String title;
    private String description;
    private List<String> images;
    private String price;
    private String originalPrice;
    private String discount;
    private String whatsappMessage;
    private Boolean isNew;
    private List<String> tags;
}

