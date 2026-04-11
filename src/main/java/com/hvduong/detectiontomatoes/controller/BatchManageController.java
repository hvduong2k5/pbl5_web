package com.hvduong.detectiontomatoes.controller;

import com.hvduong.detectiontomatoes.model.entity.Batch;
import com.hvduong.detectiontomatoes.repository.BatchRepository;
import com.hvduong.detectiontomatoes.model.entity.SystemConfig;
import com.hvduong.detectiontomatoes.repository.SystemConfigRepository;
import com.hvduong.detectiontomatoes.service.FruitExportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
    private final FruitExportService fruitExportService;

    public BatchManageController(BatchRepository batchRepository, SystemConfigRepository systemConfigRepository, FruitExportService fruitExportService) {
        this.batchRepository = batchRepository;
        this.systemConfigRepository = systemConfigRepository;
        this.fruitExportService = fruitExportService;
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

    @GetMapping("/current/export")
    public void exportCurrentBatchToExcel(HttpServletResponse response) throws IOException {
        SystemConfig config = systemConfigRepository.findById(1).orElse(null);
        if (config != null && config.getCurrentBatch() != null) {
            fruitExportService.exportFruitsByBatch(config.getCurrentBatch().getId(), response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No active batch found");
        }
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
