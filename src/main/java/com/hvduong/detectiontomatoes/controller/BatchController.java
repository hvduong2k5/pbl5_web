package com.hvduong.detectiontomatoes.controller;

import com.hvduong.detectiontomatoes.model.entity.Fruit;
import com.hvduong.detectiontomatoes.repository.FruitRepository;
import com.hvduong.detectiontomatoes.repository.BatchRepository;
import com.hvduong.detectiontomatoes.service.FruitExportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/batch")
public class BatchController {
    private final FruitRepository fruitRepository;
    private final BatchRepository batchRepository;
    private final FruitExportService fruitExportService;

    public BatchController(FruitRepository fruitRepository, BatchRepository batchRepository, FruitExportService fruitExportService) {
        this.fruitRepository = fruitRepository;
        this.batchRepository = batchRepository;
        this.fruitExportService = fruitExportService;
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

    @GetMapping("/{batchId}/export")
    public void exportToExcel(@PathVariable Integer batchId, HttpServletResponse response) throws IOException {
        fruitExportService.exportFruitsByBatch(batchId, response);
    }
}
