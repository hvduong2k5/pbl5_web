package com.hvduong.detectiontomatoes.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiResponseDTO {
    private String id;
    private String result; // RIPE/UNRIPE/...
    private String imageUrl;
    private Double confidence;
}
