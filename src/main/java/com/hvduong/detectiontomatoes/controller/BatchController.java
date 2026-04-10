package com.hvduong.detectiontomatoes.controller;

import com.hvduong.detectiontomatoes.model.entity.Fruit;
import com.hvduong.detectiontomatoes.repository.FruitRepository;
import com.hvduong.detectiontomatoes.repository.BatchRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/batch")
public class BatchController {
    private final FruitRepository fruitRepository;
    private final BatchRepository batchRepository;

    public BatchController(FruitRepository fruitRepository, BatchRepository batchRepository) {
        this.fruitRepository = fruitRepository;
        this.batchRepository = batchRepository;
    }

    @GetMapping("/{batchId}/fruits")
    public List<Map<String, Object>> getFruitsByBatch(@PathVariable Integer batchId) {
        List<Fruit> fruits = fruitRepository.findAllByBatch_Id(batchId);
        return fruits.stream().map(f -> {
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
    }
}
