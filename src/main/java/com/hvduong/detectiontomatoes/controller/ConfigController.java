package com.hvduong.detectiontomatoes.controller;

import com.hvduong.detectiontomatoes.model.entity.SystemConfig;
import com.hvduong.detectiontomatoes.model.entity.Batch;
import com.hvduong.detectiontomatoes.repository.SystemConfigRepository;
import com.hvduong.detectiontomatoes.repository.BatchRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {
    private final SystemConfigRepository systemConfigRepository;
    private final BatchRepository batchRepository;

    public ConfigController(SystemConfigRepository systemConfigRepository, BatchRepository batchRepository) {
        this.systemConfigRepository = systemConfigRepository;
        this.batchRepository = batchRepository;
    }

    @GetMapping("/current-batch")
    public Map<String, Object> getCurrentBatch() {
        SystemConfig config = systemConfigRepository.findById(1).orElse(null);
        if (config == null || config.getCurrentBatch() == null) {
            return Map.of();
        }
        Batch batch = config.getCurrentBatch();
        return Map.of("id", batch.getId(), "name", batch.getName());
    }

    @GetMapping("/batch/list")
    public List<Map<String, Object>> getBatchList() {
        return batchRepository.findAll().stream()
                .map(b -> Map.<String, Object>of("id", b.getId(), "name", b.getName()))
                .collect(Collectors.toList());
    }
}
