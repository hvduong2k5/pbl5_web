package com.hvduong.detectiontomatoes.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "system_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfig {
    @Id
    private Integer id = 1;

    @OneToOne
    @JoinColumn(name = "current_batch_id", referencedColumnName = "id")
    private Batch currentBatch;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

