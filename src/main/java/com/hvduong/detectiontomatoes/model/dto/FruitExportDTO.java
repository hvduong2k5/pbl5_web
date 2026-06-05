package com.hvduong.detectiontomatoes.model.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import lombok.Data;

@Data
@ContentRowHeight(80)
public class FruitExportDTO {
    @ExcelProperty("Mã quả")
    @ColumnWidth(20)
    private String id;

    @ExcelProperty("Hình ảnh")
    @ColumnWidth(20)
    private byte[] image;

    @ExcelProperty("Trạng thái")
    @ColumnWidth(15)
    private String status;

    @ExcelProperty("Phân loại")
    @ColumnWidth(15)
    private String label;

    @ExcelProperty("Thời gian tạo")
    @ColumnWidth(25)
    private String createdAt;

    @ExcelProperty("Thời gian nhận diện")
    @ColumnWidth(25)
    private String classifiedAt;

    @ExcelProperty("Thời gian hoàn thành")
    @ColumnWidth(25)
    private String sortedAt;

    @ExcelProperty("Độ tin cậy")
    @ColumnWidth(15)
    private Double confidence;
}
