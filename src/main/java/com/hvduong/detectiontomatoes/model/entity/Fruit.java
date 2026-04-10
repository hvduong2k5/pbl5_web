package com.hvduong.detectiontomatoes.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "fruits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fruit {
    @Id
    private String id;
    private String status;
    private String label;
    @Column(name = "sorted_type")
    private String sortedType;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "classified_at")
    private LocalDateTime classifiedAt;
    @Column(name = "sorted_at")
    private LocalDateTime sortedAt;
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    @JsonIgnore
    private Batch batch;

    private Double confidence;
}