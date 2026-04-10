package com.hvduong.detectiontomatoes.model.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import lombok.Data;

@Data
@ContentRowHeight(80)
public class FruitExportDTO {
    @ExcelProperty("ID")
    @ColumnWidth(20)
    private String id;

    @ExcelProperty("Image")
    @ColumnWidth(20)
    private byte[] image;

    @ExcelProperty("Status")
    @ColumnWidth(15)
    private String status;

    @ExcelProperty("Label")
    @ColumnWidth(15)
    private String label;

    @ExcelProperty("Created At")
    @ColumnWidth(25)
    private String createdAt;

    @ExcelProperty("Classified At")
    @ColumnWidth(25)
    private String classifiedAt;

    @ExcelProperty("Sorted At")
    @ColumnWidth(25)
    private String sortedAt;

    @ExcelProperty("Confidence")
    @ColumnWidth(15)
    private Double confidence;
}
