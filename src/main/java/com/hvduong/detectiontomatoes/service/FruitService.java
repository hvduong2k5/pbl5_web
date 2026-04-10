package com.hvduong.detectiontomatoes.service;

import com.hvduong.detectiontomatoes.model.entity.Fruit;
import com.hvduong.detectiontomatoes.model.entity.Batch;
import com.hvduong.detectiontomatoes.model.entity.SystemConfig;
import com.hvduong.detectiontomatoes.model.dto.FruitEventDTO;
import com.hvduong.detectiontomatoes.model.dto.AiResponseDTO;
import com.hvduong.detectiontomatoes.model.mapper.FruitMapper;
import com.hvduong.detectiontomatoes.repository.FruitRepository;
import com.hvduong.detectiontomatoes.repository.SystemConfigRepository;
import com.hvduong.detectiontomatoes.websocket.WebSocketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

@Service
public class FruitService {
    private final FruitRepository fruitRepository;
    private final FruitMapper fruitMapper;
    private final WebSocketHandler webSocketHandler;
    private final SystemConfigRepository systemConfigRepository;

    public FruitService(FruitRepository fruitRepository, FruitMapper fruitMapper, WebSocketHandler webSocketHandler, SystemConfigRepository systemConfigRepository) {
        this.fruitRepository = fruitRepository;
        this.fruitMapper = fruitMapper;
        this.webSocketHandler = webSocketHandler;
        this.systemConfigRepository = systemConfigRepository;
    }

    private Batch getCurrentBatch() {
        SystemConfig config = systemConfigRepository.findById(1).orElse(null);
        if (config != null) {
            return config.getCurrentBatch();
        }
        return null;
    }

    private boolean isAlreadyProcessed(Fruit fruit, String event) {
        if (fruit == null) return false;
        switch (event) {
            case "detected":
                return fruit.getStatus() != null && !fruit.getStatus().equals("DETECTED");
            case "classified":
                return fruit.getStatus() != null && fruit.getStatus().equals("CLASSIFIED");
            case "sorted":
                return fruit.getStatus() != null && fruit.getStatus().equals("SORTED");
            case "transfer":
                return fruit.getStatus() != null && fruit.getStatus().equals("TRANSFERRED");
            default:
                return false;
        }
    }

    public Map<String, Integer> getStatsByBatch(Integer batchId) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", (int) fruitRepository.countByBatch_Id(batchId));
        stats.put("wait", (int) fruitRepository.countByBatch_IdAndStatus(batchId, "DETECTED")); // Wait applies only when detected but not yet classified
        
        List<Object[]> labelCounts = fruitRepository.countLabelsByBatchId(batchId);
        stats.put("ripe", 0);
        stats.put("unripe", 0);
        stats.put("rotten", 0);

        for (Object[] row : labelCounts) {
            String label = (String) row[0];
            Long count = (Long) row[1];
            if (label != null) {
                if (label.equals("green")) label = "unripe";
                stats.put(label, count.intValue());
            }
        }
        return stats;
    }

    private void broadcastStatsForBatch(Batch batch) {
        if (batch != null) {
            Map<String, Integer> stats = getStatsByBatch(batch.getId());
            webSocketHandler.broadcastStats(stats);
        }
    }

    @Transactional
    public void handleDetected(FruitEventDTO dto) {
        Fruit fruit = fruitRepository.findById(dto.getId()).orElse(null);
        if (fruit != null && isAlreadyProcessed(fruit, "detected")) return;
        Batch batch = getCurrentBatch();
        if (fruit == null) {
            fruit = fruitMapper.toEntity(dto);
            fruit.setCreatedAt(LocalDateTime.now());
            fruit.setBatch(batch);
        }
        fruit.setStatus("DETECTED");
        if (dto.getConfidence() != null) {
            fruit.setConfidence(dto.getConfidence());
        }
        if (dto.getImage_url() != null) {
            fruit.setImageUrl(dto.getImage_url());
        }
        fruitRepository.save(fruit);
        
        webSocketHandler.broadcastEvent(fruitMapper.toEventDTO(fruit, "detected"));
        broadcastStatsForBatch(batch);
    }

    @Transactional
    public void handleClassified(FruitEventDTO dto) {
        Fruit fruit = fruitRepository.findById(dto.getId()).orElse(null);
        if (fruit == null || isAlreadyProcessed(fruit, "classified")) return;
        fruit.setLabel(dto.getLabel());
        if (dto.getConfidence() != null) fruit.setConfidence(dto.getConfidence());
        if (dto.getImage_url() != null) fruit.setImageUrl(dto.getImage_url());
        fruit.setClassifiedAt(LocalDateTime.now());
        fruit.setStatus("CLASSIFIED");
        fruitRepository.save(fruit);
        
        webSocketHandler.broadcastEvent(fruitMapper.toEventDTO(fruit, "classified"));
        broadcastStatsForBatch(fruit.getBatch());
    }

    @Transactional
    public void handleSorted(FruitEventDTO dto) {
        Fruit fruit = fruitRepository.findById(dto.getId()).orElse(null);
        if (fruit == null || isAlreadyProcessed(fruit, "sorted")) return;
        fruit.setSortedType(dto.getType());
        if (dto.getConfidence() != null) fruit.setConfidence(dto.getConfidence());
        fruit.setSortedAt(LocalDateTime.now());
        fruit.setStatus("SORTED");
        fruitRepository.save(fruit);
        
        webSocketHandler.broadcastEvent(fruitMapper.toEventDTO(fruit, "sorted"));
        broadcastStatsForBatch(fruit.getBatch());
    }

    @Transactional
    public void handleAiResponse(AiResponseDTO dto) {
        System.out.println("[AI POST LOG] Nhận dữ liệu cập nhật từ AI: " + dto);
        Fruit fruit = fruitRepository.findById(dto.getId()).orElse(null);
        Batch batch = getCurrentBatch();
        
        if (fruit != null) {
            fruitMapper.updateFromAiResponse(fruit, dto);
            fruit.setClassifiedAt(LocalDateTime.now());
            fruit.setStatus("CLASSIFIED");
            fruitRepository.save(fruit);
            batch = fruit.getBatch();
        } else {
            // Nếu không tìm thấy thì tạo mới Fruit
            fruit = new Fruit();
            fruit.setId(dto.getId());
            fruit.setLabel(dto.getResult());
            fruit.setImageUrl(dto.getImageUrl());
            fruit.setConfidence(dto.getConfidence());
            fruit.setClassifiedAt(LocalDateTime.now());
            fruit.setStatus("CLASSIFIED");
            fruit.setCreatedAt(LocalDateTime.now());
            fruit.setBatch(batch);
            fruitRepository.save(fruit);
        }
        
        webSocketHandler.broadcastEvent(fruitMapper.toEventDTO(fruit, "classified"));
        broadcastStatsForBatch(batch);
    }

    @Transactional
    public void handleTransfer(FruitEventDTO dto) {
        Fruit fruit = fruitRepository.findById(dto.getId()).orElse(null);
        if (fruit == null || isAlreadyProcessed(fruit, "transfer")) return;
        if (dto.getLabel() != null) fruit.setLabel(dto.getLabel());
        if (dto.getType() != null) fruit.setSortedType(dto.getType());
        if (dto.getConfidence() != null) fruit.setConfidence(dto.getConfidence());
        fruit.setStatus("TRANSFERRED");
        fruitRepository.save(fruit);
        
        webSocketHandler.broadcastEvent(fruitMapper.toEventDTO(fruit, "transfer"));
        broadcastStatsForBatch(fruit.getBatch());
    }
}
