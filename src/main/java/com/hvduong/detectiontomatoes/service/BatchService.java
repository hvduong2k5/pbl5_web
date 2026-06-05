package com.hvduong.detectiontomatoes.service;

import com.hvduong.detectiontomatoes.model.entity.Batch;
import com.hvduong.detectiontomatoes.model.entity.SystemConfig;
import com.hvduong.detectiontomatoes.repository.BatchRepository;
import com.hvduong.detectiontomatoes.repository.SystemConfigRepository;
import com.hvduong.detectiontomatoes.websocket.WebSocketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BatchService {
    private final BatchRepository batchRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final WebSocketHandler webSocketHandler;

    public BatchService(BatchRepository batchRepository, SystemConfigRepository systemConfigRepository, WebSocketHandler webSocketHandler) {
        this.batchRepository = batchRepository;
        this.systemConfigRepository = systemConfigRepository;
        this.webSocketHandler = webSocketHandler;
    }

    public Map<String, Object> toBatchMap(Batch batch) {
        Map<String, Object> result = new HashMap<>();
        if (batch == null) return result;
        result.put("id", batch.getId());
        result.put("name", batch.getName());
        result.put("createdAt", batch.getCreatedAt() != null ? batch.getCreatedAt().toString() : null);
        return result;
    }

    @Transactional
    public Map<String, Object> createNewBatch(String name) {
        Batch batch = new Batch();
        batch.setName(name != null ? name : "Batch " + System.currentTimeMillis());
        batch.setCreatedAt(LocalDateTime.now());
        batch = batchRepository.save(batch);

        SystemConfig config = systemConfigRepository.findById(1).orElse(SystemConfig.builder().id(1).build());
        config.setCurrentBatch(batch);
        config.setUpdatedAt(LocalDateTime.now());
        systemConfigRepository.save(config);

        Map<String, Integer> emptyStats = new HashMap<>();
        emptyStats.put("total", 0);
        emptyStats.put("wait", 0);
        emptyStats.put("ripe", 0);
        emptyStats.put("unripe", 0);
        emptyStats.put("reject", 0);
        webSocketHandler.broadcastStats(emptyStats);

        return toBatchMap(batch);
    }

    public Map<String, Object> getCurrentBatch() {
        SystemConfig config = systemConfigRepository.findById(1).orElse(null);
        if (config != null && config.getCurrentBatch() != null) {
            return toBatchMap(config.getCurrentBatch());
        }
        return null;
    }
    
    public Integer getCurrentBatchId() {
        SystemConfig config = systemConfigRepository.findById(1).orElse(null);
        if (config != null && config.getCurrentBatch() != null) {
            return config.getCurrentBatch().getId();
        }
        return null;
    }

    public List<Map<String, Object>> getAllBatches() {
        List<Batch> batches = batchRepository.findAll();
        return batches.stream()
                .map(this::toBatchMap)
                .collect(Collectors.toList());
    }
}
