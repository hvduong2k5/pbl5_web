package com.hvduong.detectiontomatoes.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FruitEventDTO {
    private String event;
    private String id;
    private String label;
    
    @JsonAlias("type")
    private String sortedType;
    
    private String status;
    
    @JsonAlias("image_url")
    private String imageUrl;
    
    private Double confidence;
    private String createdAt;
    private String classifiedAt;
    private String sortedAt;
}
