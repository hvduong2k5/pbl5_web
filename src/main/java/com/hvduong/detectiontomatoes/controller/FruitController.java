package com.hvduong.detectiontomatoes.controller;

import com.hvduong.detectiontomatoes.model.dto.AiResponseDTO;
import com.hvduong.detectiontomatoes.model.entity.Fruit;
import com.hvduong.detectiontomatoes.model.entity.Batch;
import com.hvduong.detectiontomatoes.model.entity.SystemConfig;
import com.hvduong.detectiontomatoes.repository.FruitRepository;
import com.hvduong.detectiontomatoes.repository.SystemConfigRepository;
import com.hvduong.detectiontomatoes.service.FruitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fruit")
public class FruitController {
    private final FruitService fruitService;
    private final FruitRepository fruitRepository;
    private final SystemConfigRepository systemConfigRepository;

    public FruitController(FruitService fruitService, FruitRepository fruitRepository, SystemConfigRepository systemConfigRepository) {
        this.fruitService = fruitService;
        this.fruitRepository = fruitRepository;
        this.systemConfigRepository = systemConfigRepository;
    }

    @PostMapping
    public ResponseEntity<Void> aiResponse(@RequestBody AiResponseDTO dto) {
        System.out.println("[DEBUG AI POST] Nhận dữ liệu từ Server AI: " + dto);
        fruitService.handleAiResponse(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllFruits() {
        List<Fruit> fruits = fruitRepository.findAll();
        List<Map<String, Object>> result = fruits.stream().map(f -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", f.getId());
            map.put("status", f.getStatus());
            map.put("label", f.getLabel());
            map.put("sortedType", f.getSortedType());
            map.put("createdAt", f.getCreatedAt() != null ? f.getCreatedAt().toString() : null);
            map.put("classifiedAt", f.getClassifiedAt() != null ? f.getClassifiedAt().toString() : null);
            map.put("sortedAt", f.getSortedAt() != null ? f.getSortedAt().toString() : null);
            map.put("imageUrl", f.getImageUrl());
            map.put("confidence", f.getConfidence());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Integer>> getStats() {
        SystemConfig config = systemConfigRepository.findById(1).orElse(null);
        if (config != null && config.getCurrentBatch() != null) {
            Batch batch = config.getCurrentBatch();
            return ResponseEntity.ok(fruitService.getStatsByBatch(batch.getId()));
        }
        
        Map<String, Integer> emptyStats = new HashMap<>();
        emptyStats.put("total", 0);
        emptyStats.put("wait", 0);
        emptyStats.put("ripe", 0);
        emptyStats.put("unripe", 0);
        emptyStats.put("rotten", 0);
        return ResponseEntity.ok(emptyStats);
    }
}
