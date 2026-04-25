package com.hvduong.detectiontomatoes.controller;

import com.hvduong.detectiontomatoes.service.FruitExportService;
import com.hvduong.detectiontomatoes.service.FruitService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/batch")
public class BatchController {
    private final FruitService fruitService;
    private final FruitExportService fruitExportService;

    public BatchController(FruitService fruitService, FruitExportService fruitExportService) {
        this.fruitService = fruitService;
        this.fruitExportService = fruitExportService;
    }

    @GetMapping("/{batchId}/fruits")
    public List<Map<String, Object>> getFruitsByBatch(@PathVariable Integer batchId) {
        return fruitService.getFruitsByBatch(batchId);
    }

    @GetMapping("/{batchId}/export")
    public void exportToExcel(@PathVariable Integer batchId, HttpServletResponse response) throws IOException {
        fruitExportService.exportFruitsByBatch(batchId, response);
    }
}
