package com.hvduong.detectiontomatoes.controller;

import com.hvduong.detectiontomatoes.service.BatchService;
import com.hvduong.detectiontomatoes.service.FruitExportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/batch")
public class BatchManageController {
    
    private final BatchService batchService;
    private final FruitExportService fruitExportService;

    public BatchManageController(BatchService batchService, FruitExportService fruitExportService) {
        this.batchService = batchService;
        this.fruitExportService = fruitExportService;
    }

    @PostMapping("/new")
    @PreAuthorize("hasAuthority('MANAGE_BATCH')")
    public ResponseEntity<Map<String, Object>> createNewBatch(@RequestParam(required = false) String name) {
        return ResponseEntity.ok(batchService.createNewBatch(name));
    }

    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentBatch() {
        Map<String, Object> currentBatch = batchService.getCurrentBatch();
        if (currentBatch != null) {
            return ResponseEntity.ok(currentBatch);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/current/export")
    public void exportCurrentBatchToExcel(HttpServletResponse response) throws IOException {
        Integer currentBatchId = batchService.getCurrentBatchId();
        if (currentBatchId != null) {
            fruitExportService.exportFruitsByBatch(currentBatchId, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No active batch found");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllBatches() {
        return ResponseEntity.ok(batchService.getAllBatches());
    }
}
