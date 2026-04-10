package com.hvduong.detectiontomatoes.controller;

import com.hvduong.detectiontomatoes.model.entity.Batch;
import com.hvduong.detectiontomatoes.repository.BatchRepository;
import com.hvduong.detectiontomatoes.model.entity.SystemConfig;
import com.hvduong.detectiontomatoes.repository.SystemConfigRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/batch")
public class BatchManageController {
    private final BatchRepository batchRepository;
    private final SystemConfigRepository systemConfigRepository;

    public BatchManageController(BatchRepository batchRepository, SystemConfigRepository systemConfigRepository) {
        this.batchRepository = batchRepository;
        this.systemConfigRepository = systemConfigRepository;
    }

    private Map<String, Object> toBatchMap(Batch batch) {
        Map<String, Object> result = new HashMap<>();
        if (batch == null) return result;
        result.put("id", batch.getId());
        result.put("name", batch.getName());
        result.put("createdAt", batch.getCreatedAt() != null ? batch.getCreatedAt().toString() : null);
        return result;
    }

    @PostMapping("/new")
    public ResponseEntity<Map<String, Object>> createNewBatch(@RequestParam(required = false) String name) {
        Batch batch = new Batch();
        batch.setName(name != null ? name : "Batch " + System.currentTimeMillis());
        batch.setCreatedAt(LocalDateTime.now());
        batch = batchRepository.save(batch);
        
        SystemConfig config = systemConfigRepository.findById(1).orElse(SystemConfig.builder().id(1).build());
        config.setCurrentBatch(batch);
        config.setUpdatedAt(LocalDateTime.now());
        systemConfigRepository.save(config);
        
        return ResponseEntity.ok(toBatchMap(batch));
    }

    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentBatch() {
        SystemConfig config = systemConfigRepository.findById(1).orElse(null);
        if (config != null && config.getCurrentBatch() != null) {
            return ResponseEntity.ok(toBatchMap(config.getCurrentBatch()));
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllBatches() {
        List<Batch> batches = batchRepository.findAll();
        List<Map<String, Object>> result = batches.stream()
                .map(this::toBatchMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
