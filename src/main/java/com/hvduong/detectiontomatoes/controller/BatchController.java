package com.hvduong.detectiontomatoes.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.hvduong.detectiontomatoes.model.dto.FruitExportDTO;
import com.hvduong.detectiontomatoes.model.entity.Fruit;
import com.hvduong.detectiontomatoes.repository.FruitRepository;
import com.hvduong.detectiontomatoes.repository.BatchRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
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

    @GetMapping("/{batchId}/export")
    public void exportToExcel(@PathVariable Integer batchId, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("Batch_" + batchId + "_Export", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), FruitExportDTO.class).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet("Data").build();
            int pageSize = 500;
            int pageNumber = 0;
            Page<Fruit> page;

            do {
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                page = fruitRepository.findAllByBatch_Id(batchId, pageable);
                
                List<FruitExportDTO> data = page.getContent().stream().map(fruit -> {
                    FruitExportDTO dto = new FruitExportDTO();
                    dto.setId(fruit.getId());
                    dto.setStatus(fruit.getStatus());
                    dto.setLabel(fruit.getLabel());
                    dto.setCreatedAt(fruit.getCreatedAt() != null ? fruit.getCreatedAt().toString() : null);
                    dto.setClassifiedAt(fruit.getClassifiedAt() != null ? fruit.getClassifiedAt().toString() : null);
                    dto.setSortedAt(fruit.getSortedAt() != null ? fruit.getSortedAt().toString() : null);
                    dto.setConfidence(fruit.getConfidence());

                    // Download image if URL exists
                    if (fruit.getImageUrl() != null && !fruit.getImageUrl().isEmpty()) {
                        try {
                            URL url = new URL(fruit.getImageUrl());
                            try (InputStream is = url.openStream(); ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                                int nRead;
                                byte[] dataBytes = new byte[1024];
                                while ((nRead = is.read(dataBytes, 0, dataBytes.length)) != -1) {
                                    buffer.write(dataBytes, 0, nRead);
                                }
                                buffer.flush();
                                dto.setImage(buffer.toByteArray());
                            }
                        } catch (Exception e) {
                            System.err.println("Could not download image for fruit " + fruit.getId() + ": " + e.getMessage());
                        }
                    }
                    return dto;
                }).collect(Collectors.toList());

                excelWriter.write(data, writeSheet);
                pageNumber++;
            } while (page.hasNext());
        }
    }
}
