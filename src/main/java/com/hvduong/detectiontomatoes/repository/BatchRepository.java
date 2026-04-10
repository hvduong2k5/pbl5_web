package com.hvduong.detectiontomatoes.repository;

import com.hvduong.detectiontomatoes.model.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchRepository extends JpaRepository<Batch, Integer> {
}

