package com.hvduong.detectiontomatoes.repository;

import com.hvduong.detectiontomatoes.model.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemConfigRepository extends JpaRepository<SystemConfig, Integer> {
}

