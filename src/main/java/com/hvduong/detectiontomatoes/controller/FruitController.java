package com.hvduong.detectiontomatoes.controller;

import com.hvduong.detectiontomatoes.model.dto.AiResponseDTO;
import com.hvduong.detectiontomatoes.service.FruitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fruit")
public class FruitController {
    private final FruitService fruitService;

    public FruitController(FruitService fruitService) {
        this.fruitService = fruitService;
    }

    @PostMapping
    public ResponseEntity<Void> aiResponse(@RequestBody AiResponseDTO dto) {
        System.out.println("[DEBUG AI POST] Nhận dữ liệu từ Server AI: " + dto);
        fruitService.handleAiResponse(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllFruits() {
        return ResponseEntity.ok(fruitService.getAllFruits());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Integer>> getStats() {
        return ResponseEntity.ok(fruitService.getCurrentStats());
    }
}
