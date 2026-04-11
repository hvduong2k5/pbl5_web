package com.hvduong.detectiontomatoes.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.hvduong.detectiontomatoes.model.dto.FruitExportDTO;
import com.hvduong.detectiontomatoes.model.entity.Fruit;
import com.hvduong.detectiontomatoes.repository.FruitRepository;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FruitExportService {

    private final FruitRepository fruitRepository;
    private final MinioClient minioClient;

    @Value("${minio.bucket-name:tomatoes}")
    private String bucketName;

    public FruitExportService(FruitRepository fruitRepository, MinioClient minioClient) {
        this.fruitRepository = fruitRepository;
        this.minioClient = minioClient;
    }

    public void exportFruitsByBatch(Integer batchId, HttpServletResponse response) throws IOException {
        System.out.println("[Export] Bắt đầu xuất Excel cho Batch ID = " + batchId);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("Batch_" + batchId + "_Export", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), FruitExportDTO.class).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet("Data").build();
            int pageSize = 500;
            int pageNumber = 0;
            Page<Fruit> page;

            do {
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                page = fruitRepository.findAllByBatch_Id(batchId, pageable);

                System.out.println("[Export] Page " + pageNumber + " tìm thấy " + page.getContent().size() + " bản ghi.");

                List<FruitExportDTO> data = page.getContent().stream().map(fruit -> {
                    FruitExportDTO dto = new FruitExportDTO();
                    dto.setId(fruit.getId());
                    dto.setStatus(fruit.getStatus());
                    dto.setLabel(fruit.getLabel());
                    dto.setCreatedAt(fruit.getCreatedAt() != null ? fruit.getCreatedAt().toString() : null);
                    dto.setClassifiedAt(fruit.getClassifiedAt() != null ? fruit.getClassifiedAt().toString() : null);
                    dto.setSortedAt(fruit.getSortedAt() != null ? fruit.getSortedAt().toString() : null);
                    dto.setConfidence(fruit.getConfidence());

                    String imageUrl = fruit.getImageUrl();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        try {
                            String objectName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

                            try (InputStream is = minioClient.getObject(
                                    GetObjectArgs.builder()
                                            .bucket(bucketName)
                                            .object(objectName)
                                            .build()
                            ); ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                                
                                int nRead;
                                byte[] dataBytes = new byte[1024];
                                while ((nRead = is.read(dataBytes, 0, dataBytes.length)) != -1) {
                                    buffer.write(dataBytes, 0, nRead);
                                }
                                buffer.flush();
                                dto.setImage(buffer.toByteArray());
                            }
                        } catch (Exception e) {
                            System.err.println("[Export Error] Không thể tải ảnh từ MinIO cho fruit ID " + fruit.getId() + ": " + e.getMessage());
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
