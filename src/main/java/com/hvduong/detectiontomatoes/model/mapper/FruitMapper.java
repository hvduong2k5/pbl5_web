package com.hvduong.detectiontomatoes.model.mapper;

import com.hvduong.detectiontomatoes.model.entity.Fruit;
import com.hvduong.detectiontomatoes.model.dto.FruitEventDTO;
import com.hvduong.detectiontomatoes.model.dto.AiResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FruitMapper {

    @Value("${minio.url}")
    private String minioUrl;

    public Fruit toEntity(FruitEventDTO dto) {
        return Fruit.builder()
                .id(dto.getId())
                .label(dto.getLabel())
                .confidence(dto.getConfidence())
                .build();
    }

    public FruitEventDTO toEventDTO(Fruit fruit, String event) {
        String imageUrl = fruit.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                String baseUrl = minioUrl;
                if (baseUrl != null) {
                    if (baseUrl.endsWith("/")) {
                        baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                    }
                    if (imageUrl.startsWith("/")) {
                        imageUrl = baseUrl + imageUrl;
                    } else {
                        imageUrl = baseUrl + "/" + imageUrl;
                    }
                }
            }
        }
        String label = fruit.getLabel();
        if ("rotten".equalsIgnoreCase(label)) {
            label = "reject";
        }
        return FruitEventDTO.builder()
                .event(event)
                .id(fruit.getId())
                .label(label)
                .type(fruit.getSortedType())
                .image_url(imageUrl)
                .confidence(fruit.getConfidence())
                .build();
    }

    public Fruit updateFromAiResponse(Fruit fruit, AiResponseDTO dto) {
        fruit.setLabel(dto.getResult());
        String imageUrl = dto.getImageUrl();
        if (imageUrl != null) {
            int index = imageUrl.indexOf("/", imageUrl.indexOf("//") + 2);
            if (index != -1) {
                imageUrl = imageUrl.substring(index);
            }
        }
        fruit.setImageUrl(imageUrl);
        if (dto.getConfidence() != null) {
            fruit.setConfidence(dto.getConfidence());
        }
        return fruit;
    }
}
